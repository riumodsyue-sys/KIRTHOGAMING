package com.DCPC.Injector.WR;

import android.util.Log;

public class Application extends android.app.Application {
    static {
        System.loadLibrary("Start");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        try { Start(this); } catch (Exception e) { Log.e("InjectorLocal", "Start error: " + e); }
    }
    public native void Start(Object ctx);
}
