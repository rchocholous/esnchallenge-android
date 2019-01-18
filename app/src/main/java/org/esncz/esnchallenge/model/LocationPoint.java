package org.esncz.esnchallenge.model;

import android.content.res.Resources;
import android.location.Location;
import android.support.v4.content.res.ResourcesCompat;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

import org.esncz.esnchallenge.R;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class LocationPoint implements Serializable {
    private Long id;
    private String title;
    private String type;
    private Double lat;
    private Double lng;

    private int radius = 100;//meters
    private boolean checked = false;

    private Marker marker;
    private Circle circle;
    private Resources resources;

    public LocationPoint() {

    }

    public LocationPoint(Long id, String title, String type, Double lat, Double lng, boolean checked) {
        this(id, title, type, lat, lng);
        this.checked = checked;
    }

    public LocationPoint(Long id, String title, String type, Double lat, Double lng) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
    }


    public MarkerOptions buildMarkerOptions() {
        return new MarkerOptions().position(new LatLng(lat,lng)).title(title).snippet(type);
    }

    public CircleOptions buildCircleOptions(Resources resources) {
        this.resources = resources;

        int fillColor = ResourcesCompat.getColor(this.resources, R.color.orangeLightTransparentColor, null);
        if(checked) {
            fillColor = ResourcesCompat.getColor(this.resources, R.color.colorAccentTransparent, null);
        }
        return new CircleOptions()
                .center(new LatLng(lat,lng))
                .radius(this.radius)
                .fillColor(fillColor)
                .strokeColor(ResourcesCompat.getColor(this.resources, R.color.orangeColor, null))
                .strokeWidth(10);
    }

    public float distanceTo(LocationPoint location) {
        return distanceTo(location.getLocation());
    }

    public float distanceTo(Location location) {
        return getLocation().distanceTo(location);
    }

    public Location getLocation() {
        Location location = new Location(title);
        location.setLatitude(lat);
        location.setLongitude(lng);
        return location;
    }

    public boolean isInsideRadius(Location location) {
        return location.distanceTo(getLocation()) <= radius;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public void check() {
        this.checked = true;
        if(circle != null) {
            circle.setFillColor(ResourcesCompat.getColor(this.resources, R.color.colorAccentTransparent, null));
        }
    }

    @Override
    public String toString() {
        return "LocationPoint{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", radius=" + radius +
                ", checked=" + checked +
                '}';
    }
}
