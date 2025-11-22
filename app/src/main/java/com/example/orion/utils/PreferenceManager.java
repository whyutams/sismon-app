package com.example.orion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PreferenceManager {
    private static final String PREF_NAME = "OrionAppPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_DATA = "user_data";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserData(JsonObject data) {
        editor.putString(KEY_USER_DATA, gson.toJson(data));
        editor.apply();
    }

    public JsonObject getUserData() {
        String json = prefs.getString(KEY_USER_DATA, null);
        return json != null ? gson.fromJson(json, JsonObject.class) : null;
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}