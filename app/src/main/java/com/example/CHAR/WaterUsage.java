package com.example.CHAR;

import java.util.Date;

public class WaterUsage {
    private Date date;
    private int waterUsed;

    public WaterUsage(Date date, int waterUsed) {
        this.date = date;
        this.waterUsed = waterUsed;
    }

    // Getters and Setters
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getWaterUsed() {
        return waterUsed;
    }

    public void setWaterUsed(int waterUsed) {
        this.waterUsed = waterUsed;
    }
}
