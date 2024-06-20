package com.example.cookbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Recipe implements Parcelable {
    private String id;
    private String name;
    private String user;
    private ArrayList<String> ingredients;
    private ArrayList<String> steps;
    private ArrayList<String> favorited;

    public Recipe() {}

    public Recipe(String id, String name, String user, ArrayList<String> ingredients, ArrayList<String> steps, ArrayList<String> favorited) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.ingredients = ingredients;
        this.steps = steps;
        this.favorited = favorited;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    protected Recipe(Parcel in) {
        id = in.readString();
        name = in.readString();
        user = in.readString();
        ingredients = in.createStringArrayList();
        steps = in.createStringArrayList();
        favorited = in.createStringArrayList();
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(user);
        dest.writeStringList(ingredients);
        dest.writeStringList(steps);
        dest.writeStringList(favorited);
    }
}
