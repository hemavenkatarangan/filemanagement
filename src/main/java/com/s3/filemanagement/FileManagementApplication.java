package com.s3.filemanagement;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2

@RestController
@ComponentScan("com.s3")

public class FileManagementApplication {
	
	

	public static void main(String[] args) {
		  SpringApplication.run(FileManagementApplication.class, args);
	   }

}
