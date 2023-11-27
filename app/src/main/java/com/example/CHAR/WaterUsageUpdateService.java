package com.example.CHAR;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WaterUsageUpdateService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // This service is not designed with binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Implement your logic to update water usage data here
        updateWaterUsageData();

        // If the system kills the service after `onStartCommand` returns, it restarts the service
        return START_STICKY;
    }

    private void updateWaterUsageData() {

        // Example implementation of your data update logic
        // This could involve fetching data from a sensor, API, or calculating based on user input

        // For demonstration, let's assume we have a method to fetch the current water usage
        int currentWaterUsage = -1;
        try {
            currentWaterUsage = getCurrentWaterUsage();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // Create a new WaterUsage object for today's data
        WaterUsage waterUsage = new WaterUsage(new Date(), currentWaterUsage);

        // Save this data to SharedPreferences
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        sharedPreferencesManager.addWaterUsage(waterUsage);

        // Optionally, if you want to notify the UI to update (if it's active), send a broadcast
        Intent localIntent = new Intent("UPDATE_WATER_USAGE_DISPLAY");
        sendBroadcast(localIntent);
    }

    private int getCurrentWaterUsage() throws ExecutionException, InterruptedException {
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
            return getCurrentWaterUsageHelper(websiteText); // Returns the integer result
        });
        return future.get();
    }

    private int getCurrentWaterUsageHelper(String str) {
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
