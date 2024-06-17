package com.example.cookbook;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewFragment extends Fragment {
    private EditText recipeInputText;
    private TextView searchRecipeText;
    private LinearLayout ingredientsLayout;
    private LinearLayout stepsLayout;
    private EditText firstIngredientText;
    private EditText firstStepText;

    public NewFragment() {}

    public static NewFragment newInstance() {
        return new NewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);
        recipeInputText = view.findViewById(R.id.recipeInputNameText);
        searchRecipeText = view.findViewById(R.id.searchRecipeText);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button addIngredientButton = view.findViewById(R.id.addIngredientButton);
        Button addStepButton = view.findViewById(R.id.addStepButton);
        ingredientsLayout = view.findViewById(R.id.ingredientsLayout);
        stepsLayout = view.findViewById(R.id.stepsLayout);
        firstIngredientText = view.findViewById(R.id.firstIngredientText);
        firstStepText = view.findViewById(R.id.firstStepText);
        // actions
        searchRecipeText.setOnClickListener(v -> searchRecipe());
        saveButton.setOnClickListener(v -> saveRecipe());
        addIngredientButton.setOnClickListener(v -> addIngredient());
        addStepButton.setOnClickListener(v -> addStep());
        return view;
    }

    // search recipe info on API
    private void searchRecipe() {
        String recipeName = recipeInputText.getText().toString();
        if (recipeName.isEmpty()) {
            Toast.makeText(getContext(), "Input Recipe Name", Toast.LENGTH_SHORT).show();
            return;
        }
        // search recipe API

    }

    // save recipe to database
    private void saveRecipe() {

    }

    // add ingredient input line
    private void addIngredient() {
        if (!firstIngredientText.getText().toString().isEmpty()) {
            EditText ingredientField = new EditText(getContext());
            ingredientField.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 48));
            ingredientField.setHint("Enter Ingredient");
            ingredientsLayout.addView(ingredientField);
        }
    }

    // add step input line
    private void addStep() {
        if (!firstStepText.getText().toString().isEmpty()) {
            EditText stepField = new EditText(getContext());
            stepField.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 48));
            stepField.setHint("Enter Step");
            stepsLayout.addView(stepField);
        }
    }


}