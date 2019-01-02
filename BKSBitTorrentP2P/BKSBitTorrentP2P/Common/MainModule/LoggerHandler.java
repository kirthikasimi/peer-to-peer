package Common.MainModule;

import BKSTorrent.MainModule.Node;
import Connection.MainModule.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.PriorityQueue;

public class LoggerHandler {

	private static LoggerHandler customLogger;

	public static PrintWriter printWriter = null;

	private LoggerHandler() {
		try {
			System.out.println("Current Peer:" + Node.getInstance().getNetwork().getPeerId());

			File file = new File(CommonProperties.PEER_LOG_FILE_PATH + Node.getInstance().getNetwork().getPeerId()
					+ CommonProperties.PEER_LOG_FILE_EXTENSION);
			file.getParentFile().mkdirs(); // Will create parent directories if not exists
			file.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(file, false);
			printWriter = new PrintWriter(fileOutputStream, true);
		} catch (Exception e) {
			System.out.println("Error: Failed to create log file");
		}
	}

	public static LoggerHandler getInstance() {
		synchronized (LoggerHandler.class) {
			if (customLogger == null) {
				customLogger = new LoggerHandler();
			}
		}
		return customLogger;
	}

	private void writeToFile(String message) {
		synchronized (this) {
			printWriter.println(message);
		}
	}

	// [Time]: Peer [peer_ID 1] makes a connection to Peer [peer_ID 2].
	public void logTcpConnectionTo(String peerFrom, String peerTo) {
		writeToFile(CommonProperties.getTime() + "Peer " + peerFrom + " makes a connection to Peer " + peerTo + ".");
	}

	public void logTcpConnectionFrom(String peerFrom, String peerTo) {
		writeToFile(CommonProperties.getTime() + "Peer " + peerFrom + " is connected from Peer " + peerTo + ".");
	}

	public void logChangePreferredNeighbors(String timestamp, String peerId, PriorityQueue<ConnectionModel> peers) {
		StringBuilder log = new StringBuilder();
		log.append(timestamp);
		log.append("Peer " + peerId + " has the preferred neighbors ");
		String prefix = "";
		Iterator<ConnectionModel> iter = peers.iterator();
		while (iter.hasNext()) {
			log.append(prefix);
			prefix = ", ";
			log.append(iter.next().getRemotePeerId());
		}
		writeToFile(log.toString() + ".");
	}


	public void logOptimisticallyUnchokeNeighbor(String timestamp, String source, String unchokedNeighbor) {
		writeToFile(
				timestamp + "Peer " + source + " has the optimistically unchoked neighbor " + unchokedNeighbor + ".");
	}

	public void logUnchokNeighbor(String timestamp, String peerId1, String peerId2) {
		writeToFile(timestamp + "Peer " + peerId1 + " is unchoked by " + peerId2 + ".");
	}

	// [Time]: Peer [peer_ID 1] is choked by [peer_ID 2].
	public void logChokNeighbor(String timestamp, String peerId1, String peerId2) {
		writeToFile(timestamp + "Peer " + peerId1 + " is choked by " + peerId2 + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘have’ message from [peer_ID 2] for
	// the piece [piece index].
	public void logReceivedHaveMessage(String timestamp, String to, String from, int pieceIndex) {
		writeToFile(timestamp + "Peer " + to + " received the 'have' message from " + from + " for the piece "
				+ pieceIndex + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘interested’ message from [peer_ID
	// 2].
	public void logInterestedMessage(String timestamp, String to, String from) {
		writeToFile(timestamp + "Peer " + to + " received the 'interested' message from " + from + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘not interested’ message from
	// [peer_ID 2].
	public void logNotInterestedMessage(String timestamp, String to, String from) {
		writeToFile(timestamp + "Peer " + to + " received the 'not interested' message from " + from + ".");
	}

	// [Time]: Peer [peer_ID 1] has downloaded the piece [piece index] from
	// [peer_ID 2].
	public void logDownloadedPiece(String timestamp, String to, String from, int pieceIndex, int numberOfPieces) {
		String message = timestamp + "Peer " + to + " has downloaded the piece " + pieceIndex + " from " + from + ".";
		message += "Now the number of pieces it has is " + numberOfPieces;
		writeToFile(message);

	}

	public void logDownloadComplete(String timestamp, String peerId) {
		writeToFile(timestamp + "Peer " + peerId + " has downloaded the complete file.");
	}



}