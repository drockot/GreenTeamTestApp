package com.example.drockot.testapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import ws.wamp.jawampa.*;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;


public class MainActivity extends ActionBarActivity {

    private WampClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupSubscriptions () {
        final Observable<String> stringSubscription = client.makeSubscription("Test", String.class);
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
            }
        });
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
                    if (state instanceof WampClient.ConnectedState) {
                        setupSubscriptions();
                    }
                }
            });

        client.open();
    }

}
