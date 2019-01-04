package cz.chochy.esnchallenge.model;

import java.io.Serializable;

/**
 * @author chochy
 * Date: 2019-01-04
 */
public class University implements Serializable {
    private String name;
    private String sectionShort;

    public University() {

    }

    public University(String name, String sectionShort) {
        this.name = name;
        this.sectionShort = sectionShort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSectionShort() {
        return sectionShort;
    }

    public void setSectionShort(String sectionShort) {
        this.sectionShort = sectionShort;
    }

    @Override
    public String toString() {
        return "University{" +
                "name='" + name + '\'' +
                ", sectionShort='" + sectionShort + '\'' +
                '}';
    }
}
