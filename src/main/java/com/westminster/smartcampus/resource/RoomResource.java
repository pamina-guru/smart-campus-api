package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.ResourceNotFoundException;
import com.westminster.smartcampus.exception.RoomNotEmptyException;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.storage.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    /** GET /api/v1/rooms — list all rooms */
    @GET
    public Response getAllRooms() {
        Collection<Room> all = store.getRooms().values();
        return Response.ok(all).build();
    }

    /** GET /api/v1/rooms/{roomId} — get one room by ID */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + roomId + "' was not found.");
        }
        return Response.ok(room).build();
    }

    /** POST /api/v1/rooms — create a new room */
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Room id is required.",
                            "status", 400))
                    .build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of(
                            "error", "Room with id '" + room.getId() + "' already exists.",
                            "status", 409))
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new java.util.ArrayList<>());
        }
        store.getRooms().put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Room created successfully.");
        body.put("status", 201);
        body.put("room", room);

        return Response.created(location).entity(body).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Business rule: a room cannot be deleted while it still owns sensors.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            // Idempotent behaviour: second delete of same resource still returns 404
            throw new ResourceNotFoundException(
                    "Room with id '" + roomId + "' was not found.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' still has " + room.getSensorIds().size()
                            + " active sensor(s) assigned. Remove sensors before deletion.");
        }

        store.getRooms().remove(roomId);
        return Response.noContent().build(); // 204
    }
}