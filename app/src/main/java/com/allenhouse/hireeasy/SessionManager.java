package com.allenhouse.hireeasy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "HireEasySession";
    private static final String KEY_ROLE = "user_role"; // admin, agent, user
    private static final String KEY_ID = "user_id";     // admin_id, agent_id, user_id

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void createSession(String role, String userId) {
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_ID, userId);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.contains(KEY_ROLE) && sharedPreferences.contains(KEY_ID);
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_ID, null);
    }

    public void logout(Context context) {
        editor.clear();
        editor.apply();
        // Go to Splash after logout
        Intent i = new Intent(context, WaveSplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }
}
