package com.se.sociallocation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    protected EditText passwordEditText;
    protected EditText confirmPasswordEditText;
    protected EditText emailEditText;
    protected EditText usernameEditText;
    protected Button signUpButton;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        passwordEditText = (EditText)findViewById(R.id.password);
        usernameEditText = (EditText)findViewById(R.id.username);
        confirmPasswordEditText = (EditText)findViewById(R.id.confirm_password);
        emailEditText = (EditText)findViewById(R.id.email);
        signUpButton = (Button)findViewById(R.id.email_sign_up_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                final String email = emailEditText.getText().toString().trim();
                final String username = usernameEditText.getText().toString().trim();

                password = password.trim();
                confirmPassword = confirmPassword.trim();
//                email = email.trim();
//                username = username.trim();

                if (password.isEmpty() || email.isEmpty() || username.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if(confirmPassword.isEmpty()) {
                    confirmPasswordEditText.setError(getString(R.string.confirm_password_missing));
                    confirmPasswordEditText.requestFocus();
                } else if(!checkPassword(password,confirmPassword)) {/*function to check passwords that returns bool*/
                    confirmPasswordEditText.setError(getString(R.string.password_match_error));
                    confirmPasswordEditText.requestFocus();
                    confirmPasswordEditText.setText("");
                } else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        addUser(email, username);
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                        builder.setMessage(task.getException().getMessage())
                                                .setTitle(R.string.login_error_title)
                                                .setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean checkPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    private void addUser(String email, String username) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.child("data").child("users").child(mUser.getUid()).child("email").setValue(email);
        mDatabase.child("data").child("users").child(mUser.getUid()).child("name").setValue(username);
    }
}