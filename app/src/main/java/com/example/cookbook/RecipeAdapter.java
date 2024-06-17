package com.example.cookbook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final ArrayList<Recipe> recipeList;
    private final Context context;

    public RecipeAdapter(Context context, ArrayList<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeAdapter.RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.nameText.setText(recipe.getName());
        holder.authorText.setText(String.format("Created by: %s", recipe.getUser()));
        holder.numberStepsText.setText(String.format("# Steps: %d", recipe.getSteps().size()));

        // if in favorites, set image to filled in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ArrayList<String> favorites = recipe.getFavorited();
        if (favorites != null && favorites.contains(user.getUid())) {
            holder.heartImage.setImageResource(R.drawable.heart);
        } else {
            holder.heartImage.setImageResource(R.drawable.heart_colored);
        }

        /*holder.itemView.setOnClickListener(v -> {
            Intent nextIntent = new Intent(context, DetailsActivity.class);
            nextIntent.putExtra("recipe", recipe);
            context.startActivity(nextIntent);
        });*/
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // update list
    public void updateRecipes(ArrayList<Recipe> updatedList) {
        recipeList.clear();
        recipeList.addAll(updatedList);
        notifyDataSetChanged();
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
            heartImage - itemView.findViewById(R.id.imageView);
        }
    }

}
