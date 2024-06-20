package com.example.cookbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    private FirebaseAuth mAuth;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupReference = FirebaseDatabase.getInstance().getReference("groups");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        groupCodeText = view.findViewById(R.id.groupCodeText);
        Button joinGroupButton = view.findViewById(R.id.joinGroupButton);
        TextView createCodeText = view.findViewById(R.id.createCodeText);
        joinGroupButton.setOnClickListener(v -> addGroup(groupCodeText.getText().toString()));
        createCodeText.setOnClickListener(v -> createCode());
        // creating spinner
        Spinner spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sort_recipes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // save and load from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("page_prefs", Context.MODE_PRIVATE);
        int savedPosition = sharedPreferences.getInt("sort_option", 0);
        spinner.setSelection(savedPosition);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("sort_option", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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

    // check if code already exists
    private void checkCode(String code) {
        groupReference.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    createCode();
                } else {
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

    // add code to user group list
    private void addGroup(String code) {
        if (code.isEmpty()) {
            Toast.makeText(getContext(), "Enter Code", Toast.LENGTH_SHORT).show();
            return;
        }
        groupReference.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Save the group code in the groups database
                    groupReference.child(code).setValue(true).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            addUserToGroup(code);
                        } else {
                            Toast.makeText(getContext(), "Failed to join group", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    addUserToGroup(code);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // save group code in the users database
    private void addUserToGroup(String code) {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("groups");
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> groupsList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                        groupsList.add(groupSnapshot.getValue(String.class));
                    }
                }
                if (!groupsList.contains(code)) {
                    groupsList.add(code);
                    userReference.setValue(groupsList).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Group Joined: " + code, Toast.LENGTH_SHORT).show();
                            Log.i("HERE SETTINGS", "group joined: " + code);
                        } else {
                            Log.i("HERE SETTINGS", "failed to join group");
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Already in this group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}