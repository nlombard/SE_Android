package com.se.sociallocation;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            TextView emailTV = (TextView) findViewById(R.id.profile_email);
            TextView usernameTV = (TextView) findViewById(R.id.profile_username);
            Button button = (Button) findViewById(R.id.update_username_button);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);

            mUserId = mFirebaseUser.getUid();
            mDatabase.child("data").child("users").child(mUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUsername = dataSnapshot.getValue().toString();
                    updateName(mUsername);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            });

            emailTV.setText("nlombard@nd.edu");

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name;
                    TextView username = (TextView) findViewById(R.id.profile_username);
                    name = username.getText().toString();
                    updateUsername(name);
                    Snackbar.make(view, "Username Updated", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return true;
    }

    public void updateName(String name){
        TextView username = (TextView) findViewById(R.id.profile_username);
        username.setText(name);
    }
    public void updateUsername(String name){
        mDatabase.child("data").child("users").child(mUserId).child("name").setValue(name);
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
