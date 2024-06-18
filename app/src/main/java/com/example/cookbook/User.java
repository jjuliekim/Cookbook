package com.example.cookbook;

import java.util.ArrayList;

public class User {
    private String uid;
    private String name;
    private ArrayList<String> groups;

    public User() {}

    public User(String uid, String name, ArrayList<String> groups) {
        this.uid = uid;
        this.name = name;
        this.groups = groups;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }
}
