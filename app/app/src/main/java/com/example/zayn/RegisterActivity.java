package com.example.zayn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, usernameEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        usernameEditText = findViewById(R.id.editTextUsername);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        registerButton = findViewById(R.id.buttonRegister);
        loginLink = findViewById(R.id.textViewLogin);
        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String username = usernameEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Сохраняем имя
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    // Добавляем пользователя в Realtime Database
                                    String uid = user.getUid();
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("username", username);
                                    userData.put("email", email);
                                    userData.put("profilePictureUrl", ""); // пока пусто

                                    userRef.setValue(userData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity.this, "Failed to save user to DB.", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Failed to save username.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }


//    private void registerUser() {
//        String email = emailEditText.getText().toString();
//        String password = passwordEditText.getText().toString();
//        String confirmPassword = confirmPasswordEditText.getText().toString();
//        String username = usernameEditText.getText().toString();
//
//        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) || TextUtils.isEmpty(confirmPassword)) {
//            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!password.equals(confirmPassword)) {
//            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        // Set display name
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null) {
//                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                                    .setDisplayName(username)
//                                    .build();
//                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
//                                if (profileTask.isSuccessful()) {
//
//                                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
//                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
//                                    finish();
//                                } else {
//                                    Toast.makeText(RegisterActivity.this, "Failed to save username.", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    } else {
//                        Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
} 