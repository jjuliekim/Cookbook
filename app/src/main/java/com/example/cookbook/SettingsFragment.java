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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        groupCodeText = view.findViewById(R.id.groupCodeText);
        Button joinGroupButton = view.findViewById(R.id.joinGroupButton);
        TextView createCodeText = view.findViewById(R.id.createCodeText);
        joinGroupButton.setOnClickListener(v -> addGroup());
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
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user.getUid();
                    DatabaseReference groupRef = groupReference.child(code);
                    groupRef.setValue(userId).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Group Code Created: " + code, Toast.LENGTH_SHORT).show();
                            Log.i("HERE SETTINGS", "group code: " + code);
                            groupCodeText.setText(code);
                        } else {
                            Toast.makeText(getContext(), "Failed to Create Group Code", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // add code to user group list
    private void addGroup() {
        String code = groupCodeText.getText().toString();
        if (code.isEmpty()) {
            Toast.makeText(getContext(), "Enter Code", Toast.LENGTH_SHORT).show();
            return;
        }
        // if group code does not exist
        groupReference.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "Group Does Not Exist", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user.getUid();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}