package com.example.parsechat;
import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Mensaje.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("hsbn")
                // if defined
                .clientKey(null)
                .server("http://192.168.10.100:1337/parse/")
                .build()
        );
    }
}