package com.example.drockot.testapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class UhMessageService extends Service {

    private WampClient client;

    public UhMessageService() {
    }

    private final IBinder myBinder = new MyLocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        UhMessageService getService() {
            return UhMessageService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startConnection();
        return START_STICKY;
    }

    public void setupAuthSubscription (Observer<Boolean> observer, String username, String password) {
        final Observable<Boolean> stringSubscription = client.call("greenteam.auth", Boolean.class, username, password);
        stringSubscription.subscribe(observer);
    }

    public void setupMessageSubscription (String username) {
        final Observable<String> stringSubscription = client.makeSubscription("greenteam.user." + username + ".gotmessage", String.class);
        stringSubscription.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.d("Debug", s);
                writeToFile(s);
            }
        });
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("inbox.txt", Context.MODE_PRIVATE));
            outputStreamWriter.append(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void startConnection() {
        try {
            // Create a builder and configure the client
            WampClientBuilder builder = new WampClientBuilder();
            builder.withConnectorProvider(new NettyWampClientConnectorProvider())
                    .withUri("ws://androiddev05.dlss.com:8080/")
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(5, TimeUnit.SECONDS);
            // Create a client through the builder. This will not immediatly start
            // a connection attempt
            client = builder.build();
        } catch (WampError e) {
            // Catch exceptions that will be thrown in case of invalid configuration
            System.out.println(e);
            return;
        } catch (Exception e) {
            // Catch exceptions that will be thrown in case of invalid configuration
            System.out.println(e);
            return;
        }

        client.statusChanged()
                .subscribe(new Observer<WampClient.State>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(WampClient.State state) {

                    }
                });

        client.open();
    }
}
