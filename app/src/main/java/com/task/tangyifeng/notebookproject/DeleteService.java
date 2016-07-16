package com.task.tangyifeng.notebookproject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;

import java.util.ArrayList;

/**
 * Created by tangyifeng on 16/7/13.
 */
public class DeleteService extends Service {

    private ArrayList<Boolean> deleteDone;
    private CallBack callBack;
    private AVQuery<AVObject> findKeys;
    private AVObject keys;

    private ArrayList<Note> toDeleteNotes;
    private ArrayList<String> newKeys;

    private static final String KEY_ID ="5785b5981532bc005d4ef956";

    @Override
    public IBinder onBind(Intent intent){
        return new deleteBinder();
    }

    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, final int flag, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteDone = new ArrayList<Boolean>();
                while(toDeleteNotes == null) ;
                findKeys = new AVQuery<AVObject>("AllKeys");
                findKeys.getInBackground(KEY_ID, new GetCallback<AVObject>() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        keys = avObject;
                    }
                });
                while(keys == null) ;
                newKeys = (ArrayList<String>) keys.get("keys");
                for(Note n: toDeleteNotes){
                    deleteDone.add(new Boolean(false));
                    AVObject note = AVObject.createWithoutData("note", n.getKey());
                    note.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(AVException e) {
                            if(e != null)
                                e.printStackTrace();
                            Log.d("delete","delete done.");
                            deleteDone.remove(0);
                        }
                    });
                    newKeys.remove(n.getKey());
                }
                while (!deleteDone.isEmpty()) ;
                keys = AVObject.createWithoutData("AllKeys", KEY_ID);
                keys.put("keys", newKeys);
                keys.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e != null)
                            e.printStackTrace();
                        Log.d("delete","save done");
                        callBack.unbindDelete();
                        stopSelf();
                    }
                });
            }
        }).start();
        return super.onStartCommand(intent,flag,startId);
    }

    class deleteBinder extends Binder{
        public Service getService(){
            return DeleteService.this;
        }
    }

    public void setToDeleteNotes(ArrayList<Note> notes){
        toDeleteNotes = notes;
    }

    public void setCallBack(CallBack callBack){
        this.callBack = callBack;
    }

}
