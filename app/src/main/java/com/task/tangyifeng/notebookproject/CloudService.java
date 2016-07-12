package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class CloudService extends Service {

    private CallBack callBack;

    private ArrayList<Note> data;

    private static final String KEYID = "5784f9992e958a00642a1ef8";

    @Override
    public IBinder onBind(Intent intent){
        return new msgBinder();
    }

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

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

    ///////////////////////////////////////
    public void sendLoadingMsg(){
        callBack.sendLoadingMsg();
    }

    public void sendLoadedMsg(){
        callBack.sendLoadedMsg();
    }

    public void setCallBack(CallBack callBack){
        this.callBack = callBack;
    }

}
