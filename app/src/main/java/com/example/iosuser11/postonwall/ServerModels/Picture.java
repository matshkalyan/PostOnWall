package com.example.iosuser11.postonwall.ServerModels;

import com.google.gson.annotations.SerializedName;

/**
 * Created by iosuser9 on 18.08.16.
 */
public class Picture {
    @SerializedName("feature_vector")
    public String feature_vector;
    @SerializedName("photo_position")
    private String photo_position;
    @SerializedName("key_points")
    public String key_points;
    @SerializedName("x")
    public double x;
    @SerializedName("y")
    public double y;
    @SerializedName("width")
    public double width;
    @SerializedName("height")
    public double height;
    @SerializedName("direction_x")
    public float direction_x;
    @SerializedName("direction_y")
    public float direction_y;
    @SerializedName("direction_z")
    public float direction_z;
    @SerializedName("id")
    public long id;
    @SerializedName("_id")
    public String _id = null;
    @SerializedName("url")
    public String url;
    @SerializedName("loc")
    public Location loc;
    @SerializedName("response_code")
    private int responseCode;
    @SerializedName("coordinates")
    private String coordinates;
    @SerializedName("distance")
    private double distance = 20.0;  //in meters



    public Picture(String feature_vector, String photo_position, String key_points, double x, double y,
                   double width, double height, float direction_x, float direction_y,
                   float direction_z, long id,  String url, Location loc) {
        this.feature_vector = feature_vector;
        this.key_points = key_points;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.direction_x = direction_x;
        this.direction_y = direction_y;
        this.direction_z = direction_z;
        this.loc = loc;
        this.photo_position = photo_position;
        this.url = url;
        this.id = id;
    }


    //dummy constructor
    public Picture() {

    }


    public String getCoordinates() {
        coordinates = loc.getCoordinates();
        return coordinates;
    }

    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getFeature_vector() {
        return feature_vector;
    }

    public void setFeature_vector(String feature_vector) {
        this.feature_vector = feature_vector;
    }

    public String getKey_points() {
        return key_points;
    }

    public void setKey_points(String key_points) {
        this.key_points = key_points;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public float getDirection_x() {
        return direction_x;
    }

    public void setDirection_x(float direction_x) {
        this.direction_x = direction_x;
    }

    public float getDirection_y() {
        return direction_y;
    }

    public void setDirection_y(float direction_y) {
        this.direction_y = direction_y;
    }

    public float getDirection_z() {
        return direction_z;
    }

    public void setDirection_z(float direction_z) {
        this.direction_z = direction_z;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String get_Id() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


    public String getPhoto_position() {
        return photo_position;
    }

    public void setPhoto_position(String photo_position) {
        this.photo_position = photo_position;
    }
}
