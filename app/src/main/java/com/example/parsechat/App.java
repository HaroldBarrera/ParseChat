package com.example.parsechat;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import android.app.Application;
import android.util.Log;

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