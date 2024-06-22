package com.example.cookbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ExploreFragment extends Fragment {
    private FirebaseUser user;
    private RecipeAdapter recipeAdapter;
    private DatabaseReference recipeDatabase;
    private DatabaseReference userDatabase;
    private DatabaseReference groupDatabase;
    private ArrayList<Recipe> recipeList;
    private Set<String> usersGroups; // the user's groups
    private Set<String> groupUsers; // other group members

    public ExploreFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
        usersGroups = new HashSet<>();
        groupUsers = new HashSet<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        recipeDatabase = FirebaseDatabase.getInstance().getReference("recipes");
        userDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        groupDatabase = FirebaseDatabase.getInstance().getReference("groups");
        fetchUserGroups();
        // set recycler view
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>());
        RecyclerView recyclerView = view.findViewById(R.id.exploreRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);
        return view;
    }

    // get groups the user is in
    private void fetchUserGroups() {
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getGroups() != null) {
                    usersGroups.addAll(user.getGroups());
                    fetchGroupUsers();
                } else {
                    Log.i("HERE EXPLORE", "user data null or in no groups");
                    Toast.makeText(getContext(), "No Group Recipes to Display", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE EXPLORE", "fetch user groups e: " + error.getMessage());
            }
        });
    }

    // get users in the same group as the current user
    private void fetchGroupUsers() {
        for (String code : usersGroups) {
            groupDatabase.child(code).child("userIds").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        groupUsers.add(snapshot.getKey());
                    }
                    fetchRecipesFromGroups();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("HERE EXPLORE", "failed to get group users: " + error.getMessage());
                }
            });
        }
    }

    // get recipes created by group
    private void fetchRecipesFromGroups() {
        recipeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null && groupUsers.contains(recipe.getUser())) {
                        recipeList.add(recipe);
                    }
                }
                // sort to show favorites first
                recipeList.sort((r1, r2) -> {
                    boolean r1Favorited = r1.getFavorited() != null && r1.getFavorited().contains(user.getUid());
                    boolean r2Favorited = r2.getFavorited() != null && r2.getFavorited().contains(user.getUid());
                    return Boolean.compare(r2Favorited, r1Favorited);
                });
                recipeAdapter.updateRecipes(recipeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE EXPLORE", "fetch recipes e: " + error.getMessage());
            }
        });
    }


}