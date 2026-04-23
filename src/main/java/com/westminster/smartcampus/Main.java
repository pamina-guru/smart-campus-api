package com.westminster.smartcampus;

import com.westminster.smartcampus.config.ApplicationConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://localhost:8090/";

    public static HttpServer startServer() {
        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), new ApplicationConfig());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpServer server = startServer();
        LOGGER.info("=========================================================");
        LOGGER.info("Smart Campus API started at " + BASE_URI);
        LOGGER.info("Press Ctrl+C to stop the server.");
        LOGGER.info("=========================================================");
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        Thread.currentThread().join();
    }
}