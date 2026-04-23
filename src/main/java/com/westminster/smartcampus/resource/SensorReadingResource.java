package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.ResourceNotFoundException;
import com.westminster.smartcampus.exception.SensorUnavailableException;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.storage.DataStore;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final DataStore store = DataStore.getInstance();
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException(
                    "Sensor with id '" + sensorId + "' was not found."
            );
        }

        List<SensorReading> history =
                store.getReadings().getOrDefault(sensorId, new ArrayList<>());

        return Response.ok(history).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException(
                    "Sensor with id '" + sensorId + "' was not found."
            );
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())
                || "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently "
                            + sensor.getStatus()
                            + " and cannot accept new readings."
            );
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Reading body is required.",
                            "status", 400
                    ))
                    .build();
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.getReadings()
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Reading recorded successfully.");
        body.put("status", 201);
        body.put("reading", reading);
        body.put("sensorCurrentValue", sensor.getCurrentValue());

        return Response.status(Response.Status.CREATED)
                .entity(body)
                .build();
    }
}