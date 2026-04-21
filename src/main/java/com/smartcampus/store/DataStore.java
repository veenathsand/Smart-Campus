package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        // Seed sample rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab", 30);
        Room r3 = new Room("HALL-A", "Main Hall", 200);
        Room r4 = new Room("EMPTY-01", "Empty Room", 10); // for DELETE demo
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);
        rooms.put(r4.getId(), r4);

        // Seed sample sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",   22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",  400.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "LAB-101");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "OFFLINE", 18.0, "HALL-A");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        // Link sensors to rooms
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-001");
        r2.getSensorIds().add("OCC-001");
        r3.getSensorIds().add("TEMP-002");

        // Initialise reading lists
        sensorReadings.put("TEMP-001", new ArrayList<>());
        sensorReadings.put("CO2-001",  new ArrayList<>());
        sensorReadings.put("OCC-001",  new ArrayList<>());
        sensorReadings.put("TEMP-002", new ArrayList<>());
    }

    public static DataStore getInstance() { return INSTANCE; }

    public Map<String, Room> getRooms()   { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getSensorReadings() { return sensorReadings; }
}