package com.shopme.admin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    String dirName = "user-photos";
	    Path userPhotosDir = Paths.get(dirName).toAbsolutePath();
	    String userPhotosPath = userPhotosDir.toString();
	    // Asegurar de que la ruta termine con '/' para evitar problemas en la resolución de la ruta.
	    if (!userPhotosPath.endsWith(File.separator)) {
	        userPhotosPath += File.separator;
	    }
	    // Usar 'file:///' para garantizar la compatibilidad entre sistemas operativos
	    String resourceLocation = "file:///" + userPhotosPath.replace("\\", "/");
	    registry.addResourceHandler("/" + dirName + "/**")
	            .addResourceLocations(resourceLocation);
	}
	
}
