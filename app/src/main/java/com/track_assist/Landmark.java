package com.track_assist;

public class Landmark {

    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private long timestamp;

    public Landmark() {
    }

    public Landmark(String name, String address, double latitude, double longitude, long timestamp) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
