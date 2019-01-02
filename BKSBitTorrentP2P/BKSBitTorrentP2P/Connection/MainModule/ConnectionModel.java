package Connection.MainModule;

import BKSTorrent.MainModule.*;
import Common.MainModule.LoggerHandler;
import DAL.MainModule.*;

import java.io.IOException;
import java.net.Socket;
import java.util.BitSet;

public class ConnectionModel {
	Server server;
	Client client;
	DataController dataController;
	double bytesDownloaded;
	Socket peerSocket;
	String remotePeerId;
	boolean isConnectionChoked;
	private ConnectionController connectionController = ConnectionController.getInstance();

	public double getBytesDownloaded() {
		return bytesDownloaded;
	}

	protected Server getServerInstance() {
		return server;
	}

	public synchronized void incrementTotalBytesDownloaded(long value) {
		bytesDownloaded += value;
	}

	public synchronized boolean isConnectionChoked() {
		return isConnectionChoked;
	}

	public ConnectionModel(Socket peerSocket) {
		this.peerSocket = peerSocket;
		dataController = new DataController(this);
		server = new Server(peerSocket, dataController);
		client = new Client(peerSocket, dataController);
		bootStrapNode(server, client);
		dataController.setUpload(server);
		dataController.start();
	}

	public ConnectionModel(Socket peerSocket, String peerId) {
		this.peerSocket = peerSocket;
		dataController = new DataController(this);
		server = new Server(peerSocket, peerId, dataController);
		client = new Client(peerSocket,  dataController);
		bootStrapNode(server, client);
        LoggerHandler.getInstance().logTcpConnectionTo(Node.getInstance().getNetwork().getPeerId(), peerId);
		bootStrapDataController();
	}

	private void bootStrapDataController(){
		dataController.sendHandshake();
		dataController.setUpload(server);
		dataController.start();
	}

	public void bootStrapNode(Server upload, Client download) {
		Thread uploadThread = new Thread(upload);
		Thread downloadThread = new Thread(download);
		uploadThread.start();
		downloadThread.start();
	}

	public synchronized void sendMessage(int messageLength, byte[] payload) {
		server.addMessage(messageLength, payload);
	}

	public void close() {
		try {
			peerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized String getRemotePeerId() {
		return remotePeerId;
	}

	public synchronized void broadCastHavetoAllRegisteredPeers(int fileChunkIndex) {
		connectionController.broadCastHavetoAllRegisteredPeers(fileChunkIndex);
	}

	protected synchronized void addRequestedPiece(int pieceIndex) {
		FileHandler.getInstance().addRequestedPiece(this, pieceIndex);
	}

	public synchronized void processAcceptedPeerConnections() {
		connectionController.processAcceptedPeerConnections(this,remotePeerId);
	}

	public synchronized void processRejectedPeerConnections() {
		connectionController.processRejectedPeerConnections(remotePeerId, this);
	}

	public void receiveMessage() {
		client.receiveMessage();
	}

	public void setPeerId(String value) {
		remotePeerId = value;
	}

	public synchronized void removeRequestedPiece() {
		FileHandler.getInstance().removeRequestedPiece(this);
	}

	public synchronized BitSet getPeerBitSet() {
		// TODO Auto-generated method stub
		return dataController.getPeerBitSet();
	}

	public synchronized boolean hasFile() {
		return dataController.hasFile();
	}

	public synchronized void addAllConnections() {
		// TODO Auto-generated method stub
		connectionController.addAllConnections(this);
	}

	public synchronized void setDownloadedbytes(int n) {
		bytesDownloaded = n;
	}
}
