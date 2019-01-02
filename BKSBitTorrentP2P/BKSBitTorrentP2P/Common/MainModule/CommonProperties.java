package Common.MainModule;

import Connection.MainModule.NetworkModel;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class CommonProperties {

	private static int numberOfPreferredNeighbors;
	private static int unchokingInterval;
	private static int optimisticUnchokingInterval;
	private static String fileName;
	private static long fileSize;
	private static int pieceSize;
	private static int numberOfPieces;

	private static HashMap<String, NetworkModel> peerList = new HashMap<>();


	public static NetworkModel getPeer(String id) {
		return peerList.get(id);
	}

	public static HashMap<String, NetworkModel> getPeerList() {
		return peerList;
	}

	public static int numberOfPeers() {
		return peerList.size();
	}

	public static final String PROPERTIES_CONFIG_PATH = System.getProperty("user.dir") + File.separatorChar
			+ "Common.cfg";
	public static final String PROPERTIES_FILE_PATH = System.getProperty("user.dir") + File.separatorChar;
	public static final String PROPERTIES_CREATED_FILE_PATH = System.getProperty("user.dir") + File.separatorChar
			+ "project/peer_";
	public static final String NUMBER_OF_PREFERRED_NEIGHBORS = "NumberOfPreferredNeighbors";
	public static final String UNCHOKING_INTERVAL = "UnchokingInterval";
	public static final String OPTIMISTIC_UNCHOKING_INTERVAL = "OptimisticUnchokingInterval";
	public static final String FILENAME = "FileName";
	public static final String FILESIZE = "FileSize";
	public static final String PIECESIZE = "PieceSize";

	public static final String PEER_PROPERTIES_CONFIG_PATH = System.getProperty("user.dir") + File.separatorChar
			+ "PeerInfo.cfg";

	public static final String PEER_LOG_FILE_PATH = System.getProperty("user.dir") + File.separatorChar
			+ "project/log_peer_";
	public static final String PEER_LOG_FILE_EXTENSION = ".log";

	public static int getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}

	public static int getUnchokingInterval() {
		return unchokingInterval;
	}

	public static int getNumberOfPieces() {
		return numberOfPieces;
	}

	public static void calculateNumberOfPieces() {
		numberOfPieces = (int) (fileSize % pieceSize) == 0 ? (int) (fileSize / pieceSize)
				: (int) (fileSize / pieceSize) + 1;
		System.out.println("CommonProperties.calculateNumberOfPieces - Number of pieces: " + numberOfPieces);
	}

	public static void setUnchokingInterval(int p) {
		unchokingInterval = p;
	}

	public static int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public static void setOptimisticUnchokingInterval(int m) {
		optimisticUnchokingInterval = m;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String name) {
		fileName = name;
	}

	public static long getFileSize() {
		return fileSize;
	}

	public static void setFileSize(long size) {
		fileSize = size;
	}

	public static int getPieceSize() {
		return pieceSize;
	}

	public static void setPieceSize(int size) {
		pieceSize = size;
	}

	static {
		int id = 1;
		try {
			Scanner sc = new Scanner(new File(CommonProperties.PEER_PROPERTIES_CONFIG_PATH));
			while (sc.hasNextLine()) {
				String str[] = sc.nextLine().split(" ");
				NetworkModel network = new NetworkModel();
				network.networkId = id++;
				network.peerId= str[0];
				network.hostName = str[1];
				network.port = Integer.parseInt(str[2]);
				network.setHasSharedFile(str[3].equals("1") ? true : false);
				peerList.put(str[0], network);
			}
			sc.close();
		} catch (IOException e) {
			System.out.println("PeerInfo.cfg not found/corrupt");
		}
	}

	public static String print() {
		return "PeerProperties [numberOfPreferredNeighbors=" + numberOfPreferredNeighbors + ", unchokingInterval="
				+ unchokingInterval + ", optimisticUnchokingInterval=" + optimisticUnchokingInterval + ", fileName="
				+ fileName + ", fileSize=" + fileSize + ", pieceSize=" + pieceSize + "]";
	}

	public static void setNumberOfPreferredNeighbors(int k) {
		numberOfPreferredNeighbors = k;
	}
	public static String getTime() {
		return Calendar.getInstance().getTime() + ": ";
	}

}
