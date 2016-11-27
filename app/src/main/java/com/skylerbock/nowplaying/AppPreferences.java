package com.skylerbock.nowplaying;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by sbock on 12/15/15.
 */
public class AppPreferences {
    private static final String KEY_PREFS_LAST_UPDATED = "last_updated";
    private static final String KEY_PREFS_LOCATION = "location";
    private static final String KEY_PREFS_BLUR = "blur";

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
        return _sharedPrefs.getString(KEY_PREFS_LOCATION, null);
    }

    public void setKeyPrefsLocation(String zipcode) {
        _prefsEditor.putString(KEY_PREFS_LOCATION, zipcode);
        _prefsEditor.commit();
    }

    public boolean getKeyPrefsBlur() {
        return !BuildConfig.DEBUG && _sharedPrefs.getBoolean(KEY_PREFS_BLUR, true);
    }

    public void setKeyPrefsBlur(boolean blur)
    {
        _prefsEditor.putBoolean(KEY_PREFS_BLUR, blur);
        _prefsEditor.commit();
    }
}