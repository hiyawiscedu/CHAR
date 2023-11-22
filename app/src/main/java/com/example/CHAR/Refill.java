package com.example.CHAR;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class Refill extends AppCompatActivity {

    int weightPercent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String websiteText;
            // Background work here
            try {
                Document doc = Jsoup.connect("http://192.168.4.1/").get();
                websiteText = doc.text();
            } catch (IOException e) {
                e.printStackTrace();
                websiteText = "Error: " + e.getMessage();
            }
            String finalWebsiteText = websiteText;
            Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
            Matcher matcher = pattern.matcher(finalWebsiteText);
            if (matcher.find()) {
                float floatValue = Float.parseFloat(matcher.group());
                // Round down
                int roundedValue = (int) Math.floor(floatValue);
                // Clamp to range 0-100
                roundedValue = Math.max(0, roundedValue);
                roundedValue = Math.min(100, roundedValue);
                weightPercent = roundedValue;
            }
            Log.i("Info", "" + weightPercent);
            handler.post(() -> {
            });
        });
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void refill(View view) {
        // Uri uri = Uri.parse("http://192.168.4.1/H");
        // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        // startActivity(intent);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://192.168.4.1", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }



}