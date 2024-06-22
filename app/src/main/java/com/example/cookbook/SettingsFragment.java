package com.example.cookbook;

import android.os.Bundle;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Random;

public class SettingsFragment extends Fragment {
    private EditText groupCodeText;
    private DatabaseReference groupReference;
    private DatabaseReference userReference;
    private FirebaseAuth mAuth;
    private LinearLayout groupCodeLayout;
    private TextView numberRecipeText;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupReference = FirebaseDatabase.getInstance().getReference("groups");
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        numberRecipeText = view.findViewById(R.id.numberRecipesText);
        countRecipes();
        groupCodeText = view.findViewById(R.id.groupCodeText);
        Button joinGroupButton = view.findViewById(R.id.joinGroupButton);
        TextView createCodeText = view.findViewById(R.id.createCodeText);
        joinGroupButton.setOnClickListener(v -> addGroup(groupCodeText.getText().toString()));
        createCodeText.setOnClickListener(v -> createCode());
        groupCodeLayout = view.findViewById(R.id.groupCodeLayout);
        fetchGroups();
        return view;
    }

    // display number of recipes created by user
    private void countRecipes() {
        DatabaseReference recipeDatabase = FirebaseDatabase.getInstance().getReference("recipes");
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        recipeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int recipeCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null && userId.equals(recipe.getUser())) {
                        recipeCount++;
                    }
                }
                numberRecipeText.setText(String.format("Number of Recipes Created: %d", recipeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE SETTINGS", "failed to count recipes");
                Toast.makeText(getContext(), "Failed to count recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // generate group code for user
    private void createCode() {
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(characters.charAt(random.nextInt(characters.length())));
        }
        String code = builder.toString();
        Log.i("HERE SETTINGS", "code: " + code);
        checkCode(code);
    }

    // check if code already exists in "groups"
    private void checkCode(String code) {
        groupReference.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // avoid repeated codes
                    createCode();
                } else {
                    // display code for user
                    groupCodeText.setText(code);
                    Toast.makeText(getContext(), "Code Created", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "check code e: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // join group action
    private void addGroup(String code) {
        if (code.isEmpty()) {
            Toast.makeText(getContext(), "Enter Code", Toast.LENGTH_SHORT).show();
            return;
        }
        // add code to "groups" with user id
        groupReference.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupCodeText.setText("");
                if (snapshot.exists()) {
                    // group already exists, add user
                    addUserToGroup(code);
                } else {
                    // create new group and add user
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user.getUid();
                    groupReference.child(code).child("userIds").child(userId).setValue(true);
                    addCodeToUser(code);
                    Toast.makeText(getContext(), "Joined Group", Toast.LENGTH_SHORT).show();
                    Log.i("HERE SETTINGS", "new group joined");
                    updateGroupTextView(code);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE SETTINGS", "error add group: " + error.getMessage());
            }
        });
    }

    // add user to existing group code database
    private void addUserToGroup(String code) {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        groupReference.child(code).child("userIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(userId)) {
                    Toast.makeText(getContext(), "Already in Group", Toast.LENGTH_SHORT).show();
                } else {
                    addCodeToUser(code);
                    groupReference.child(code).child("userIds").child(userId).setValue(true);
                    Toast.makeText(getContext(), "Joined Group", Toast.LENGTH_SHORT).show();
                    Log.i("HERE SETTINGS", "joined group");
                    updateGroupTextView(code);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE SETTINGS", "error joining group: " + error.getMessage());
            }
        });
    }

    // add group code under "users" list of groups
    private void addCodeToUser(String code) {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        ArrayList<String> userGroups = user.getGroups();
                        if (userGroups == null) {
                            userGroups = new ArrayList<>();
                        }
                        if (!userGroups.contains(code)) {
                            userGroups.add(code);
                            user.setGroups(userGroups);
                            userReference.setValue(user)
                                    .addOnSuccessListener(e -> Log.i("HERE SETTINGS", "Group code added to user's groups"))
                                    .addOnFailureListener(e -> Log.e("HERE SETTINGS", "Error adding group code to user's groups: " + e.getMessage()));
                        } else {
                            // The group code is already in the user's groups list
                            Log.i("HERE SETTINGS", "Group code already exists in user's groups");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("HERE SETTINGS", "error adding group code: " + error.getMessage());
            }
        });
    }

    // fetch groups and group members
    private void fetchGroups() {
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupCodeLayout.removeAllViews();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    Log.i("HERE SETTINGS", "Processing group: " + groupId);
                    // Check if the current user is a member of this group
                    if (groupSnapshot.child("userIds").hasChild(mAuth.getCurrentUser().getUid())) {
                        DataSnapshot userIdsSnapshot = groupSnapshot.child("userIds");
                        ArrayList<String> userIds = new ArrayList<>();
                        for (DataSnapshot userSnapshot : userIdsSnapshot.getChildren()) {
                            userIds.add(userSnapshot.getKey());
                        }
                        fetchUsernamesAndDisplay(groupId, userIds);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HERE SETTINGS", "Error fetching groups: " + databaseError.getMessage());
            }
        });
    }

    // fetch usernames from user IDs and display them in groupCodeLayout
    private void fetchUsernamesAndDisplay(String groupId, ArrayList<String> userIds) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        ArrayList<String> usernames = new ArrayList<>();
        for (String userId : userIds) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String username = dataSnapshot.child("name").getValue(String.class);
                    if (username != null) {
                        usernames.add(username);
                    }
                    // Check if all usernames have been fetched
                    if (usernames.size() == userIds.size()) {
                        String groupText = groupId + ": " + String.join(", ", usernames);
                        TextView groupTextView = new TextView(getContext());
                        groupTextView.setText(groupText);
                        groupTextView.setTextSize(20);
                        groupCodeLayout.addView(groupTextView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HERE SETTINGS", "Error fetching user: " + databaseError.getMessage());
                }
            });
        }
    }

    // update UI
    private void updateGroupTextView(String code) {
        groupReference.child(code).child("userIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> userIds = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    userIds.add(userSnapshot.getKey());
                }
                fetchUsernamesAndDisplay(code, userIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HERE SETTINGS", "Error updating group TextView: " + error.getMessage());
            }
        });
    }
}
