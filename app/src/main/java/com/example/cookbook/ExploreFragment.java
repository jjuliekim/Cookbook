package com.example.cookbook;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExploreFragment extends Fragment {
    private FirebaseUser user;
    private RecipeAdapter recipeAdapter;
    private DatabaseReference recipeDatabase;
    private DatabaseReference userDatabase;
    private ArrayList<Recipe> recipeList;
    private String username;
    private ArrayList<String> userGroups;

    public ExploreFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        recipeDatabase = FirebaseDatabase.getInstance().getReference("recipes");
        userDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        fetchUserName();
        fetchRecipesFromGroups();
        // set recycler view
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>());
        RecyclerView recyclerView = view.findViewById(R.id.exploreRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);
        return view;
    }

    // get current user's username
    private void fetchUserName() {
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username = user.getName();
                    userGroups = user.getGroups() != null ? user.getGroups() : new ArrayList<>();
                    Log.i("HERE EXPLORE", "fetched username: " + username);
                    Log.i("HERE EXPLORE", "fetched groups: " + userGroups);
                } else {
                    Log.i("HERE EXPLORE", "User data is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE EXPLORE", "getting username failed", error.toException());
            }
        });
    }

    // get recipes created by group
    private void fetchRecipesFromGroups() {
        recipeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        // add if created by user or user's group
                        if (recipe.getUser().equals(user.getUid()) || userGroups.contains(recipe.getUser())) {
                            recipeList.add(recipe);
                        }
                    }
                }
                recipeAdapter.updateRecipes(recipeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE EXPLORE", "fetch recipes e: " + error.getMessage());
            }
        });
    }
}