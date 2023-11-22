package com.example.CHAR;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    TextView textPercent;
    ProgressBar progressBar;
    private boolean notificationShown = false;
    private static final long DEBOUNCE_DELAY = 10000; // 10 seconds delay between notifications to account for fluctuations
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        textPercent = findViewById(R.id.txtper);
        progressBar = findViewById(R.id.Prog);
        scheduler.scheduleAtFixedRate(this::updateProgressBar, 0, 1, TimeUnit.SECONDS);
//        btnCal.setOnClickListener(v -> {
//            Executor executor = Executors.newSingleThreadExecutor();
//            Handler handler = new Handler(Looper.getMainLooper());
//
//            executor.execute(() -> {
//                String websiteText;
//                // Background work here
//                try {
//                    Document doc = Jsoup.connect("http://192.168.4.1/").get();
//                    websiteText = doc.text();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    websiteText = "Error: " + e.getMessage();
//                }
//                String finalWebsiteText = websiteText;
//                Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
//                Matcher matcher = pattern.matcher(finalWebsiteText);
//                if (matcher.find()) {
//                    float floatValue = Float.parseFloat(matcher.group());
//                    // Round down
//                    int roundedValue = (int) Math.floor(floatValue);
//                    // Clamp to range 0-100
//                    roundedValue = Math.max(0, roundedValue);
//                    roundedValue = Math.min(100, roundedValue);
//                    weightPercent = roundedValue;
//                }
//                handler.post(() -> {
//                    txtper.setText(weightPercent + "%");
//                    Prog.setProgress(weightPercent);
//                });
//            });
//        });
    }

    public void updateProgressBar() {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect("http://192.168.4.1/").get();
                String text = doc.text();
                int progressValue = extractAndRoundFloat(text);
                uiHandler.post(() -> {
                    textPercent.setText(progressValue + "%");
                    progressBar.setProgress(progressValue);
                    progressBar.setProgress(progressValue);
                    if (progressValue < 20 && !notificationShown) {
                        showNotification();
                        notificationShown = true;
                    } else if (progressValue >= 20 && notificationShown) {
                        uiHandler.postDelayed(() -> notificationShown = false, DEBOUNCE_DELAY);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public int extractAndRoundFloat(String str) {
        Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            float floatValue = Float.parseFloat(matcher.group());
            // Round down
            int roundedValue = (int) Math.floor(floatValue);
            // Clamp to range 0-100
            roundedValue = Math.max(0, roundedValue); // Ensure it's not less than 0
            roundedValue = Math.min(100, roundedValue); // Ensure it's not more than 100
            return roundedValue;
        }
        return -1; // Or any other indication that no float was found
    }

    public void goToAmazon (View view) {
        String url = "https://www.amazon.com/Pure-Life-Distilled-Water-Gallon/dp/B0BFK1C5LB/ref=sr_1_3?crid=13U7CBO7DER5F&keywords=1+gallon+of+distilled+water&qid=1698988510&sprefix=1+gallon+of+distilled+water%2Caps%2C79&sr=8-3";
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public void goToRefill(View view) {
        Intent intent = new Intent(this, Refill.class);
        startActivity(intent);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background) // replace with your own icon
                .setContentTitle("Alert")
                .setContentText("Water remaining is less than 20%")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduler.shutdownNow(); // Important to avoid memory leaks
    }
}