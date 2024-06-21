package com.example.cookbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {
    private ImageView imageView;
    private Recipe recipe;
    private String userId;
    private DatabaseReference recipeDatabase;
    private Button favoriteButton;
    private String username;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent myIntent = getIntent();
        recipe = myIntent.getParcelableExtra("recipe");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        recipeDatabase = FirebaseDatabase.getInstance().getReference("recipes");

        TextView nameText = findViewById(R.id.dishNameText);
        nameText.setText(recipe.getName());
        TextView authorText = findViewById(R.id.dishAuthorText);
        fetchUserName();
        authorText.setText(String.format("Uploaded by: %s", username));
        imageView = findViewById(R.id.recipeImage);
        Button shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> shareRecipe());
        favoriteButton = findViewById(R.id.favoriteButton);
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (recipe.getFavorited() != null && recipe.getFavorited().contains(userId)) {
            favoriteButton.setText("Remove from Favorites");
            favoriteButton.setOnClickListener(v -> removeFavorite());
        } else {
            favoriteButton.setText("Add to Favorites");
            favoriteButton.setOnClickListener(v -> addFavorite());
        }
    }

    // add user to favorite list
    private void addFavorite() {
        if (recipe.getFavorited() == null) {
            recipe.setFavorited(new ArrayList<>());
        }
        recipe.getFavorited().add(userId);
        recipeDatabase.child(recipe.getId()).setValue(recipe).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                Log.i("HERE DETAILS", "added to favorites");
                updateFavoriteButton();
            } else {
                Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                Log.i("HERE DETAILS", "failed to add to favs");
            }
        });
    }

    // remove user from favorite list
    private void removeFavorite() {
        ArrayList<String> favoritedBy = recipe.getFavorited();
        if (favoritedBy != null) {
            favoritedBy.remove(userId);
            recipe.setFavorited(favoritedBy);
            recipeDatabase.child(recipe.getId()).setValue(recipe).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    Log.i("HERE DETAILS", "removed from favs");
                    updateFavoriteButton();
                } else {
                    Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                    Log.i("HERE DETAILS", "failed to remove from favs");
                }
            });
        }
    }

    // get username from database
    private void fetchUserName() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username = user.getName();
                    Log.i("HERE NEW", "fetched username: " + user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE NEW", "getting username failed", error.toException());
            }
        });
    }

    private void shareRecipe() {

    }
}