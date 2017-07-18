package com.example.l.myapplication;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

/**
 * Created by Abner on 2017/6/9.
 */

public class App extends Application {
    public static BaseActivity activity;
    public Vibrator mVibrator;
    public static App instance;

    public static Application getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


    }
}
