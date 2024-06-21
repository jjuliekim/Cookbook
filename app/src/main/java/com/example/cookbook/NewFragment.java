package com.example.cookbook;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NewFragment extends Fragment {
    private EditText recipeInputText;
    private LinearLayout ingredientsLayout;
    private LinearLayout stepsLayout;
    private EditText firstIngredientText;
    private EditText firstStepText;
    private DatabaseReference databaseReference;
    private String userId;
    private Uri imageUri;
    private String imageURL;

    public NewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);
        recipeInputText = view.findViewById(R.id.recipeInputNameText);
        TextView searchRecipeText = view.findViewById(R.id.searchRecipeText);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button addIngredientButton = view.findViewById(R.id.addIngredientButton);
        Button addStepButton = view.findViewById(R.id.addStepButton);
        Button attachPhoto = view.findViewById(R.id.uploadPhotoButton);
        ingredientsLayout = view.findViewById(R.id.ingredientsLayout);
        stepsLayout = view.findViewById(R.id.stepsLayout);
        firstIngredientText = view.findViewById(R.id.firstIngredientText);
        firstStepText = view.findViewById(R.id.firstStepText);
        // actions
        searchRecipeText.setOnClickListener(v -> searchRecipe());
        saveButton.setOnClickListener(v -> saveRecipe());
        addIngredientButton.setOnClickListener(v -> addIngredient());
        addStepButton.setOnClickListener(v -> addStep());
        attachPhoto.setOnClickListener(v -> chooseFile());
        return view;
    }

    // open intent to choose image
    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Toast.makeText(getContext(), "Image Selected", Toast.LENGTH_SHORT).show();
            Log.i("HERE NEW", "image selected " + imageUri);
        }
    }

    // search recipe info on API
    private void searchRecipe() {
        String recipeName = recipeInputText.getText().toString();
        if (recipeName.isEmpty()) {
            Toast.makeText(getContext(), "Input Recipe Name", Toast.LENGTH_SHORT).show();
            return;
        }
        // search recipe API
        String searchURL = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + recipeName;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadDataTask().execute(searchURL);
        } else {
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }

    // save recipe to database
    private void saveRecipe() {
        String recipeName = recipeInputText.getText().toString();
        ArrayList<String> ingredients = new ArrayList<>();
        ArrayList<String> steps = new ArrayList<>();
        if (recipeName.isEmpty()) {
            Toast.makeText(getContext(), "Enter Recipe Name", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < ingredientsLayout.getChildCount(); i++) {
            EditText field = (EditText) ingredientsLayout.getChildAt(i);
            String ingredient = field.getText().toString();
            if (!ingredient.isEmpty() && !ingredient.equals("null")) {
                ingredients.add(ingredient);
            } else {
                Toast.makeText(getContext(), "Enter Ingredients", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        for (int i = 0; i < stepsLayout.getChildCount(); i++) {
            EditText field = (EditText) stepsLayout.getChildAt(i);
            String step = field.getText().toString();
            if (!step.isEmpty() && !step.equals("null")) {
                steps.add(step);
            } else {
                Toast.makeText(getContext(), "Enter Steps", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String recipeId = databaseReference.push().getKey();
        if (imageUri != null) {
            uploadImage(imageUri, recipeId);
        } else {
            imageURL = "";
        }
        Recipe recipe = new Recipe(recipeId, recipeName, userId, ingredients, steps, new ArrayList<>(), imageURL);
        databaseReference.child(recipeId).setValue(recipe).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("HERE NEW", "recipe saved");
                Toast.makeText(getContext(), "Recipe Saved", Toast.LENGTH_SHORT).show();
                Intent nextIntent = new Intent(getActivity(), MainActivity.class);
                nextIntent.putExtra("recipe", recipe);
                startActivity(nextIntent);
            } else {
                Log.i("HERE NEW", "recipe failed to add");
                Toast.makeText(getContext(), "Failed to Add Recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // add ingredient input line
    private void addIngredient() {
        if (!firstIngredientText.getText().toString().isEmpty()) {
            EditText ingredientField = new EditText(getContext());
            ingredientField.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            ingredientField.setHint("Enter Ingredient");
            ingredientsLayout.addView(ingredientField);
        }
    }

    // add step input line
    private void addStep() {
        if (!firstStepText.getText().toString().isEmpty()) {
            EditText stepField = new EditText(getContext());
            stepField.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            stepField.setHint("Enter Step");
            stepsLayout.addView(stepField);
        }
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Log.i("HERE NEW", "error retrieving");
                return;
            }
            try {
                // parse json details
                JSONObject jsonObject = new JSONObject(response);
                JSONArray mealsArray = jsonObject.getJSONArray("meals");
                if (mealsArray.length() > 0) {
                    JSONObject meal = mealsArray.getJSONObject(0);
                    String instructions = meal.getString("strInstructions");
                    String[] stepsArray = instructions.split("\r\n");
                    ArrayList<String> stepsList = new ArrayList<>();
                    for (String step : stepsArray) {
                        if (step != null && !step.isEmpty()) {
                            stepsList.add(step);
                        }
                    }
                    ArrayList<String> ingredients = new ArrayList<>();
                    for (int i = 1; i < 21; i++) {
                        String ingredient = meal.getString("strIngredient" + i);
                        if (!ingredient.isEmpty()) {
                            ingredients.add(ingredient);
                        }
                    }
                    String recipeId = databaseReference.push().getKey();
                    Recipe recipe = new Recipe(recipeId, recipeInputText.getText().toString(), userId,
                            ingredients, stepsList, new ArrayList<>(), "");
                    databaseReference.child(recipeId).setValue(recipe).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i("HERE NEW", "recipe saved");
                            Toast.makeText(getContext(), "Recipe Saved", Toast.LENGTH_SHORT).show();
                            Intent nextIntent = new Intent(getActivity(), DetailsActivity.class);
                            nextIntent.putExtra("recipe", recipe);
                            startActivity(nextIntent);
                            recipeInputText.setText("");
                        } else {
                            Log.i("HERE NEW", "recipe failed to add");
                            Toast.makeText(getContext(), "Failed to Add Recipe", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "No Recipe Found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.i("HERE NEW", "error fetching: " + e.getMessage());
                Toast.makeText(getContext(), "No Recipe Found", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                reader.close();
                return builder.toString();
            } catch (Exception e) {
                Log.i("HERE NEW DOWNLOAD DATA TASK", "error: " + e.getMessage());
                return null;
            }
        }
    }

    // take photo
    private void takePhoto() {
        // save to some storage

    }

    // upload image to storage
    private void uploadImage(Uri imageUri, String recipeId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("recipes/" + recipeId + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageURL = uri.toString();
                    databaseReference.child(recipeId).child("imageURL").setValue(imageURL);
                    Log.i("HERE NEW", "image url: " + imageURL);
                }))
                .addOnFailureListener(e -> Log.e("UPLOAD IMAGE", "Failed to upload image", e));
    }


}