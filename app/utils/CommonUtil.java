package utils;

import java.io.File;

import play.data.FileUpload;

/**
 * Common Util - Common utilities for the application.
 */
public class CommonUtil {

	/**
	 * Gets the extension.
	 *
	 * @param fileName the file name
	 * @return the extension
	 */
	public static String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}

	/**
	 * Gets the extension.
	 *
	 * @param file the file
	 * @return the extension
	 */
	public static String getExtension(File file) {
		return getExtension(file.getName());
	}

	/**
	 * Gets the extension.
	 *
	 * @param fileUpload the file upload
	 * @return the extension
	 */
	public static String getExtension(FileUpload fileUpload) {
		return getExtension(fileUpload.getFileName());
	}

	/**
	 * Gets the name.
	 *
	 * @param fileName the file name
	 * @return the name
	 */
	public static String getName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

}