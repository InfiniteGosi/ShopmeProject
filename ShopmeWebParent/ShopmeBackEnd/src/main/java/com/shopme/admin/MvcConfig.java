package com.shopme.admin;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * The MvcConfig class is a Spring @Configuration class that implements the WebMvcConfigurer interface. 
 * Its main purpose is to configure the mapping of static resources (like images, CSS, or JavaScript files)
 *  to make them accessible via HTTP requests.
 * */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// dirName is set to "user-photos". This is the folder where user profile photos are stored.
		String dirName = "user-photos";
		
		// Converts the folder name into a Path object
		Path userPhotoDir = Paths.get(dirName);
		
		// Resolves the absolute path of the directory
		String userPhotoPath = userPhotoDir.toFile().getAbsolutePath();
		
		registry.addResourceHandler("/" + dirName + "/**")
			.addResourceLocations("file:/" + userPhotoPath + "/");
	}
	
}
