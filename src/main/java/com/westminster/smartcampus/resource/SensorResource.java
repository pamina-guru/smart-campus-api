package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import com.westminster.smartcampus.exception.ResourceNotFoundException;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.storage.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/sensors[?type=CO2]
     * If the "type" query parameter is supplied, the collection is filtered
     * by sensor type (case-insensitive).
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> results = new ArrayList<>();
        for (Sensor s : store.getSensors().values()) {
            if (type == null || type.isBlank()
                    || s.getType().equalsIgnoreCase(type)) {
                results.add(s);
            }
        }
        return Response.ok(results).build();
    }

    /** GET /api/v1/sensors/{sensorId} — fetch one sensor */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException(
                    "Sensor with id '" + sensorId + "' was not found.");
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. The supplied roomId MUST reference an existing Room,
     * otherwise a 422 Unprocessable Entity is returned via the mapper.
     */
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Sensor id is required.",
                            "status", 400))
                    .build();
        }
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of(
                            "error", "Sensor with id '" + sensor.getId() + "' already exists.",
                            "status", 409))
                    .build();
        }

        // Foreign-key validation against the Room collection
        String roomId = sensor.getRoomId();
        if (roomId == null || roomId.isBlank() || !store.getRooms().containsKey(roomId)) {
            throw new LinkedResourceNotFoundException(
                    "Referenced roomId '" + roomId + "' does not exist. Sensor cannot be registered.");
        }

        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);
        store.getReadings().put(sensor.getId(), new ArrayList<>());

        // Keep the parent Room in sync
        Room parent = store.getRooms().get(roomId);
        if (!parent.getSensorIds().contains(sensor.getId())) {
            parent.getSensorIds().add(sensor.getId());
        }

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Sensor registered successfully.");
        body.put("status", 201);
        body.put("sensor", sensor);

        return Response.created(location).entity(body).build();
    }

    /**
     * SUB-RESOURCE LOCATOR — /api/v1/sensors/{sensorId}/readings
     *
     * This method does NOT carry an HTTP-method annotation; JAX-RS recognises it
     * as a locator and delegates all matching sub-paths to the returned instance.
     * This isolates the reading-management logic inside its own resource class.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        if (!store.getSensors().containsKey(sensorId)) {
            throw new ResourceNotFoundException(
                    "Sensor with id '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}