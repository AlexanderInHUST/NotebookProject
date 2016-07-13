package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;

import java.util.ArrayList;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class CloudService extends Service {

    private CallBack callBack;

    private ArrayList<Note> notes;
    private ArrayList<String> keys;
    private String key;
    private boolean done = false;

    private AVQuery<AVObject> findKeys;
    private AVQuery<AVObject> findNotes;

    private static final String KEY_ID = "5785b5981532bc005d4ef956";

    @Override
    public IBinder onBind(Intent intent){
        return new msgBinder();
    }

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(callBack == null)
                    ;
                callBack.sendLoadingMsg();
                notes = new ArrayList<Note>();
                getKeys();
                for(int i = 0; i < keys.size(); i++){
                    key = keys.get(i);
                    addNote();
                }
                while(notes.size() != keys.size());
                callBack.setNotes(notes);
                stopSelf();
            }
        }).start();
        return 0;
    }

    @Override
    public void onDestroy(){

    }

    class msgBinder extends Binder{
        public Service getService(){
            return CloudService.this;
        }
    }

    /////////////////////////////////////////

    private void addNote(){
        findNotes = new AVQuery<>("note");
        findNotes.getInBackground(key, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                String time = ((ArrayList<String>) avObject.get("time")).get(0);
                String content = ((ArrayList<String>) avObject.get("content")).get(0);
                ArrayList<String> pictures = (ArrayList<String>) avObject.get("pictures");
                String[] pic = (pictures == null ) ? null : new String[pictures.size()];
                for(int i = 0; pic != null  && i < pic.length; i++){
                    pic[i] = pictures.get(i);
                }
                Note addingNote = new Note(content, key, pic);
                addingNote.setTime(time);
                notes.add(addingNote);
                Log.d("add", addingNote.toString());
            }
        });
    }

    private void getKeys(){
        findKeys = new AVQuery<>("AllKeys");
        findKeys.getInBackground(KEY_ID, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                keys = (ArrayList<String>) avObject.get("keys");
            }
        });
        while(keys == null)
            ;
        Log.d("key",keys.toString());
    }

    public void setCallBack(CallBack callBack){
        this.callBack = callBack;
    }

}
