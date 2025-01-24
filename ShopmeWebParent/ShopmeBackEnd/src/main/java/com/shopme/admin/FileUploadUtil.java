package com.shopme.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;


public class FileUploadUtil {
	// Saves a file (uploaded by a user) to a specified directory.
	public static void saveFile(String uploadDir, // The directory where the file will be saved.
			String fileName, // The name of the file to save.
			// The file uploaded by the user, provided as a MultipartFile.
			MultipartFile multipartFile) throws IOException {
		// Converts uploadDir (a String) into a Path object.
		Path uploadPath = Paths.get(uploadDir);
		
		// If the directory doesn't exist, it creates it
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		
		try (InputStream inputStream = multipartFile.getInputStream()) {
			// Resolves the path for the file (uploadDir + fileName).
			Path filePath = uploadPath.resolve(fileName);
			// Copies the file's contents to the resolved path, If a file with the same name already exists, it replaces it 
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new IOException("Could not save file: " + fileName, ex);
		}
	}
	
	// Deletes all files in a specified directory.
	public static void cleanDir(String dir) {
		// Converts the dir string into a Path.
		Path dirPath = Paths.get(dir);
		
		try {
			// Uses Files.list() to get a list of all files and directories in the path.
			Files.list(dirPath).forEach(file -> {
				// If it's not a directory, attempts to delete it using Files.delete().
				if (!Files.isDirectory(file)) {
					try {
						Files.delete(file);
					}
					catch (IOException ex) {
						System.out.println("Could not delete file: " + file);
					}
				}
			});
		}
		catch (IOException ex) {
			System.out.println("Could not list directory: " + dirPath);
		}
	}
}
