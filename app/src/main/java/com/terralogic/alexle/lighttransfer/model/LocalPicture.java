package com.terralogic.alexle.lighttransfer.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by alex.le on 23-Aug-17.
 */

public class LocalPicture implements Serializable {
    private String name;
    private Date takenDate;
    private String location;
    private boolean isSelected = false;

    public LocalPicture() {
    }

    public LocalPicture(String name, Date takenDate, String location) {
        this.name = name;
        this.takenDate = takenDate;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTakenDate() {
        return takenDate;
    }

    public void setTakenDate(Date takenDate) {
        this.takenDate = takenDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String toLocalDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(takenDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return new StringBuilder().append(day).append("/")
                .append(month + 1).append("/")
                .append(year).toString();
    }
}
