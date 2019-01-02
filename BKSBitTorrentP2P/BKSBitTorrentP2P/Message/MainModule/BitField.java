package Message.MainModule;

import Common.MainModule.CommonProperties;
import DAL.MainModule.*;

import java.util.BitSet;

public class BitField extends MessageModel {

	private static BitField bitfield;
	private FileHandler fileHandler;

	private BitField() {
		init();
	}

	private void init() {
		type = 5;
		payload = new byte[CommonProperties.getNumberOfPieces() + 1];
		content = new byte[CommonProperties.getNumberOfPieces()];
		fileHandler = FileHandler.getInstance();
		payload[0] = type;
		BitSet filePieces = fileHandler.getFilePieces();
		for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
			if (filePieces.get(i)) {
				payload[i + 1] = 1;
			}
		}
	}

	public static BitField getInstance() {
		synchronized (BitField.class) {
			if (bitfield == null) {
				bitfield = new BitField();
			}
		}
		return bitfield;
	}

	@Override
	protected synchronized int getMessageLength() {
		init();
		return payload.length;
	}

	@Override
	protected synchronized byte[] getPayload() {
		return payload;
	}

}
