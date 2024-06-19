package com.example.cookbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private EditText nameText;
    private EditText passwordText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // xml contents
        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);
        nameText = findViewById(R.id.nameEditText);
        passwordText = findViewById(R.id.passwordEditText);

        // button actions
        loginButton.setOnClickListener(v -> loginUser());
        signupButton.setOnClickListener(v -> signupUser());
    }

    // new user process
    private void signupUser() {
        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();
        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Invalid Entries", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(name + "@email.com", password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // sign up success
                        Log.i("HERE LOGIN", "user created: " + name);
                        Toast.makeText(this, "Creating Account...", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                        User newUser = new User(user.getUid(), name, new ArrayList<>());
                        userRef.setValue(newUser);
                        Intent nextIntent = new Intent(LoginActivity.this, MainActivity.class);
                        nextIntent.putExtra("user", user);
                        nextIntent.putExtra("name", name);
                        startActivity(nextIntent);
                    } else {
                        // sign up failed
                        Log.i("HERE LOGIN", "registration failed: " + task.getException());
                        Toast.makeText(LoginActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // verify user/pw and log in
    private void loginUser() {
        String name = nameText.getText().toString();
        String password = passwordText.getText().toString();
        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Invalid Entries", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(name + "@email.com", password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // log in success
                        Log.i("HERE LOGIN", "logging in: " + name);
                        Toast.makeText(this, "Logging In...", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent nextIntent = new Intent(LoginActivity.this, MainActivity.class);
                        nextIntent.putExtra("user", user);
                        nextIntent.putExtra("name", name);
                        startActivity(nextIntent);
                    } else {
                        // log in failed
                        Log.i("HERE LOGIN", "login failed: " + task.getException());
                        Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}