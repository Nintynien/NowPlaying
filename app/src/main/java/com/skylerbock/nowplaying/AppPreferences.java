package com.skylerbock.nowplaying;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by sbock on 12/15/15.
 */
public class AppPreferences {
    public static final String KEY_PREFS_LAST_UPDATED = "last_updated";
    public static final String KEY_PREFS_LOCATION = "location";

    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName(); //  Name of the file -.xml
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;

    public AppPreferences(Context context) {
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public Date getKeyPrefsLastUpdated() {
        long time = _sharedPrefs.getLong(KEY_PREFS_LAST_UPDATED, 0);
        return new Date(time);
    }

    public void setKeyPrefsLastUpdated(Date date) {
        _prefsEditor.putLong(KEY_PREFS_LAST_UPDATED, date.getTime());
        _prefsEditor.commit();
    }

    public String getKeyPrefsLocation() {
        String zipcode = _sharedPrefs.getString(KEY_PREFS_LOCATION, null);
        return zipcode;
    }

    public void setKeyPrefsLocation(String zipcode) {
        _prefsEditor.putString(KEY_PREFS_LOCATION, zipcode);
        _prefsEditor.commit();
    }
}