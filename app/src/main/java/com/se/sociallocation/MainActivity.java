package com.se.sociallocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.drive.Permission;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleApiClient mGoogleApiClient;
    private Location myLocation;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private String mUserName;
    private HashMap<String, Marker> mHashmap = new HashMap<>();

    ///* Code added for automatic check-in
    private final Handler handler = new Handler();
    private Runnable runnable;
    private int timeInterval;//minutes
    //*/

    private Boolean showLoc;
    private Boolean autoCheckIn;


    private OnSharedPreferenceChangeListener listener =
            new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("show_location_switch")) {
                        Log.d("PreferenceChanged", "Key!" + key); // the function you want called
                        showLoc = prefs.getBoolean("show_location_switch", true);
                        if (!showLoc) {
                            //delete user when they switch show location
                            mDatabase.child("data").child("locations").child(mUserId).removeValue();
                        } else if (autoCheckIn) {
                            handler.post(runnable); //re-post checkin location
                        }
                    } else if(key.equals("auto_checkin_switch")) {
                        Log.d("PreferenceChanged", "Key:" + key);
                        autoCheckIn = prefs.getBoolean("auto_checkin_switch", false);
                        if(autoCheckIn && showLoc){ //start posting again
                            handler.post(runnable);
                        }
                    } else if(key.equals("time_interval")) {
                        Log.d("PreferenceChanged", "Key:" + key);
                        timeInterval = Integer.parseInt(prefs.getString("time_interval", "5"));
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();

            mDatabase.child("data").child("users").child(mUserId).child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserName = dataSnapshot.getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            });

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            setActionButton(); //set call back on location submit button
            syncToggle(toolbar); //set up window bindings??

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            setUpFriends();
            checkForDeletedUser();
            loadPreferences();
            Log.d("Preferences", showLoc + ", " + autoCheckIn + ", " + Integer.toString(timeInterval));


            //Code for auto check-in
            runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("Auto", "Checked in");
                        //Post location to firebase
                        getMyLocation();

                        //now posts just change the value of the children
                        postLocation();
//                        mDatabase.child("data").child("locations").child(mUserId).child("lat").setValue(String.valueOf(myLocation.getLatitude()));
//                        mDatabase.child("data").child("locations").child(mUserId).child("lng").setValue(String.valueOf(myLocation.getLongitude()));
//                        mDatabase.child("data").child("locations").child(mUserId).child("name").setValue(mUserName); //post name in update
//                        mDatabase.child("data").child("locations").child(mUserId).child("lng").setValue(String.valueOf(timeInterval++));
//                        mHashmap.get(mUserId).showInfoWindow();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //60*1000 is one minute
                        //multiply by time interval to get number of minutes to wait
//                        handler.postDelayed(this, timeInterval*60*1000);
                        recallAutoCheckin(this);
//                        handler.postDelayed(this, timeInterval * 5 * 1000);
//                        handler.postDelayed(this, 5*1000);
                    }
                }
            };
            handler.removeCallbacksAndMessages(null);//remove all existing callbacks and messages
            if (autoCheckIn && showLoc) {
                handler.post(runnable);
            }
            //*/
        }
    }

//    @Override
//    protected void onStart(){
//
//    }
    private void postLocation(){
        if(showLoc && autoCheckIn) {
            mDatabase.child("data").child("locations").child(mUserId).child("lat").setValue(String.valueOf(myLocation.getLatitude()));
            mDatabase.child("data").child("locations").child(mUserId).child("lng").setValue(String.valueOf(myLocation.getLongitude()));
            mDatabase.child("data").child("locations").child(mUserId).child("name").setValue(mUserName); //post name in update
        }
    }

    private void recallAutoCheckin(Runnable run){
        if (autoCheckIn && showLoc) {
            handler.postDelayed(run, timeInterval * 1 * 1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == 1) {
                String user = data.getStringExtra("userID");
                Log.i("click6",user);

                moveToUser(user);
                // Go to this user!
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }

    protected void moveToUser(String userID){
        DatabaseReference userRef = mDatabase.child("data").child("locations").child(userID);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng") && dataSnapshot.hasChild("name")) {
                    Double lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                    Double lng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());
                    LatLng person = new LatLng(lat,lng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(person,18.0f));
                    mHashmap.get(dataSnapshot.getKey()).showInfoWindow();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

        enableMyLocation();
        getMyLocation();
        LatLng nd = new LatLng(41.703119, -86.238992); //Dome Coords
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nd,13.0f));
    }

    private void setUpFriends(){
        setFirebaseFriendBinding(mUserId);

        mDatabase.child("data").child("friends").child(mUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if( (long) dataSnapshot.getValue() >= 1) {
                    setFirebaseFriendBinding(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { //when value of friend is changed this will update
                if( (long) dataSnapshot.getValue() >= 1) {
                    setFirebaseFriendBinding(dataSnapshot.getKey());
                } else if( (long) dataSnapshot.getValue() == 0){  //this might be the reason for missing your person
                    removeFirebaseFriendBinding(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeFirebaseFriendBinding(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    //takes the userID of the person to bind to
    //input a userID
    //places this user on the current user's map
    private void setFirebaseFriendBinding( String userID) {

        mDatabase.child("data").child("locations").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng") && dataSnapshot.hasChild("name")) {
                    Double lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                    Double lng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());
                    LatLng person = new LatLng(lat,lng);
                    if (!mHashmap.containsKey(dataSnapshot.getKey())) { //marker logic, if not in hashmap then create a marker
                        if (dataSnapshot.getKey() != mUserId) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(person).title(dataSnapshot.child("name").getValue().toString())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                            mHashmap.put(dataSnapshot.getKey(), marker);
//                            Marker marker = mMap.addMarker(new MarkerOptions().position(person).title(dataSnapshot.child("name").getValue().toString())
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
//                            mHashmap.put(dataSnapshot.getKey(), marker);
                        } else { //current user
                            Marker marker = mMap.addMarker(new MarkerOptions().position(person).title(dataSnapshot.child("name").getValue().toString())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            mHashmap.put(dataSnapshot.getKey(), marker);
                        }
                    } else { //else if it is in the map so the marker exists, move it
                        mHashmap.get(dataSnapshot.getKey()).setPosition(person);
                        mHashmap.get(dataSnapshot.getKey()).setTitle(mUserName);
                        //if current user can move camera to their location. Only really makes sense when one has multiple devices 
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        setContentView(R.layout.activity_main);
//    }
    private void checkForDeletedUser() {

        mDatabase.child("data").child("locations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { //when value of friend is changed this will update

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("KEYRemoved", "OnChildRemoved"+dataSnapshot.getValue().toString());
                removeFirebaseFriendBinding(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private void removeFirebaseFriendBinding( String friendID){

        mHashmap.get(friendID).remove();
        mHashmap.remove(friendID);
        Log.d("Delete!!", "Friend deleted");
    }

    private void setActionButton(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showLoc) { //if sharing location
                    getMyLocation();
                    if(myLocation != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 17.0f));

                        //now posts just change the value of the children
                        mDatabase.child("data").child("locations").child(mUserId).child("lat").setValue(String.valueOf(myLocation.getLatitude()));
                        mDatabase.child("data").child("locations").child(mUserId).child("lng").setValue(String.valueOf(myLocation.getLongitude()));
                        mDatabase.child("data").child("locations").child(mUserId).child("name").setValue(mUserName);

                        if(mHashmap.get(mUserId) != null){
                            mHashmap.get(mUserId).showInfoWindow();
                        }

                        Snackbar.make(view, "Current Location Updated", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    Snackbar.make(view, "You are currently not sharing your location.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
    private void syncToggle(Toolbar toolbar){
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission to access the location is missing
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        getMyLocation();
    }

    public void getMyLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //TODO Ask Kris what this line is for
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i("Tag", "Connection Failed");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("TAG", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
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

        //noinspection SimplifiableIfStatement
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
            //current context
        } else if (id == R.id.nav_friends) {
            // intent to friends activity
            Intent intent = new Intent(this, FriendList.class);
            startActivityForResult(intent,1); //1 for all good
        } else if (id == R.id.nav_add_friend) {
            // go to add friend
            Intent intent = new Intent(this, addFriends.class);
            startActivityForResult(intent, 1);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, AppPreferences.class);
            startActivity(intent);
        } else if (id == R.id.nav_friend_requests) {
            Intent intent = new Intent(this, FriendRequests.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
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

    private void loadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        showLoc = sharedPreferences.getBoolean("show_location_switch", true);
        autoCheckIn = sharedPreferences.getBoolean("auto_checkin_switch", false);
        timeInterval = Integer.parseInt(sharedPreferences.getString("time_interval", "5"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }
}

