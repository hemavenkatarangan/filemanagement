package com.s3.filemanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Path;

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
	 * @return 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping(value = "/upload")
	@ResponseBody
	@CrossOrigin(origins = "*")
	public ResponseEntity upload(@RequestPart("file") MultipartFile files,@RequestParam("course_name") String courseName) {
		

		logger.info("*******************************UPLOAD FILE START**************************************************"
				+ files.getName());
		logger.info("File Upload to ....... "+STORAGE_SYSTEM);
		try {
			
			List<String> fileNames = new ArrayList<>();
		      Arrays.asList(files).stream().forEach(file -> {
		    	  String fileName = "";
		  		  String dirPath = "";
		    	  fileName = file.getOriginalFilename();
					String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
					logger.info("File Type Identified " + fileType);
					
					
					InputStream fileInputStream = null;
					
						try {
							fileInputStream = file.getInputStream();
						
					

					
						dirPath = SERVER_UPLOAD_LOCATION_FOLDER+File.separator+courseName;
						logger.info("Uploading path "+dirPath);
					
					// create dir if not there
					File dir = new File(dirPath);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					dirPath = dirPath + File.separator + file.getOriginalFilename();
					int fileNameLength = 0;
					fileNameLength = fileName.length();
					if (fileNameLength > 250) {
						JSONObject responseObject = new JSONObject();
						responseObject.put("status_code", HttpStatus.NOT_ACCEPTABLE);
						responseObject.put("status_message", "FileName length cannot exceed 250 characters ");
						logger.info("File name is exceeded ,its more than 250 characters ,please check");

						fileInputStream.close();
						
					}
		    	  
					if("local".equals(STORAGE_SYSTEM))
					{
					String filePath=saveFile(fileInputStream,dirPath);
					fileNames.add(filePath);
					}
					
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        
			
		      });
						
		      JSONObject responseObject = new JSONObject();
				responseObject.put("status_code", "200");
				responseObject.put("status_message", "Upload successfully");
				responseObject.put("result", fileNames);
				logger.info("*******************************************END OF UPLOAD************************");			
				return new ResponseEntity(responseObject.toString(), HttpStatus.OK);
			// check filename length, if exceeds 50 return invalid filename
			

			
			

		} catch (Exception e) {
			e.printStackTrace();
			JSONObject responseObject = new JSONObject();
			responseObject.put("status_code", HttpStatus.INTERNAL_SERVER_ERROR);
			responseObject.put("status_message", e.getMessage());
			logger.info("*******************************************END OF UPLOAD************************");

			return new ResponseEntity(responseObject.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {

		}
		
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
		return fileLocation;

	}
}
