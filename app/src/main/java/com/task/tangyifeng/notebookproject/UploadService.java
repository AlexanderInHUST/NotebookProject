package com.task.tangyifeng.notebookproject;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class UploadService extends Service {

    private static final String KEY_ID = "5785b5981532bc005d4ef956";

    private Note readyToUpload;
    private String key;
    private AVObject note;
    private AVObject keys;
    private AVQuery<AVObject> findKeys;
    private ArrayList<String> keysList;

    private boolean iniDoneNote = false;

    @Override
    public IBinder onBind(Intent intent){
        return new upLoadBinder();
    }

    @Override
    public void onCreate(){
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(readyToUpload == null)
                    ;
                initialNote();
                while(iniDoneNote == false)
                    ;
                if(TextUtils.isEmpty(readyToUpload.getKey())) {
                    initialFindKeys();
                    note.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            key = note.getObjectId();
                            keysList = (ArrayList<String>) keys.getList("keys");
                            keysList.add(key);
                            keys.put("keys",keysList);
                            keys.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    stopSelf();
                                }
                            });
                        }
                    });
                }else{
                    note.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if(e != null)
                                e.printStackTrace();
                            Log.d("save",note.toString());
                            stopSelf();
                        }
                    });
                }
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

    private void initialNote(){
        if(TextUtils.isEmpty(readyToUpload.getKey())) {
            note = new AVObject("note");
            ArrayList<String> uploadPicName = new ArrayList<>();
            ArrayList<String> uploadPictures = new ArrayList<>();
            if(readyToUpload.getPicName() != null) {
                for (String s : readyToUpload.getPicName())
                    uploadPicName.add(s);
                for(String s : readyToUpload.getPictures())
                    uploadPictures.add(s);
            }
            note.add("time", readyToUpload.getTime());
            note.add("content", readyToUpload.getContent());
            note.add("picName", uploadPicName);
            note.add("pictures",uploadPictures);
            iniDoneNote = true;
        }else{
            note = AVObject.createWithoutData("note",readyToUpload.getKey());
            ArrayList<String> uploadTime = new ArrayList<>();
            ArrayList<String> uploadContent = new ArrayList<>();
            ArrayList<String> pic = new ArrayList<>();
            ArrayList<String> picN = new ArrayList<>();
            ArrayList<ArrayList<String>> uploadPictures = new ArrayList<>();
            ArrayList<ArrayList<String>> uploadPicName = new ArrayList<>();
            uploadTime.add(readyToUpload.getTime());
            uploadContent.add(readyToUpload.getContent());
            for(String s: readyToUpload.getPictures()){
                pic.add(s);
            }
            for(String s : readyToUpload.getPicName()){
                picN.add(s);
            }
            uploadPicName.add(picN);
            uploadPictures.add(pic);
            note.put("time",uploadTime);
            note.put("content",uploadContent);
            note.put("pictures",uploadPictures);
            note.put("picName",uploadPicName);
            iniDoneNote = true;
        }
    }

    private void initialFindKeys(){
        findKeys = new AVQuery<>("AllKeys");
        findKeys.getInBackground(KEY_ID, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                keys = avObject;
            }
        });
        while(keys == null)
            ;
    }

}
