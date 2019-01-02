package BKSTorrent.MainModule;

import Common.MainModule.*;
import DAL.MainModule.*;
import Message.MainModule.*;

import java.io.IOException;

public class BitTorrentMainController {
	public static String peerId;

	public static void main(String args[]) throws IOException {
		if(args!=null && args.length>0)
			peerId = args[0];
		else
			peerId = "1001";
		init();
		System.out.println("I am peer :"+ peerId);
		System.out.println(CommonProperties.print());
		Node current = Node.getInstance();
		current.createTCPConnections();
		current.listenForConnections();
	}

	private static void init() {
		new ConfigHandler();
		HandshakeHelper.setId(peerId);
		if (CommonProperties.getPeer(peerId).hasSharedFile) {
			FileHandler.getInstance().splitFile();
		}
	}

}
