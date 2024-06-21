package com.example.cookbook;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final ArrayList<Recipe> recipeList;
    private final Context context;
    private String userId;
    private FirebaseUser user;
    private String username;

    public RecipeAdapter(Context context, ArrayList<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        if (recipe != null) {
            holder.nameText.setText(recipe.getName());
            holder.authorText.setText(String.format("Created by: %s", username));
            holder.numberStepsText.setText(String.format("# Steps: %d", recipe.getSteps().size()));
            if (user != null) {
                ArrayList<String> favorites = recipe.getFavorited();
                if (favorites == null) {
                    favorites = new ArrayList<>();
                }
                if (favorites.contains(userId)) {
                    holder.heartImage.setImageResource(R.drawable.heart_colored);
                } else {
                    holder.heartImage.setImageResource(R.drawable.heart);
                }
            }
            holder.itemView.setOnClickListener(v -> {
                Intent nextIntent = new Intent(context, DetailsActivity.class);
                nextIntent.putExtra("recipe", recipe);
                context.startActivity(nextIntent);
            });
        } else {
            Log.i("HERE ADAPTER", "Recipe at position " + position + " is null.");
        }
    }

    public void setUsername(String username) {
        this.username = username;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // update list
    public void updateRecipes(ArrayList<Recipe> updatedList) {
        if (updatedList != null) {
            recipeList.clear();
            recipeList.addAll(updatedList);
            notifyDataSetChanged();
        } else {
            Toast.makeText(context, "No Recipes To Display", Toast.LENGTH_SHORT).show();
        }
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView authorText;
        TextView numberStepsText;
        ImageView heartImage;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.recipeNameText);
            authorText = itemView.findViewById(R.id.recipeAuthorText);
            numberStepsText = itemView.findViewById(R.id.recipeStepsNumberText);
            heartImage = itemView.findViewById(R.id.imageView);
        }
    }

}
