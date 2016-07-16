package com.task.tangyifeng.notebookproject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.SaveCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangyifeng on 16/7/16.
 */
public class UploadPicService extends Service {

    PicCallBack callBack;

    String picName;
    String name;
    AVFile pic;

    @Override
    public IBinder onBind(Intent intent){
        return new uploadPicBinder();
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(callBack == null || picName == null || name == null) ;
                try {
                    pic = AVFile.withAbsoluteLocalPath(name, picName);
                    pic.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if(e != null)
                                e.printStackTrace();
                            Log.d("uploadPic","done" + pic.getUrl());
                            callBack.setPicture(pic.getUrl());
                            stopSelf();
                        }
                    });
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
        return 0;
    }

    class uploadPicBinder extends Binder{
        public Service getService(){
            return UploadPicService.this;
        }
    }

    public void setCallback(PicCallBack callback){
        this.callBack = callback;
    }

    public void setPicName(String picName){
        this.picName = picName;
        String names[] = picName.split("/");
        name = names[names.length - 1];
    }

}
