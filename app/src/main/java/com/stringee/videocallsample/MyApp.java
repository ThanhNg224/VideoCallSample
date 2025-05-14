// MyApp.java
package com.stringee.videocallsample;

import android.app.Application;

import com.stringee.StringeeClient;

public class MyApp extends Application {
    private StringeeClient client;

    public StringeeClient getClient() {
        return client;
    }

    public void setClient(StringeeClient client) {
        this.client = client;
    }
}
