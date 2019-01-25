package org.esncz.esnchallenge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author chochy
 * Date: 2019-01-08
 */
public class HighScore implements Serializable, Parcelable {
    private String signature;
    private String name;
    private String country;
    private String section;
    private Integer point;
    private Integer rank;

    protected HighScore(Parcel in) {
        signature = in.readString();
        name = in.readString();
        country = in.readString();
        section = in.readString();
        if (in.readByte() == 0) {
            point = null;
        } else {
            point = in.readInt();
        }
        if (in.readByte() == 0) {
            rank = null;
        } else {
            rank = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(signature);
        dest.writeString(name);
        dest.writeString(country);
        dest.writeString(section);
        if (point == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(point);
        }
        if (rank == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(rank);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HighScore> CREATOR = new Creator<HighScore>() {
        @Override
        public HighScore createFromParcel(Parcel in) {
            return new HighScore(in);
        }

        @Override
        public HighScore[] newArray(int size) {
            return new HighScore[size];
        }
    };

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "HighScore{" +
                "signature='" + signature + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", section='" + section + '\'' +
                ", point=" + point +
                ", rank=" + rank +
                '}';
    }
}
