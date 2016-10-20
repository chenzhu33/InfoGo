package com.carelife.infogo.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by chenzhuwei on 16/10/19.
 */
@Table(name = "Positions")
public class Position extends Model {
    @Column(name = "lat")
    public double lat;

    @Column(name = "lon")
    public double lon;

    @Column(name = "label")
    public String label;

    @Column(name = "isHot")
    public boolean isHot = false;

    public boolean isHot() {
        return isHot;
    }

    public String getLabel() {
        return label;
    }

    public void setHot(boolean hot) {
        isHot = hot;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
