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
                Log.i("HERE HOME", "failed to load recipes");
                Toast.makeText(getContext(), "failed to load recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

}