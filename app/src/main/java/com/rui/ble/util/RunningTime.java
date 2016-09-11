package com.rui.ble.util;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by rhuang on 9/10/16.
 */
public class RunningTime {

    public RunningTime() {
        super();
    }

    public String getDate() {
        Calendar c = Calendar.getInstance();
        DecimalFormat df = new DecimalFormat("00");

        return new StringBuilder().append("_")
                .append(df.format(c.get(Calendar.YEAR))).append("-")
                .append(df.format(c.get(Calendar.MONTH) + 1)).append("-")
                .append(df.format(c.get(Calendar.DATE))).append("-")
                .append(df.format(c.get(Calendar.HOUR_OF_DAY))).append("-")
                .append(df.format(c.get(Calendar.MINUTE))).append("-")
                .append(df.format(c.get(Calendar.SECOND))).toString();
    }
}
