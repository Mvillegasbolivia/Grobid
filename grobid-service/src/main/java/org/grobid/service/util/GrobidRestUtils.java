package org.grobid.service.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.service.exceptions.GrobidServiceException;
// RL: ajout pour respecter le tmpPath des propriétés
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Damien
 * 
 */
public class GrobidRestUtils {
	
	// RL: ajout pour tmpPath
	static File tempDir = GrobidProperties.getTempPath();
	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestUtils.class);


	/**
	 * Check whether the result is null or empty.
	 * 
	 * @param result
	 *            the result of the process.
	 * @return true if the result is not null and not empty, false else.
	 */
	public static boolean isResultOK(String result) {
		return StringUtils.isBlank(result) ? false : true;
	}

	/**
	 * Write an input stream in temp directory.
	 * 
	 * @param inputStream
	 * @return
	 */
	public static File writeInputFile(InputStream inputStream) {
		LOGGER.debug(">> set origin document for stateless service'...");

		File originFile = null;
		OutputStream out = null;
		try {
			originFile = newTempFile("origin", "pdf");

			out = new FileOutputStream(originFile);

			byte buf[] = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (IOException e) {
			LOGGER.error(
					"An internal error occurs, while writing to disk (file to write '"
							+ originFile + "').", e);
			originFile = null;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				inputStream.close();
			} catch (IOException e) {
				LOGGER.error("An internal error occurs, while writing to disk (file to write '"
						+ originFile + "').");
				originFile = null;
			}
		}
		return originFile;
	}

	/**
	 * Creates a new not used temprorary file and returns it.
	 * 
	 * @return
	 */
	public static File newTempFile(String fileName, String extension) {
		try {
			// modif RL: ajout d'un 3e argument: File tempDir
			return File.createTempFile(fileName, extension, tempDir);
		} catch (IOException e) {
			throw new GrobidServiceException(
					"Could not create temprorary file, '" + fileName + "."
							+ extension + "'.");
		}
	}

	/**
	 * Delete the temporary file.
	 * 
	 * @param file
	 *            the file to delete.
	 */
	public static void removeTempFile(final File file) {
		try {
			LOGGER.debug("Removing " + file.getAbsolutePath());
			file.delete();
		} catch (Exception exp) {
			LOGGER.error("Error while deleting the temporary file: " + exp);
		}
	}

	/**
	 * @return a new engine from GrobidFactory if the execution is parallel,
	 *         else return the instance of engine.
	 */
	public static Engine getEngine(boolean isparallelExec) {
		return isparallelExec ? GrobidPoolingFactory.getEngineFromPool()
				: GrobidFactory.getInstance().getEngine();
	}

}
