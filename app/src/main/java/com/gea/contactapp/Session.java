package com.gea.contactapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {

    private SharedPreferences preferences;
    private String UserId, UserEmail;

    public Session(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getUserId() {
        return preferences.getString("UserId","");
    }

    public void setUserId(String userId) {
        preferences.edit().putString("UserId", userId).apply();
    }

    public String getUserEmail() {
        return preferences.getString("UserEmail","");
    }

    public void setUserEmail(String userEmail) {
        preferences.edit().putString("UserEmail", userEmail).apply();
    }

    public void logOut(){
        preferences.edit().clear().apply();
    }
}
