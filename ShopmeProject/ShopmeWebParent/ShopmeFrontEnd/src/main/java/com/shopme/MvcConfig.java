package com.shopme;

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
	    registerUserImageResource(registry,  "user-photos");
	    registerUserImageResource(registry,  "category-images");
	    registerUserImageResource(registry,  "brand-logos");
	}

	private void registerUserImageResource(ResourceHandlerRegistry registry, String dirName ) {
		
	    Path dir = Paths.get(dirName).toAbsolutePath();
	    String path = dir.toString();
	    // Asegurar de que la ruta termine con '/' para evitar problemas en la resolución de la ruta.
	    if (!path.endsWith(File.separator)) {
	    	path += File.separator;
	    }
	    // Usar 'file:///' para garantizar la compatibilidad entre sistemas operativos
	    String resourceLocation = "file:///" + path.replace("\\", "/");
	    registry.addResourceHandler("/" + dirName + "/**")
	            .addResourceLocations(resourceLocation);
	}
}
