package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> list = new ArrayList<>(store.getRooms().values());
        return Response.ok(list).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "INVALID_INPUT");
            err.put("message", "Room ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "CONFLICT");
            err.put("message", "Room '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        store.getRooms().put(room.getId(), room);
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Room created successfully.");
        body.put("room", room);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "NOT_FOUND");
            err.put("message", "Room not found: " + roomId);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "NOT_FOUND");
            err.put("message", "Room not found: " + roomId);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + "'. It still has " +
                room.getSensorIds().size() + " sensor(s) assigned to it."
            );
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build();   
    }
}