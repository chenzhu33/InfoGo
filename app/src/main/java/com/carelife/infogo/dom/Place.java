package com.carelife.infogo.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by chenzhuwei on 16/10/19.
 */

@Table(name = "Places")
public class Place extends Model {

    @Column(name = "name", index = true)
    public String name;

    @Column(name = "desc")
    public String description;

    @Column(name = "position")
    public Position position;

    @Column(name = "address")
    public String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Position getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

}
