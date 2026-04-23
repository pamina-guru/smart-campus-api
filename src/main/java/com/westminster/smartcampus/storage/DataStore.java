package com.westminster.smartcampus.storage;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralised, thread-safe in-memory store for the Smart Campus API.
 * A singleton is used because JAX-RS resource classes are instantiated
 * per-request by default — so shared state must live outside of them.
 *
 * ConcurrentHashMap is chosen to guarantee safe concurrent access
 * without coarse-grained synchronisation.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    // Readings indexed by sensorId → list of readings for that sensor
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getReadings() { return readings; }

    /** Seed a couple of sample rooms and sensors for demonstration. */
    private void seed() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room r2 = new Room("CAV-105", "Cavendish Lecture Hall", 120);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-002",  "CO2",         "ACTIVE", 410.0, "LIB-301");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());

        readings.put(s1.getId(), new ArrayList<>());
        readings.put(s2.getId(), new ArrayList<>());
    }
}