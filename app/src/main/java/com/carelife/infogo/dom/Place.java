package com.carelife.infogo.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by chenzhuwei on 16/10/19.
 */

@Table(name = "Places")
public class Place extends Model {

    @Column(name = "name")
    private String name;

    @Column(name = "desc")
    private String description;

    @Column(name = "position")
    private Position position;

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
