package com.cursoandroid.app_hwreminder.config;

import android.util.Log;

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
        this.sexta = Calendar.getInstance();
        this.calendar.set(Calendar.DAY_OF_WEEK, this.calendar.getFirstDayOfWeek());
        this.calendar.setTimeInMillis(calendar.getTimeInMillis() + Long.parseLong("86400000")); // configura pro primeiro dia da semana ser segunda
        long timeMili = this.calendar.getTimeInMillis();
        this.sextaMili = timeMili + Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
        this.sexta.setTimeInMillis(sextaMili);
    }

    public String getMonthString(){
        return new SimpleDateFormat("MMMM", new java.util.Locale("pt","BR")).format(this.calendar.getTime());
    }

    public String getYearString(){
        return new SimpleDateFormat("yyyy", new java.util.Locale("pt","BR")).format(this.calendar.getTime());
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
        calendar.set(Calendar.MONTH, mes);
        calendar.set(Calendar.WEEK_OF_MONTH, 1);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        int qtdSemanas = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        for(int i=1 ; i<=qtdSemanas ; i++) {
            Log.i("Teste","month 1: "+calendar.get(Calendar.MONTH)+" month 2: "+month);
            if(calendar.get(Calendar.MONTH)!= month)
                break;
            Calendar sexta = Calendar.getInstance();
            sexta.setTime(calendar.getTime());
            long timeMili = calendar.getTimeInMillis();
            long sextaMili = timeMili + Long.parseLong("345600000"); // monday in millisecs + 4 days in millisecs
            sexta.setTimeInMillis(sextaMili);
            map.put(
                    "Semana "+i, mFormat.format(Double.valueOf(calendar.get(Calendar.DAY_OF_MONTH))) +
                            "/" + mFormat.format(Double.valueOf(calendar.get(Calendar.MONTH)+1)) +
                            " a " + mFormat.format(Double.valueOf(sexta.get(Calendar.DAY_OF_MONTH))) +
                            "/" + mFormat.format(Double.valueOf(sexta.get(Calendar.MONTH)+1))
            );
            calendar.setTimeInMillis(calendar.getTimeInMillis()+604800000); //+7 dias
        }
        return map;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendarTime(java.util.Date time){
        this.calendar.setTime(time);
    }

    public Calendar getSexta(){
        return sexta;
    }

    public void setSextaInMili(long mili){
        this.sexta.setTimeInMillis(mili);
    }

    public long getSextaMili() {
        return sextaMili;
    }
}
