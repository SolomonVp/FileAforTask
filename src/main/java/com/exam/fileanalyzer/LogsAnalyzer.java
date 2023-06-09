package com.exam.fileanalyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class LogsAnalyzer
{
		/**
		 * Given a zip file, a search query, and a date range,
		 * count the number of occurrences of the search query in each file in the zip file
		 *
		 * @param searchQuery The string to search for in the file.
		 * @param zipFile The zip file to search in.
		 * @param startDate The start date of the search.
		 * @param numberOfDays The number of days to search for.
		 * @return A map of file names and the number of occurrences of the search query in the file.
		 */
		public Map<String, Integer> countEntriesInZipFile(
			String searchQuery, File zipFile, LocalDate startDate, Integer numberOfDays)
			throws IOException
		{
			Map<String, Integer> entryCountMap = new HashMap<>();

			try {
				File tempDir = createTempDirectory();
				unzipFiles(zipFile.getAbsolutePath(), tempDir);

				for (int i = 0; i < numberOfDays; i++) {
					LocalDate currentDate = startDate.plusDays(i);
					String fileName = "logs_" + currentDate.toString() + "-access.log";
					File logFile = new File(tempDir, fileName);

					if (logFile.exists()) {
						log.info("log exists");
						int count = countOccurrencesInFile(searchQuery, logFile);
						entryCountMap.put(fileName, count);
					}
				}

				deleteTempDirectory(tempDir);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			return entryCountMap;
		}

	private File createTempDirectory() throws IOException {
		// Создание временной директории
		return Files.createTempDirectory("logs").toFile();
	}

	private void unzipFiles(String zipPath, File destination) throws IOException {
		try (ZipFile zipFile = new ZipFile(zipPath)) {
			zipFile.stream().forEach(entry -> {
				try {
					Path destPath = new File(destination, entry.getName()).toPath();
					Files.copy(zipFile.getInputStream(entry), destPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
						log.info(e.getMessage());
				}
			});
		}
	}

	private int countOccurrencesInFile(String searchString, File file) throws IOException {
		int count = 0;

		for (String line : Files.readAllLines(file.toPath())) {
			if (line.contains(searchString)) {
				count++;
			}
		}

		return count;
	}

	private void deleteTempDirectory(File tempDir) {
		tempDir.delete();
	}
}
