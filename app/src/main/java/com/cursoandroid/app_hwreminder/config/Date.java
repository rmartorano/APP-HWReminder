package com.cursoandroid.app_hwreminder.config;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Date {

    private long sextaMili;
    private Calendar calendar, sexta;
    private static DecimalFormat mFormat = new DecimalFormat("00");

    public Date(){
        this.calendar = Calendar.getInstance();
        this.calendar.set(Calendar.MONTH, this.calendar.get(Calendar.MONTH)+1);
        this.sexta = Calendar.getInstance();
        this.calendar.set(Calendar.DAY_OF_WEEK, this.calendar.getFirstDayOfWeek());
        this.calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // configura pro primeiro dia da semana ser segunda
        long timeMili = this.calendar.getTimeInMillis();
        this.sextaMili = timeMili + Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
        this.sexta.setTimeInMillis(sextaMili);
    }

    public String getMonthString(){
        return new SimpleDateFormat("MMMM").format(this.calendar.getTime());
    }

    public String getYearString(){
        return new SimpleDateFormat("yyyy").format(this.calendar.getTime());
    }

    public String getWeekIntervalAsChildString(){
        return "Semana "+mFormat.format(Double.valueOf(this.calendar.get(Calendar.DAY_OF_MONTH)))+" | "+mFormat.format(Double.valueOf(this.calendar.get(Calendar.MONTH)+1))+" a "+mFormat.format(Double.valueOf(sexta.get(Calendar.DAY_OF_MONTH)))+" | "+mFormat.format(Double.valueOf(sexta.get(Calendar.MONTH)+1));
    }

    public String getWeekInterval(){
        return "Semana "+mFormat.format(Double.valueOf(this.calendar.get(Calendar.DAY_OF_MONTH)))+" / "+mFormat.format(Double.valueOf(this.calendar.get(Calendar.MONTH)+1))+" a "+mFormat.format(Double.valueOf(sexta.get(Calendar.DAY_OF_MONTH)))+" / "+mFormat.format(Double.valueOf(sexta.get(Calendar.MONTH)+1));
    }

    public Map<String, String> getAllWeekIntervals(int mes) {

        Map<String, String> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, mes+1);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);


        for(int i=1 ; i<5 ; i++) {
            calendar.set(Calendar.WEEK_OF_MONTH, i);
            Calendar sexta = Calendar.getInstance();
            long timeMili = calendar.getTimeInMillis();
            long sextaMili = timeMili + Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
            sexta.setTimeInMillis(sextaMili);
            map.put(
                    "Semana "+i, mFormat.format(Double.valueOf(calendar.get(Calendar.DAY_OF_MONTH))) +
                            " | " + mFormat.format(Double.valueOf(calendar.get(Calendar.MONTH))) +
                            " a " + mFormat.format(Double.valueOf(sexta.get(Calendar.DAY_OF_MONTH))) +
                            " | " + mFormat.format(Double.valueOf(sexta.get(Calendar.MONTH)))
            );
        }

        return map;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Calendar getSexta(){
        return sexta;
    }

    public long getSextaMili() {
        return sextaMili;
    }
}
