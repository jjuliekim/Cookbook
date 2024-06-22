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
import android.widget.TableLayout;
import android.widget.TableRow;
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
        groupCodeText = view.findViewById(R.id.groupCodeText);
        Button joinGroupButton = view.findViewById(R.id.joinGroupButton);
        TextView createCodeText = view.findViewById(R.id.createCodeText);
        joinGroupButton.setOnClickListener(v -> addGroup(groupCodeText.getText().toString()));
        createCodeText.setOnClickListener(v -> createCode());
        groupCodeLayout = view.findViewById(R.id.groupCodeLayout);
        fetchGroups();
        return view;
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
                if (snapshot.exists()) {
                    // add user to group
                    addUserToGroup(code);
                } else {
                    // create new group and add user
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user.getUid();
                    groupReference.child(code).child("userIds").child(userId).setValue(true);
                    addCodeToUser(code);
                    Toast.makeText(getContext(), "Joined Group", Toast.LENGTH_SHORT).show();
                    Log.i("HERE SETTINGS", "new group joined");
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
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey(); // Get the group ID
                    Log.i("HERE SETTINGS", "Processing group ID: " + groupId);

                    // Check if the group has userIds
                    if (groupSnapshot.hasChild("userIds")) {
                        DataSnapshot userIdsSnapshot = groupSnapshot.child("userIds");
                        for (DataSnapshot userSnapshot : userIdsSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey(); // Get the user ID
                            Log.i("HERE SETTINGS", "User ID in group " + groupId + ": " + userId);
                            // Process user ID as needed
                            fetchUsernameAndDisplay(groupId, userId); // Fetch username and display it
                        }
                    } else {
                        Log.i("HERE SETTINGS", "Group " + groupId + " does not have userIds");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HERE SETTINGS", "Error fetching groups: " + databaseError.getMessage());
            }
        });
    }

    // Fetch username from user ID and display it in groupCodeLayout
    private void fetchUsernameAndDisplay(String groupId, String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue(String.class);
                Log.i("HERE SETTINGS", "Getting username: " + username);

                // Create a TextView to display group code and username
                String groupText = groupId + ": " + username;
                TextView groupTextView = new TextView(getContext());
                groupTextView.setText(groupText);
                groupTextView.setTextSize(18);
                groupCodeLayout.addView(groupTextView); // Add TextView to groupCodeLayout
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HERE SETTINGS", "Error fetching user: " + databaseError.getMessage());
            }
        });
    }



}