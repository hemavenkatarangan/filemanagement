package com.s3.filemanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class FileManagementService {

	Logger logger = Logger.getLogger(FileManagementService.class.getName());
	private String SERVER_UPLOAD_LOCATION_FOLDER = "";
	private String STORAGE_SYSTEM="";
	
	@Autowired
    public FileManagementService(Environment env) {

        logger.info("UPLOAD Directory"+env.getProperty("upload-dir"));
        this.SERVER_UPLOAD_LOCATION_FOLDER =env.getProperty("upload-dir");
        this.STORAGE_SYSTEM = env.getProperty("storage_system");
        
    }
	
	
	/**
	 * API method to upload a file, Uploaded file will be scanned with clamav and then if scan is fine, file will be uploaded to Datalake 
	 * 
	 * 
	 * @param emailId
	 *
	 * @param language
	 *
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
   	@CrossOrigin(origins = "*")
	public ResponseEntity upload(@RequestParam("files") MultipartFile[] files,@RequestParam("course_name") String courseName) throws IOException {
		String fileName = "";
		String dirPath = "";

		if (files == null || files.length == 0) {
			throw new RuntimeException("You must select at least one file for uploading");
		}

		StringBuilder sb = new StringBuilder(files.length);
		ArrayList fileNames=new ArrayList();

		for (int i = 0; i < files.length; i++) {
			InputStream inputStream = null;
			try {
				inputStream = files[i].getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String originalName = files[i].getOriginalFilename();
			String name = files[i].getName();
			String contentType = files[i].getContentType();
			long size = files[i].getSize();

			sb.append("File Name: " + originalName + "\n");

			logger.info("InputStream: " + inputStream);
			logger.info("OriginalName: " + originalName);
			logger.info("Name: " + name);
			logger.info("ContentType: " + contentType);
			logger.info("Size: " + size);
			
			dirPath = SERVER_UPLOAD_LOCATION_FOLDER + File.separator + courseName;
			logger.info("Uploading path "+dirPath);
			
			// create dir if not there
						File dir = new File(dirPath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
						dirPath = dirPath + File.separator + files[i].getOriginalFilename();
						
						// check filename length, if exceeds 50 return invalid filename
						int fileNameLength = 0;
						fileNameLength = fileName.length();
						if (fileNameLength > 250) {
							JSONObject responseObject = new JSONObject();
							responseObject.put("status_code", HttpStatus.NOT_ACCEPTABLE);
							responseObject.put("status_message", "FileName length cannot exceed 250 characters ");
							logger.info("File name is exceeded ,its more than 250 characters ,please check");

							inputStream.close();
							return new ResponseEntity(responseObject.toString(), HttpStatus.NOT_ACCEPTABLE);
						}
			String filePath=saveFile(inputStream,dirPath);
			
			fileNames.add(filePath);
			
		}
		
		JSONObject responseObject = new JSONObject();
		responseObject.put("status_code", HttpStatus.OK);
		responseObject.put("status_message", "Uploaded Successfully ");
		responseObject.put("result", fileNames);
		return new ResponseEntity(responseObject.toString(), HttpStatus.OK);
		
		
	} // method uploadFile

	
	/**
	 * Method to save file locally
	 * 
	 * @param is
	 * @param fileLocation
	 * @throws IOException
	 */
	private String saveFile(InputStream is, String fileLocation)  {
		logger.info("saveFile --> " + fileLocation);
		File file =new File(fileLocation);
		try (OutputStream os = new FileOutputStream(new File(fileLocation))) {
			byte[] buffer = new byte[256];
			int bytes = 0;
			while ((bytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytes);
			}
		} catch (FileNotFoundException e) {
			logger.info("File not found excpetion "+e.getLocalizedMessage());
		} catch (IOException e) {
			logger.info("IO excpetion "+e.getLocalizedMessage());
		}
		if(fileLocation.contains("/images"))
		{
			
			String s1 = fileLocation.substring(fileLocation.indexOf("/images") + 1);
			fileLocation=s1.trim();
			
		}
		if(fileLocation.contains("\\images"))
		{
			
			String s1 = fileLocation.substring(fileLocation.indexOf("\\images") + 1);
			fileLocation=s1.trim();
			
		}
		while(fileLocation.contains("\\"))
		{
		fileLocation = fileLocation.replace("\\", "/");
		}
		logger.info(fileLocation);
		return fileLocation;

	}
}
