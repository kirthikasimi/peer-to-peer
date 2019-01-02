package Common.MainModule;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHandler {

	public ConfigHandler() {

		init();
	}

	private void init() {
		System.out.println(CommonProperties.PROPERTIES_FILE_PATH);
		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(CommonProperties.PROPERTIES_CONFIG_PATH);
			properties.load(in);
		}
		catch (Exception e) {
			System.out.println("File not found : " + e.getMessage());
		}

		CommonProperties.setFileName(properties.get(CommonProperties.FILENAME).toString());
		System.out.println(CommonProperties.PROPERTIES_FILE_PATH + CommonProperties.getFileName());

		CommonProperties.setFileSize(Long.parseLong(properties.get(CommonProperties.FILESIZE).toString()));
		CommonProperties.setNumberOfPreferredNeighbors(
				Integer.parseInt(properties.get(CommonProperties.NUMBER_OF_PREFERRED_NEIGHBORS).toString()));
		CommonProperties.setOptimisticUnchokingInterval(
				Integer.parseInt(properties.get(CommonProperties.OPTIMISTIC_UNCHOKING_INTERVAL).toString()));
		CommonProperties.setPieceSize(Integer.parseInt(properties.getProperty(CommonProperties.PIECESIZE).toString()));
		CommonProperties.setUnchokingInterval(
				Integer.parseInt(properties.getProperty(CommonProperties.UNCHOKING_INTERVAL).toString()));
		CommonProperties.calculateNumberOfPieces();

	}

}
