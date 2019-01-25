package org.esncz.esnchallenge.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author chochy
 * Date: 2019-01-04
 */
public class ProfileData implements Serializable, Parcelable {
    private String firstname;
    private String lastname;
    private String gender;
    private String email;
    private University university = new University();

    @SerializedName("locations")
    private List<LocationPoint> checkedLocations;

    protected ProfileData(Parcel in) {
        firstname = in.readString();
        lastname = in.readString();
        gender = in.readString();
        email = in.readString();

        if(in.readByte() == 0) {
            university.setName(in.readString());
        }

        if(in.readByte() == 0) {
            university.setSectionShort(in.readString());
        }

        if(in.readByte() == 0) {
            university.setSectionLong(in.readString());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(gender);
        dest.writeString(email);

        if (university.getName() == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(university.getName());
        }

        if (university.getSectionShort() == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(university.getSectionShort());
        }

        if (university.getSectionLong() == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(university.getSectionLong());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProfileData> CREATOR = new Creator<ProfileData>() {
        @Override
        public ProfileData createFromParcel(Parcel in) {
            return new ProfileData(in);
        }

        @Override
        public ProfileData[] newArray(int size) {
            return new ProfileData[size];
        }
    };

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public List<LocationPoint> getCheckedLocations() {
        return checkedLocations;
    }

    public void setCheckedLocations(List<LocationPoint> checkedLocations) {
        this.checkedLocations = checkedLocations;
    }

    @Override
    public String toString() {
        return "ProfileData{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", university=" + university +
                ", checkedLocations=" + checkedLocations +
                '}';
    }
}
