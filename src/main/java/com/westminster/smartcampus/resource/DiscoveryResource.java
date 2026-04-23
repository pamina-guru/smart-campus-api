package com.westminster.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Smart Campus Sensor & Room Management API");
        body.put("version", "1.0.0");
        body.put("description",
                "RESTful API for managing rooms, sensors and sensor readings on campus.");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team", "Smart Campus Backend Team");
        contact.put("email", "smartcampus@westminster.ac.uk");
        contact.put("module", "5COSC022W Client-Server Architectures");
        body.put("contact", contact);

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensorsByType", "/api/v1/sensors?type={type}");
        links.put("sensorReadings", "/api/v1/sensors/{sensorId}/readings");
        body.put("_links", links);

        return Response.ok(body).build();
    }
     @Path("/crash")
    @GET
    public Response crashTest() {
        throw new RuntimeException("Simulated server crash");
    }
}