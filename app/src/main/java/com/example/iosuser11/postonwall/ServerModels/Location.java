package com.example.iosuser11.postonwall.ServerModels;

import com.google.gson.annotations.SerializedName;

/**
 * Created by iosuser9 on 18.08.16.
 */
public class Location {
    @SerializedName("type")
    private String type = "Point";
    @SerializedName("coordinates")
    private double[] coordinates = new double[2];
    private String s;

    public Location(double longitude, double latutude) {
        coordinates[0] = longitude;
        coordinates[1] = latutude;
    }

    public Location() {

    }

    public String getCoordinates() {
        String s = coordinates[0] + "," + coordinates[1];
        return s;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

}
