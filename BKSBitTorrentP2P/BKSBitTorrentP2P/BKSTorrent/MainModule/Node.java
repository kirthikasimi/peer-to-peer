package BKSTorrent.MainModule;

import Common.MainModule.CommonProperties;
import Connection.MainModule.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Node {

	private static Node current = new Node();
	private NetworkModel networkModel;
	ConnectionController connectionController;
	public static boolean allPeersReceivedFiles = false;

	private Node() {
		networkModel = CommonProperties.getPeer(BitTorrentMainController.peerId);
		connectionController = ConnectionController.getInstance();
	}

	public static Node getInstance() {
		return current;
	}

	// TODO: Use filePieces of filehandler instead of network
	public boolean hasFile() {
		return networkModel.hasSharedFile;
	}
	// TODO: Optimize by maintaining index upto which all files have been received

	public NetworkModel getNetwork() {
		return networkModel;
	}

	public void setNetwork(NetworkModel network) {
		this.networkModel = network;
	}

	public void listenForConnections() throws IOException {

		ServerSocket socket = null;
		try {
			socket = new ServerSocket(networkModel.port);
			// TODO: End connection when all peers have received files
			while (false == allPeersReceivedFiles) {
				Socket peerSocket = socket.accept();
				connectionController.createConnection(peerSocket);
			}
		} catch (Exception e) {
			System.out.println("Closed exception");
		} finally {
			socket.close();
		}
	}

	public void createTCPConnections() {
		HashMap<String, NetworkModel> map = CommonProperties.getPeerList();
		int myNumber = networkModel.networkId;
		for (String peerId : map.keySet()) {
			NetworkModel peerInfo = map.get(peerId);
			if (peerInfo.networkId < myNumber) {
				new Thread() {
					@Override
					public void run() {

						createConnection(peerInfo);
					}
				}.start();

			}
		}
	}

	private void createConnection(NetworkModel peerInfo) {
		int peerPort = peerInfo.port;
		String peerHost = peerInfo.hostName;
		try {
			Socket clientSocket = new Socket(peerHost, peerPort);
			connectionController.createConnection(clientSocket, peerInfo.getPeerId());
			Thread.sleep(300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
