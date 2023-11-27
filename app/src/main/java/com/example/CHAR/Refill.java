package com.example.CHAR;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Refill extends AppCompatActivity {

    int weightPercent;
    private SharedPreferencesManager sharedPreferencesManager;
    private BroadcastReceiver updateUIReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill);
        sharedPreferencesManager = new SharedPreferencesManager(this);
        setupUpdateUIReceiver();
        //scheduleNoonAlarm();
        scheduleService();
        updateWaterUsageDisplay();
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

    public void scheduleService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WaterUsageUpdateService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 12:00 PM
        // CHANGE THESE TIMES FOR TESTING
        // 24-HOUR TIME
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past noon, set it to trigger the next day
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private void updateWaterUsageDisplay() {
        // Retrieve the list of water usages from SharedPreferencesManager.
        List<WaterUsage> waterUsages = sharedPreferencesManager.getWaterUsageList();

        // An array of TextView IDs for dates.
        int[] dateTextViewIds = new int[]{
                R.id.dateTextView1,
                R.id.dateTextView2,
                R.id.dateTextView3,
                R.id.dateTextView4,
                R.id.dateTextView5
        };

        // An array of TextView IDs for water usage amounts.
        int[] usageTextViewIds = new int[]{
                R.id.usageTextView1,
                R.id.usageTextView2,
                R.id.usageTextView3,
                R.id.usageTextView4,
                R.id.usageTextView5
        };

        // Define a date format for displaying the dates.
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        // Loop through each TextView ID and update the text.
        for (int i = 0; i < waterUsages.size(); i++) {
            // Get the corresponding TextView for the date and water usage.
            TextView dateTextView = findViewById(dateTextViewIds[i]);
            TextView usageTextView = findViewById(usageTextViewIds[i]);

            // Set the text for the date and water usage.
            WaterUsage waterUsage = waterUsages.get(i);
            dateTextView.setText(dateFormat.format(waterUsage.getDate()));
            usageTextView.setText(String.format(Locale.getDefault(), "%d mL", waterUsage.getWaterUsed()));
        }

        // If there are fewer than 5 records, clear the remaining TextViews.
        for (int i = waterUsages.size(); i < 5; i++) {
            TextView dateTextView = findViewById(dateTextViewIds[i]);
            TextView usageTextView = findViewById(usageTextViewIds[i]);
            dateTextView.setText(""); // Clear the date TextView
            usageTextView.setText(""); // Clear the water usage TextView
        }
    }

    private void setupUpdateUIReceiver() {
        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Update the UI using the method defined to refresh your TextViews
                updateWaterUsageDisplay();
            }
        };

        // Register to receive the local broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateUIReceiver, new IntentFilter("UPDATE_WATER_USAGE_DISPLAY"));
    }



    @Override
    protected void onDestroy() {
        // Unregister the local broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateUIReceiver);
        super.onDestroy();
    }


    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



}