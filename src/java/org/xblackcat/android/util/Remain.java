package org.xblackcat.android.util;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 23.07.12 15:53
 *
 * @author xBlackCat
 */
public class Remain {
    private final static Pattern COUNTER_PATTERN = Pattern.compile("(\\d+)\\w");

    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int milliseconds;

    public Remain(long countDown) {
        milliseconds = (int) countDown % 1000;
        countDown /= 1000;
        seconds = (int) countDown % 60;
        countDown /= 60;
        minutes = (int) countDown % 60;
        countDown /= 60;
        hours = (int) countDown % 24;
        days = (int) countDown / 24;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Remain remain = (Remain) o;

        if (days != remain.days) return false;
        if (hours != remain.hours) return false;
        if (milliseconds != remain.milliseconds) return false;
        if (minutes != remain.minutes) return false;
        if (seconds != remain.seconds) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = days;
        result = 31 * result + hours;
        result = 31 * result + minutes;
        result = 31 * result + seconds;
        result = 31 * result + milliseconds;
        return result;
    }

    public SpannableString toString(int strResId, Context r) {
        String string = r.getString(
                strResId,
                days,
                hours,
                minutes,
                seconds
        ).toUpperCase();

        SpannableString coloredString = new SpannableString(string);


        Matcher m = COUNTER_PATTERN.matcher(string);
        while (m.find()) {
            ForegroundColorSpan countDownSpan = new ForegroundColorSpan(0xFFFFFFFF);
            coloredString.setSpan(countDownSpan, m.start(1), m.end(1), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return coloredString;
    }
}