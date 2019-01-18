package org.esncz.esnchallenge.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author chochy
 * Date: 2019-01-04
 */
public class ProfileData implements Serializable {
    private String firstname;
    private String lastname;
    private String gender;
    private String email;
    private University university = new University();

    @SerializedName("locations")
    private List<LocationPoint> checkedLocations;

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
