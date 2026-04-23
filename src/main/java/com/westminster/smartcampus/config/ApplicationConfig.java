package com.westminster.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        packages(
                "com.westminster.smartcampus.resource",
                "com.westminster.smartcampus.mapper",
                "com.westminster.smartcampus.filter"
        );
    }
}