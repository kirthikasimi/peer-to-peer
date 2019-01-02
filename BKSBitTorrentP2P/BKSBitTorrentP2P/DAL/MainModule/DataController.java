package DAL.MainModule;

import BKSTorrent.MainModule.*;
import Common.MainModule.*;
import Connection.MainModule.*;
import Message.MainModule.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.*;

public class DataController extends Thread {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private ConnectionModel activeConnection;
	private volatile boolean uploadHandshake;
	private volatile boolean isHandshakeDownloaded;
	private FileHandler dataController;
	private MessageBroadcastThreadPoolHandler broadcaster;
	private boolean peerHasFile;
	private Node node = Node.getInstance();
	private BlockingQueue<byte[]> messageQueue;
	private boolean isDataControllerInstanceAlive;
	Server server;

	public DataController(ConnectionModel connection) {
		activeConnection = connection;
		messageQueue = new LinkedBlockingQueue<>();
		isDataControllerInstanceAlive = true;
		dataController = FileHandler.getInstance();
		broadcaster = MessageBroadcastThreadPoolHandler.getInstance();
		peerBitset = new BitSet(CommonProperties.getNumberOfPieces());
	}

	public void setUpload(Server value) {
		server = value;
		if (getUploadHandshake()) {
			broadcaster.addMessage(new Object[] { activeConnection, MessageModel.Type.HANDSHAKE, Integer.MIN_VALUE });
		}
	}

	@Override
	public void run() {
		while (isDataControllerInstanceAlive) {
			try {
				byte[] messageItem = messageQueue.take();
				processMessage(messageItem);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void addPayload(byte[] payload) {
		try {
			messageQueue.put(payload);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized BitSet getPeerBitSet() {
		return peerBitset;
	}

	public synchronized void sendHandshake() {
		setUploadHandshake();
	}

	public synchronized void setUploadHandshake() {
		uploadHandshake = true;
	}

	public synchronized boolean getUploadHandshake() {
		return uploadHandshake;
	}

	public void updatePeerId(String peerId) {
		remotePeerId = peerId;
	}

	public synchronized String getRemotePeerId() {
		return remotePeerId;
	}

	public synchronized void setRemotePeerId(String remotePeerId) {
		this.remotePeerId = remotePeerId;
	}

	public synchronized void setBitfieldSent() {
		bitfieldSent = true;
	}

	public synchronized void setPeerBitset(byte[] payload) {
		for (int i = 1; i < payload.length; i++) {
			if (payload[i] == 1) {
				peerBitset.set(i - 1);
			}
		}
		if (peerBitset.cardinality() == CommonProperties.getNumberOfPieces()) {
			peerHasFile = true;
			ConnectionController.getInstance().addToPeersWithFullFile(remotePeerId);
		}
	}

	public synchronized void updatePeerBitset(int index) {
		peerBitset.set(index);
		if (peerBitset.cardinality() == CommonProperties.getNumberOfPieces()) {
			ConnectionController.getInstance().addToPeersWithFullFile(remotePeerId);
			peerHasFile = true;
		}
	}

	private MessageModel.Type processChoke()
	{
		LoggerHandler.getInstance().logChokNeighbor(CommonProperties.getTime(), BitTorrentMainController.peerId, activeConnection.getRemotePeerId());
		activeConnection.removeRequestedPiece();
		return null;
	}

	private MessageModel.Type processInterested(){
		LoggerHandler.getInstance().logInterestedMessage(CommonProperties.getTime(), BitTorrentMainController.peerId,
				activeConnection.getRemotePeerId());
		activeConnection.processAcceptedPeerConnections();
		return null;
	}

	private MessageModel.Type processNotInterested(){
		LoggerHandler.getInstance().logNotInterestedMessage(CommonProperties.getTime(), BitTorrentMainController.peerId,
				activeConnection.getRemotePeerId());
		activeConnection.processRejectedPeerConnections();
		return null;
	}
	protected void processMessage(byte[] message) {
		MessageModel.Type messageType = getMessageType(message[0]);
		MessageModel.Type responseMessageType = null;
		int fileChunkIndex = Integer.MIN_VALUE;
		System.out.println("Received message: " + messageType);
		switch (messageType) {
				case CHOKE:	responseMessageType = processChoke();
				break;
			case UNCHOKE:
				// respond with request
				LoggerHandler.getInstance().logUnchokNeighbor(CommonProperties.getTime(), BitTorrentMainController.peerId, activeConnection.getRemotePeerId());
				responseMessageType = MessageModel.Type.REQUEST;
				fileChunkIndex = dataController.getRequestPieceIndex(activeConnection);
				break;
			case INTERESTED: responseMessageType = processInterested();
				break;
			case NOTINTERESTED: responseMessageType = processNotInterested();
				break;
			case HAVE:
				fileChunkIndex = ByteBuffer.wrap(message, 1, 4).getInt();
				LoggerHandler.getInstance().logReceivedHaveMessage(CommonProperties.getTime(), BitTorrentMainController.peerId, activeConnection.getRemotePeerId(),
						fileChunkIndex);
				updatePeerBitset(fileChunkIndex);
				responseMessageType = getInterestedNotInterested();
				break;
			case BITFIELD:
				setPeerBitset(message);
				responseMessageType = getInterestedNotInterested();
				break;
			case REQUEST:
				// send requested piece
				responseMessageType = MessageModel.Type.PIECE;
				byte[] content = new byte[4];
				System.arraycopy(message, 1, content, 0, 4);
				fileChunkIndex = ByteBuffer.wrap(content).getInt();
				// System.out.println(pieceIndex);
				if (fileChunkIndex == Integer.MIN_VALUE) {
					System.out.println("received file");
					responseMessageType = null;
				}
				break;
			case PIECE:
				/*
				 * update own bitset & file . Send have to all neighbors & notinterested to
				 * neighbors with same bitset. Respond with request update bytesDownloaded pi =
				 * pieceIndex
				 */
				fileChunkIndex = ByteBuffer.wrap(message, 1, 4).getInt();
				activeConnection.incrementTotalBytesDownloaded(message.length);
				dataController.setPiece(Arrays.copyOfRange(message, 1, message.length));
				LoggerHandler.getInstance().logDownloadedPiece(CommonProperties.getTime(), BitTorrentMainController.peerId, activeConnection.getRemotePeerId(),
						fileChunkIndex, dataController.getReceivedFileSize());
				responseMessageType = MessageModel.Type.REQUEST;
				activeConnection.broadCastHavetoAllRegisteredPeers(fileChunkIndex);
				fileChunkIndex = dataController.getRequestPieceIndex(activeConnection);
				if (fileChunkIndex == Integer.MIN_VALUE) {
					LoggerHandler.getInstance().logDownloadComplete(CommonProperties.getTime(), BitTorrentMainController.peerId);
					dataController.writeToFile(BitTorrentMainController.peerId);
					messageType = null;
					isDataControllerInstanceAlive = false;
					responseMessageType = null;
					// conn.close();
				}
				break;
			case HANDSHAKE: processHandshake(message,responseMessageType,fileChunkIndex);
				break;
		}

		if (null != responseMessageType) {
			broadcaster.addMessage(new Object[] { activeConnection, responseMessageType, fileChunkIndex });
		}
	}

	private void processPiece(int fileChunkIndex, byte[] message, MessageModel.Type responseMessageType,
							  MessageModel.Type messageType){
		fileChunkIndex = ByteBuffer.wrap(message, 1, 4).getInt();
		activeConnection.incrementTotalBytesDownloaded(message.length);
		dataController.setPiece(Arrays.copyOfRange(message, 1, message.length));
		LoggerHandler.getInstance().logDownloadedPiece(CommonProperties.getTime(), BitTorrentMainController.peerId, activeConnection.getRemotePeerId(),
				fileChunkIndex, dataController.getReceivedFileSize());
		responseMessageType = MessageModel.Type.REQUEST;
		activeConnection.broadCastHavetoAllRegisteredPeers(fileChunkIndex);
		fileChunkIndex = dataController.getRequestPieceIndex(activeConnection);
		if (fileChunkIndex == Integer.MIN_VALUE) {
			LoggerHandler.getInstance().logDownloadComplete(CommonProperties.getTime(), BitTorrentMainController.peerId);
			dataController.writeToFile(BitTorrentMainController.peerId);
			messageType = null;
			isDataControllerInstanceAlive = false;
			responseMessageType = null;
		}
		if (null != responseMessageType) {
			broadcaster.addMessage(new Object[] { activeConnection, responseMessageType, fileChunkIndex });
		}
	}


	private void processHandshake(byte[] message, MessageModel.Type responseMessageType, int fileChunkIndex){
		remotePeerId = HandshakeHelper.getId(message);
		activeConnection.setPeerId(remotePeerId);
		activeConnection.addAllConnections();
		if (!getUploadHandshake()) {
			setUploadHandshake();
			LoggerHandler.getInstance().logTcpConnectionFrom(node.getNetwork().getPeerId(), remotePeerId);
			broadcaster.addMessage(new Object[] { activeConnection, MessageModel.Type.HANDSHAKE, Integer.MIN_VALUE });
		}
		if (dataController.hasAnyPieces()) {
			responseMessageType = MessageModel.Type.BITFIELD;
		}
		if (null != responseMessageType) {
			broadcaster.addMessage(new Object[] { activeConnection, responseMessageType, fileChunkIndex });
		}
	}

	private boolean isInterested() {
		for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
			if (peerBitset.get(i) && !dataController.isPieceAvailable(i)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasFile() {
		return peerHasFile;
	}

	private MessageModel.Type getInterestedNotInterested() {
		if (isInterested()) {
			return MessageModel.Type.INTERESTED;
		}
		return MessageModel.Type.NOTINTERESTED;
	}

	private MessageModel.Type getMessageType(byte type) {
		MessageController messageManager = MessageController.getInstance();
		if (!isHandshakeDownloaded()) {
			setHandshakeDownloaded();
			return MessageModel.Type.HANDSHAKE;
		}
		return messageManager.getType(type);
	}

	private boolean isHandshakeDownloaded() {
		return isHandshakeDownloaded;
	}

	private void setHandshakeDownloaded() {
		isHandshakeDownloaded = true;
	}

}
