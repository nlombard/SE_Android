package com.se.sociallocation;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendRequests extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private String mName;
    ArrayList<String> friend_requests_array = new ArrayList<>();
    ArrayList<String> id_array = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();
            CreateFriendList();
            updateView();
            registerForContextMenu(findViewById(R.id.friend_requestsview));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mFirebaseAuth.signOut(); //Signout
            loadLogInView(); //leave page
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            //finsih intent and go back to the main map
            finish();
        } else if (id == R.id.nav_add_friend){
            Intent intent = new Intent(this, addFriends.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, 1);
            finish();
        } else if (id == R.id.nav_friends) {
            // intent to friends activity
            Intent intent = new Intent(this, FriendList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, 1);
            finish();

        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, 1);
            finish();

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, AppPreferences.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, 1);
            finish();
        } else if (id == R.id.nav_friend_requests){
            //current context
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void updateView(){

        String tArray[] = friend_requests_array.toArray(new String[0]);

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tArray);


        ListView friend_list = (ListView) findViewById(R.id.friend_requestsview);
        friend_list.setAdapter(itemsAdapter);

        friend_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.i("click",String.valueOf(position));
                Log.i("Click on user: ", id_array.get(position));
            }
        });
    }

    public void CreateFriendList(){
        mDatabase.child("data").child("friends").child(mUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if( (long) dataSnapshot.getValue() >= 2) { //value 2 for friend requests
                    //TODO Add to list here
                    //(dataSnapshot.getKey());
                    //get name here
                    findUserName(dataSnapshot.getKey());
                    //friend_array.add(findUserName(dataSnapshot.getKey()));
                    id_array.add(dataSnapshot.getKey());
                    //Log.i("click4", findUserName(dataSnapshot.getKey()));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { //when value of friend is changed this will update
                if( (long) dataSnapshot.getValue() >= 2) {
                    //TODO Add to list here
                    //(dataSnapshot.getKey());
                    findUserName(dataSnapshot.getKey());
                    //friend_array.add(dataSnapshot.child("name").getValue().toString());
                    id_array.add(dataSnapshot.getKey());
                } else if( (long) dataSnapshot.getValue() == 0){  //this might be the reason for missing your person
                    //TODO Remove from list here
                    //(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //TODO Remove to list here
                //(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public String findUserName(String userID){

        //eventually use mDatabase.child("data").child("users").child(userID).child("name").addValueEventListener()
        mDatabase.child("data").child("locations").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng") && dataSnapshot.hasChild("name")) {
                    mName = dataSnapshot.child("name").getValue().toString();
                    friend_requests_array.add(dataSnapshot.child("name").getValue().toString());
                    Log.i("new_item",friend_requests_array.get(friend_requests_array.size() - 1));
                    updateView();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return mName;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.long_press_menu_friend_requests ,menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getItemId() == R.id.confirm_add_friend) {
            Log.d("Add Friend", "Friend long pressed" + id_array.get(info.position));
            mDatabase.child("data").child("friends").child(mUserId).child(id_array.get(info.position)).setValue(1); //becomes friend
            mDatabase.child("data").child("friends").child(id_array.get(info.position)).child(mUserId).setValue(1); //they become your friend as well

            Intent intent = getIntent();
            finish();
            startActivity(intent);
            this.overridePendingTransition(0, 0);

        }
        return super.onContextItemSelected(item);
    }

}
