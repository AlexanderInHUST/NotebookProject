package com.task.tangyifeng.notebookproject;

import android.os.SystemClock;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tangyifeng on 16/7/11.
 */
public class Note {

    private static final int MAX_OF_DES = 20;

    private String title;
    private int year;
    private int month;
    private int day;
    private String description;
    private String content;
    private Calendar time;

    public Note(String title, String content){
        this.title = title;
        this.content = content;
        time = Calendar.getInstance();
        year = time.get(Calendar.YEAR);
        month = time.get(Calendar.MONTH);
        day = time.get(Calendar.DAY_OF_MONTH);
        setDescription();
    }

    public String getTitle(){
        return title;
    }

    public String getTime(){
        return String.format(Locale.CHINA,"%d/%d/%d",year,month,day);
    }

    public String getDescription(){
        return description;
    }

    public String getContent(){
        return content;
    }

    private void setDescription(){
        if(TextUtils.isEmpty(content))
            description = null;
        else{
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < Math.min(MAX_OF_DES, content.length()); i++){
                builder.append(content.charAt(i));
            }
            description = builder.toString();
        }
    }

}
