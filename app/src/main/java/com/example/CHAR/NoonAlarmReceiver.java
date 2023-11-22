package com.example.CHAR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoonAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        // Update the water usage data here
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);

        // Obtain the new water usage data (this is just a placeholder for actual data retrieval logic)
        int newWaterUsage = -1;
        try {
            newWaterUsage = getTodaysWaterUsage();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // Create a WaterUsage object for the new data
        WaterUsage waterUsage = new WaterUsage(new Date(), newWaterUsage);

        // Add the new water usage to the SharedPreferences
        sharedPreferencesManager.addWaterUsage(waterUsage);

        // Example using LocalBroadcastManager to notify the activity
        Intent localIntent = new Intent("UPDATE_WATER_USAGE_DISPLAY");
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    private int getTodaysWaterUsage() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                String websiteText;
                // Background work here
                try {
                    Document doc = Jsoup.connect("http://192.168.4.1/").get();
                    websiteText = doc.text();
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            return getTodaysWaterUsageHelper(websiteText); // Returns the integer result
        });
        return future.get();
    }

//    private int getTodaysWaterUsage() {
//        Executor executor = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());
//        executor.execute(() -> {
//            String websiteText;
//            // Background work here
//            try {
//                Document doc = Jsoup.connect("http://192.168.4.1/").get();
//                websiteText = doc.text();
//            } catch (IOException e) {
//                e.printStackTrace();
//                websiteText = "Error: " + e.getMessage();
//            }
//            String finalWebsiteText = websiteText;
//            handler.post(() -> {
//
//            });
//        });
//        return -1;
//    }

    private int getTodaysWaterUsageHelper(String str) {
        Pattern pattern = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)\\s+([-+]?[0-9]*\\.?[0-9]+)$");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            float num1 = Float.parseFloat(matcher.group(1));
            float num2 = Float.parseFloat(matcher.group(2));
            return (int) Math.ceil(num2 - num1);
        }
        return -1;
    }
}
