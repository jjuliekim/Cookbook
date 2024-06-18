package com.example.cookbook;

import java.util.ArrayList;

public class User {
    private String uid;
    private ArrayList<String> groups;

    public User() {}

    public User(String uid, ArrayList<String> groups) {
        this.uid = uid;
        this.groups = groups;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }
}
