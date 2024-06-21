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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {
    private FirebaseUser user;
    private RecipeAdapter recipeAdapter;
    private DatabaseReference recipeDatabase;
    private ArrayList<Recipe> recipeList;
    private DatabaseReference userReference;

    public HomeFragment() {}

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // set recycler view
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);

        recipeDatabase = FirebaseDatabase.getInstance().getReference("recipes");
        userReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        fetchUserName();

        return view;
    }

    // get username from database
    private void fetchUserName() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String username = user.getName();
                    fetchRecipesFromUser();
                    recipeAdapter.setUsername(username);
                } else {
                    Log.i("HERE NEW", "User data is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE NEW", "getting username failed", error.toException());
            }
        });
    }

    // fetch from db recipes created by user
    private void fetchRecipesFromUser() {
        recipeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null && user.getUid().equals(recipe.getUser())) {
                        recipeList.add(recipe);
                    }
                }
                sortRecipes();
                recipeAdapter.updateRecipes(recipeList);
                Log.i("HERE HOME", "updated list and loaded " + recipeList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE HOME", "failed to load recipes");
                Toast.makeText(getContext(), "failed to load recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // sort the recipes in the recycler view
    public void sortRecipes() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("page_prefs", Context.MODE_PRIVATE);
        int sortOption = sharedPreferences.getInt("sort_option", 0);
        switch (sortOption) {
            case 0: // alphabetical
                recipeList.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
                break;
            case 1: // increasing number of steps
                recipeList.sort(Comparator.comparingInt(r -> r.getSteps().size()));
                break;
            case 2: // favorites at the top
                recipeList.sort((r1, r2) -> {
                    boolean r1Favorite = r1.getFavorited() != null && r1.getFavorited().contains(user.getUid());
                    boolean r2Favorite = r2.getFavorited() != null && r2.getFavorited().contains(user.getUid());
                    return Boolean.compare(r2Favorite, r1Favorite);
                });
                break;
        }
    }


}