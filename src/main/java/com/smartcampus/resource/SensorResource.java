package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isBlank()) {
            list = list.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        }
        return Response.ok(list).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "INVALID_INPUT");
            err.put("message", "Sensor ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (store.getSensors().containsKey(sensor.getId())) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "CONFLICT");
            err.put("message", "Sensor '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        if (sensor.getRoomId() != null && !sensor.getRoomId().isBlank()) {
            if (!store.getRooms().containsKey(sensor.getRoomId())) {
                throw new LinkedResourceNotFoundException(
                    "Room with ID '" + sensor.getRoomId() +
                    "' does not exist. Cannot register sensor without a valid room."
                );
            }
            store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        }
        store.getSensors().put(sensor.getId(), sensor);
        store.getSensorReadings().put(sensor.getId(), new ArrayList<>());
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Sensor registered successfully.");
        body.put("sensor", sensor);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "NOT_FOUND");
            err.put("message", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-resource locator — delegates to SensorReadingResource (Part 4)
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        if (!store.getSensors().containsKey(sensorId)) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "NOT_FOUND");
            err.put("message", "Sensor not found: " + sensorId);
            throw new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(err)
                    .build()
            );
        }
        return new SensorReadingResource(sensorId);
    }
}