package com.example.cookbook;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewFragment extends Fragment {
    private EditText recipeInputText;
    private TextView searchRecipeText;

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
        searchRecipeText.setOnClickListener(v -> searchRecipe());
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveRecipe());
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

}