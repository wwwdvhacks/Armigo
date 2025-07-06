package com.example.zayn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private TextView registerLink;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER = "remember";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        rememberMeCheckBox = findViewById(R.id.checkBoxRememberMe);
        loginButton = findViewById(R.id.buttonLogin);
        registerLink = findViewById(R.id.textViewRegister);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loadPreferences();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loadPreferences() {
        boolean remember = sharedPreferences.getBoolean(PREF_REMEMBER, false);
        if (remember) {
            String email = sharedPreferences.getString(PREF_EMAIL, "");
            String password = sharedPreferences.getString(PREF_PASSWORD, "");
            emailEditText.setText(email);
            passwordEditText.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }

    private void savePreferences(String email, String password, boolean remember) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (remember) {
            editor.putString(PREF_EMAIL, email);
            editor.putString(PREF_PASSWORD, password);
            editor.putBoolean(PREF_REMEMBER, true);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        boolean remember = rememberMeCheckBox.isChecked();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        savePreferences(email, password, remember);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 