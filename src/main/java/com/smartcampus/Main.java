package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig()
                .packages(
                    "com.smartcampus.resource",
                    "com.smartcampus.exception.mapper",
                    "com.smartcampus.filter"
                );
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println("===========================================");
        System.out.println(" Smart Campus API started successfully!");
        System.out.println(" URL: " + BASE_URI);
        System.out.println("===========================================");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.stop();
    }
}