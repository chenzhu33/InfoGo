package com.carelife.infogo.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by chenzhuwei on 16/10/19.
 */

@Table(name = "Photos")
public class Photo extends Model {

    @Column(name = "url")
    public String url;

    @Column(name = "position")
    public Position position;

    @Column(name = "timestamp")
    public long timestamp;

    @Column(name = "desc")
    public String description;

    public String getUrl() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Position getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
