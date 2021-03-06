package org.esncz.esnchallenge.model;

import android.content.res.Resources;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
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
public class LocationPoint implements Serializable, Parcelable {
    private Long id;
    private String title;
    private String type;
    private Double lat;
    private Double lng;

    private int radius = 60;//meters
    private String description;
    private ColorRGBA color;
    private boolean checked = false;

    private transient Marker marker;
    private transient Circle circle;
    private transient Resources resources;

    public LocationPoint() {

    }

    public LocationPoint(Long id, String title, String type, Double lat, Double lng, String description, boolean checked) {
        this(id, title, type, lat, lng, description);
        this.checked = checked;
    }

    public LocationPoint(Long id, String title, String type, Double lat, Double lng, String description) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
    }


    protected LocationPoint(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        type = in.readString();
        if (in.readByte() == 0) {
            lat = null;
        } else {
            lat = in.readDouble();
        }
        if (in.readByte() == 0) {
            lng = null;
        } else {
            lng = in.readDouble();
        }
        radius = in.readInt();
        checked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(title);
        dest.writeString(type);
        if (lat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lat);
        }
        if (lng == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(lng);
        }
        dest.writeInt(radius);
        dest.writeByte((byte) (checked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationPoint> CREATOR = new Creator<LocationPoint>() {
        @Override
        public LocationPoint createFromParcel(Parcel in) {
            return new LocationPoint(in);
        }

        @Override
        public LocationPoint[] newArray(int size) {
            return new LocationPoint[size];
        }
    };

    public MarkerOptions buildMarkerOptions() {
        String snippet = type;
        if(description != null && !description.isEmpty()) {
            snippet += "\n\n" + description;
        }
        return new MarkerOptions().position(new LatLng(lat,lng)).title(title).snippet(snippet);
    }

    public int getFillColor(boolean checked) {
        if(!checked) {
            return color != null ? color.asColorInt() : ResourcesCompat.getColor(this.resources, R.color.orangeLightTransparentColor, null);
        } else {
            return ResourcesCompat.getColor(this.resources, R.color.colorAccentTransparent, null);
        }
    }

    public int getStrokeColor(boolean checked) {
        if(!checked) {
            return color != null ? color.asSolidColorInt() : ResourcesCompat.getColor(this.resources, R.color.orangeColor, null);
        } else {
            return color != null ? color.asSolidColorInt() : ResourcesCompat.getColor(this.resources, R.color.orangeColor, null);
        }
    }

    public CircleOptions buildCircleOptions(Resources resources) {
        this.resources = resources;

        return new CircleOptions()
                .center(new LatLng(lat,lng))
                .radius(this.radius)
                .fillColor(getFillColor(checked))
                .strokeColor(getStrokeColor(checked))
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
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

    public boolean check() {
        if (!this.checked) {
            this.checked = true;
            if (circle != null) {
                circle.setFillColor(getFillColor(this.checked));
                circle.setStrokeColor(getStrokeColor(this.checked));
            }
            return true;
        } else {
            return false;// Already checked response!
        }
    }

    public boolean uncheck() {
        if (this.checked) {
            this.checked = false;
            if (circle != null) {
                circle.setFillColor(getFillColor(this.checked));
                circle.setStrokeColor(getStrokeColor(this.checked));
            }
            return true;
        } else {
            return false;// Already checked response!
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

    public boolean isShownOnMap() {
        return getCircle() != null && getMarker() != null;
    }
}
