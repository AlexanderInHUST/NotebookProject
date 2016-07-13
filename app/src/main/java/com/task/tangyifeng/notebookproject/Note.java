package com.task.tangyifeng.notebookproject;

import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by tangyifeng on 16/7/11.
 */
public class Note {

    private static final int MAX_OF_DES = 80;

    private String title;
    private int year;
    private int month;
    private int day;
    private String description;
    private String content;
    private String key;
    private Calendar time;
    private String[] pictures;

    public Note(String content, String key){
        this.content = content;
        this.key = key;
        time = Calendar.getInstance();
        year = time.get(Calendar.YEAR);
        month = time.get(Calendar.MONTH);
        day = time.get(Calendar.DAY_OF_MONTH);
        setDescription();
        setTitle();
    }

    public Note(String content,String key, String[] pictures){
        this(content, key);
        this.pictures = pictures;
    }

    public String getTitle(){
        return title;
    }

    public void setTime(String time){
        String[] s = time.split("/");
        day = Integer.parseInt(s[0]);
        month = Integer.parseInt(s[1]);
        year = Integer.parseInt(s[2]);
    }

    public void setKey(String key){
        this.key = key;
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

    public String getKey(){
        return key;
    }

    public String[] getPictures(){
        return pictures;
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

    private void setTitle(){
        StringBuilder builder = new StringBuilder();
        if(TextUtils.isEmpty(content))
            title = "(无内容)";
        else {
            for (int i = 0; i < Math.min(10, content.length()) && (content.charAt(i) != '\n'); i++) {
                builder.append(content.charAt(i));
            }
            title = builder.toString();
        }
    }

}
