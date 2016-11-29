package com.se.sociallocation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import com.google.firebase.iid.FirebaseInstanceId;
//import com.pusher.android.PusherAndroid;
//import com.pusher.android.notifications.PushNotificationRegistration;
import com.google.firebase.messaging.RemoteMessage;
import com.pusher.android.PusherAndroid;
import com.pusher.android.notifications.PushNotificationRegistration;
import com.pusher.android.notifications.fcm.FCMPushNotificationReceivedListener;
import com.pusher.android.notifications.interests.InterestSubscriptionChangeListener;
import com.pusher.android.notifications.tokens.PushNotificationRegistrationListener;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleApiClient mGoogleApiClient;
    private Location myLocation;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;
    private HashMap<String, Marker> mHashmap = new HashMap<String, Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Pusher pusher = new Pusher("c54c5ba2b4c92e1b7056");
        Channel channel = pusher.subscribe("test_channel");

        channel.bind("my_event", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
//                System.out.println(data);
                sendNotification(data);
            }
        });

        pusher.connect();

        registerPusher();

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String token = MyFirebaseInstanceIDService.token();
        Log.d("Token", token);

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getMyLocation();
                    FireLocation fire_location = new FireLocation(String.valueOf(myLocation.getLatitude()), String.valueOf(myLocation.getLongitude()));
                    mDatabase.child("data").child("locations").child(mUserId).setValue(fire_location);
                    Snackbar.make(view, "Your location sent to database", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            //getMyLocation();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            //Firebase callback setup
            mDatabase.child("data").child("locations").addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FireLocation fire_location = new FireLocation(dataSnapshot.child("lat").getValue().toString(),dataSnapshot.child("lng").getValue().toString());
                    LatLng person = new LatLng(Double.parseDouble(fire_location.getLat()),Double.parseDouble(fire_location.getLng()));
                    if(!mHashmap.containsKey(dataSnapshot.getKey())) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(person).title(dataSnapshot.getKey())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
                        mHashmap.put(dataSnapshot.getKey(), marker);
                    } else {
                        mHashmap.get(dataSnapshot.getKey()).setPosition(person);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //need to only change the one that matches the user id?! How to reget the database?
                    FireLocation fire_location = new FireLocation(dataSnapshot.child("lat").getValue().toString(),dataSnapshot.child("lng").getValue().toString());
                    LatLng person = new LatLng(Double.parseDouble(fire_location.getLat()),Double.parseDouble(fire_location.getLng()));
                    if(!mHashmap.containsKey(dataSnapshot.getKey())) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(person).title(dataSnapshot.getKey())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
                        mHashmap.put(dataSnapshot.getKey(), marker);
                    } else {
                        mHashmap.get(dataSnapshot.getKey()).setPosition(person);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot){

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


        @Override
        public void onMapReady(GoogleMap googleMap){
            mMap = googleMap;

            enableMyLocation();
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

        if (id == R.id.nav_camera) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng());
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
//            new NetworkUtils().execute();
            sendNotification("kris", "hey");
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

    public void sendNotification(String user, final String message) {

        Pusher pusher = new Pusher("c54c5ba2b4c92e1b7056");
        Channel channel = pusher.subscribe("test_channel");

        channel.bind("my_event", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channelName, String eventName, final String data) {
                System.out.println(data);
            }
        });

        pusher.connect();
    }

    public void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Social Location")
                .setContentText(messageBody)
//                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public void registerPusher() {
        PusherAndroid pusher = new PusherAndroid("c54c5ba2b4c92e1b7056");
        PushNotificationRegistration nativePusher = pusher.nativePusher();
        try {
            nativePusher.registerFCM(this, new PushNotificationRegistrationListener() {
                @Override
                public void onSuccessfulRegistration() {
                    Log.d("Registration", "success");
                }

                @Override
                public void onFailedRegistration(int i, String s) {
                    Log.d("Registration", "failure");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        nativePusher.subscribe("test_channel", new InterestSubscriptionChangeListener() {
            @Override
            public void onSubscriptionChangeSucceeded() {
                Log.d("Subscription", "success");
            }

            @Override
            public void onSubscriptionChangeFailed(int i, String s) {
                Log.d("Subscription", "failure");
            }
        });
        nativePusher.setFCMListener(new FCMPushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(RemoteMessage remoteMessage) {
                Log.d("Message", remoteMessage.getData().toString());
                sendNotification(remoteMessage.getData().toString());
            }
        });
    }
}

