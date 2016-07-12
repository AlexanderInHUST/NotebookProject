package com.task.tangyifeng.notebookproject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;

import java.util.ArrayList;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class UploadService extends Service {

    private static final String KEY_ID = "5784f9992e958a00642a1ef8";

    private Note readyToUpload;
    private String key;
    private AVObject note;
    private AVObject keys;
    private AVQuery<AVObject> findKeys;

    @Override
    public IBinder onBind(Intent intent){
        return new upLoadBinder();
    }

    @Override
    public void onCreate(){
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                initialNote();
                initialFindKeys();
                note.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                       key = note.getObjectId();
                    }
                });
                ((ArrayList<String>) keys.get("keys")).add(key);
                stopSelf();
            }
        }).start();
        return 0;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    class upLoadBinder extends Binder{
        public Service getService(){
            return UploadService.this;
        }
    }

    public void setReadyToUpload(Note setReadyToUpload){
        this.readyToUpload = setReadyToUpload;
    }

    ///////////////////
    private void initialNote(){
        note = new AVObject(readyToUpload.getTitle());
        note.add("time",readyToUpload.getTime());
        note.add("content",readyToUpload.getContent());
        note.add("pictures",readyToUpload.getPictures());
    }

    private void initialFindKeys(){
        findKeys = new AVQuery<>("FindKeys");
        findKeys.getInBackground(KEY_ID, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                keys = avObject;
            }
        });
    }

}
