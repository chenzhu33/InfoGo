package com.carelife.infogo.dom;

/**
 * Created by chenzhuwei on 16/10/19.
 */

public class BaseInfo {
    private int id;
    private String name;

    public BaseInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
