package com.se.sociallocation;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Kris on 11/15/2016.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

//    private static final String TAG = "MyFirebaseIIDService";
    private String token;

    public static String token() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    public void onTokenRefresh() {
        token = FirebaseInstanceId.getInstance().getToken();

        Log.d("Token", token);
    }
}
