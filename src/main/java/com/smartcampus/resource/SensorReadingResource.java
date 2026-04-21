package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings =
            store.getSensorReadings().getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading incoming) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "NOT_FOUND");
            err.put("message", "Sensor not found: " + sensorId);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is under MAINTENANCE " +
                "and cannot accept new readings."
            );
        }
        SensorReading reading = new SensorReading(incoming.getValue());
        store.getSensorReadings()
             .computeIfAbsent(sensorId, k -> new ArrayList<>())
             .add(reading);
        // Side effect: update sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Reading recorded successfully.");
        body.put("reading", reading);
        body.put("sensorCurrentValue", sensor.getCurrentValue());
        return Response.status(Response.Status.CREATED).entity(body).build();
    }
}