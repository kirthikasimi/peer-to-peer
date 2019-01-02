package BKSTorrent.MainModule;

import DAL.MainModule.DataController;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;

public class Server implements Runnable {
	private Socket socket;
	private DataOutputStream outputDataStream;
	protected BlockingQueue<Integer> outboundMessageLengthQueue;
	protected BlockingQueue<byte[]> outboundMessageQueue;
	private boolean isConnectionActive;

	// client thread initialization
	public Server(Socket socket, String id, DataController data) {
		init(socket, data);
	}

	// server thread initialization
	public Server(Socket socket, DataController data) {
		init(socket, data);
	}

	private void init(Socket clientSocket, DataController data) {
		outboundMessageQueue = new LinkedBlockingQueue<>();
		outboundMessageLengthQueue = new LinkedBlockingQueue<>();
		isConnectionActive = true;
		this.socket = clientSocket;
		try {
			outputDataStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (isConnectionActive) {
			try {
				int messageLength = outboundMessageLengthQueue.take();
				outputDataStream.writeInt(messageLength);
				outputDataStream.flush();
				byte[] message = outboundMessageQueue.take();
				outputDataStream.write(message);
				outputDataStream.flush();
			}
			catch (SocketException e) {
				isConnectionActive = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addMessage(int length, byte[] payload) {
		try {
			outboundMessageLengthQueue.put(length);
			outboundMessageQueue.put(payload);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
