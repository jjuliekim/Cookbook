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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FirebaseUser user;
    private RecipeAdapter recipeAdapter;
    private DatabaseReference recipeDatabase;
    private ArrayList<Recipe> recipeList;
    private String userName;
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
                    userName = user.getName();
                    Log.i("HERE NEW", "fetched username: " + userName);
                    fetchRecipesFromUser();
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
                try {
                    Log.i("HERE HOME", "username: " + userName);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Recipe recipe = snapshot.getValue(Recipe.class);
                        Log.i("HERE HOME", "recipe name: " + recipe.getUser());
                        if (recipe.getUser().equals(userName)) {
                            recipeList.add(recipe);
                        }
                    }
                    recipeAdapter.updateRecipes(recipeList);
                    Log.i("HERE HOME", "updated list and loaded " + recipeList.size());
                } catch (Exception e) {
                    Log.i("HERE HOME", "fetching e: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE HOME", "failed to load recipes");
                Toast.makeText(getContext(), "failed to load recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

}