package com.example.project;

public class Item {
    String id;
    String name;
    String description;

    String imageBase64;

    double latitude;
    double longitude;

    String locationDescription;

    String dateFound;

    public Item(){}

    public Item(String id, String name, String description, String imageBase64, double latitude, double longitude,String locationDescription, String dateFound) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationDescription = locationDescription;
        this.dateFound = dateFound;

    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageBase64() { return imageBase64; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocationDescription() { return locationDescription; }
    public String getDateFound() {
        return dateFound;
    }


    private String firebaseKey;

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
