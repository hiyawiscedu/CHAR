package com.example.CHAR;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {

    private static final String PREFS_NAME = "water_usage_prefs";
    private static final String KEY_WATER_USAGE_LIST = "water_usage_list";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveWaterUsageList(List<WaterUsage> waterUsages) {
        String json = gson.toJson(waterUsages);
        sharedPreferences.edit().putString(KEY_WATER_USAGE_LIST, json).apply();
    }

    public List<WaterUsage> getWaterUsageList() {
        String json = sharedPreferences.getString(KEY_WATER_USAGE_LIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<WaterUsage>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // Add methods to add and remove items from the list
    public void addWaterUsage(WaterUsage waterUsage) {
        List<WaterUsage> waterUsages = getWaterUsageList();
        // Add to the beginning of the list for the most recent date
        waterUsages.add(0, waterUsage);
        // Keep only the last 5 entries
        if (waterUsages.size() > 5) {
            waterUsages = waterUsages.subList(0, 5);
        }
        saveWaterUsageList(waterUsages);
    }

    public void removeOldestWaterUsage() {
        List<WaterUsage> waterUsages = getWaterUsageList();
        if (!waterUsages.isEmpty()) {
            // Remove the last item in the list which is the oldest
            waterUsages.remove(waterUsages.size() - 1);
            saveWaterUsageList(waterUsages);
        }
    }
}