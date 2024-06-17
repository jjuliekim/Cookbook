package com.example.cookbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Recipe implements Parcelable {
    private String name;
    private String user;
    private ArrayList<String> ingredients;
    private ArrayList<String> steps;
    private ArrayList<String> favorited;
    private ArrayList<String> groups;

    public Recipe() {}

    public Recipe(String name, String user, ArrayList<String> ingredients, ArrayList<String> steps,
                  ArrayList<String> favorited, ArrayList<String> groups) {
        this.name = name;
        this.user = user;
        this.ingredients = ingredients;
        this.steps = steps;
        this.favorited = favorited;
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }

    public ArrayList<String> getFavorited() {
        return favorited;
    }

    public void setFavorited(ArrayList<String> favorited) {
        this.favorited = favorited;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    protected Recipe(Parcel in) {
        name = in.readString();
        user = in.readString();
        ingredients = in.createStringArrayList();
        steps = in.createStringArrayList();
        favorited = in.createStringArrayList();
        groups = in.createStringArrayList();
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(user);
        dest.writeStringList(ingredients);
        dest.writeStringList(steps);
        dest.writeStringList(favorited);
        dest.writeStringList(groups);
    }
}
