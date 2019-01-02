package BKSTorrent.MainModule;

import DAL.MainModule.DataController;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client implements Runnable {
	private Socket currentSocket;
	private DataInputStream inputDataStream;
	private DataController sharedData;
	private boolean isDownloadActive;


	public Client(Socket socket, DataController data) {
		this.currentSocket = socket;
		sharedData = data;
		isDownloadActive = true;
		try {
			inputDataStream = new DataInputStream(socket.getInputStream());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		receiveMessage();
	}

	public void receiveMessage() {
		while (isDownloadActive()) {
			int messageLength = Integer.MIN_VALUE;
			messageLength = receiveMessageLength();
			if (!isDownloadActive()) {
				continue;
			}
			byte[] payload = new byte[messageLength];
			receiveMessagePayload(payload);
			sharedData.addPayload(payload);
		}

	}

	private synchronized boolean isDownloadActive() {
		// TODO Auto-generated method stub
		return isDownloadActive;
	}

	private int receiveMessageLength() {
		int len = Integer.MIN_VALUE;
		byte[] messageLength = new byte[4];
		try {
			receiveRawData(messageLength);
			len = ByteBuffer.wrap(messageLength).getInt();
		} catch (Exception e) {
			// isAlive = false;
			e.printStackTrace();
		}
		return len;
	}

	private void receiveMessagePayload(byte[] payload) {
		receiveRawData(payload);
	}

	private void receiveRawData(byte[] message) {
		try {
			inputDataStream.readFully(message);
		} catch (EOFException e) {
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
