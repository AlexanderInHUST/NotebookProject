package com.task.tangyifeng.notebookproject;

import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.okhttp.internal.framed.FramedStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tangyifeng on 16/7/15.
 */
public class LoadPicService extends Service {

    private PicCallBack callBack;
    private String[] picture;
    private String[] picName;
    private String[] picPath;
    private File[] pic;

    AVFile picFile;

    private int index;
    private int innerIndex;
    private ArrayList<Boolean> iniDone;

    @Override
    public IBinder onBind(Intent intent){
        Log.d("loadService","bind");
        return new loadPicBinder();
    }

    @Override
    public void onCreate(){
        Log.d("loadService","create");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (callBack == null) ;
                Log.d("loadPic","1");
                if(picture == null) {
                    Log.d("loadPic","picture null");
                    return;
                }
                Log.d("loadPic","2");
                iniDone = new ArrayList<Boolean>();
                pic = new File[picture.length];
                innerIndex = 0;
                Log.d("loadPic","3");
                callBack.sendStartLoadPic();
                for(index = 0; index < picture.length; index++) {
                    Log.d("loadPic",""+index);
                    iniDone.add(new Boolean(false));
                    picFile = new AVFile(picName[index], picture[index], new HashMap<String, Object>());
                    picFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, AVException e) {
                            if (e != null)
                                e.printStackTrace();
                            Log.d("loadPic", "4 got data");
                            File dirs = new File(picName[innerIndex]);
                            if (!dirs.exists())
                                dirs.mkdirs();
                            pic[innerIndex] = new File(picName[innerIndex]);
                            try {
                                FileOutputStream outputStream = new FileOutputStream(pic[innerIndex]);
                                outputStream.write(bytes, 0, bytes.length);
                            } catch (IOException IOe) {
                                IOe.printStackTrace();
                            }
                            Log.d("loadPic", "5 write done");
                            BitmapFactory factory = new BitmapFactory();
                            callBack.sendSetImage(factory.decodeFile(picName[innerIndex]));
                            innerIndex++;
                            iniDone.remove(0);
                        }
                    }, new ProgressCallback() {
                        @Override
                        public void done(Integer integer) {
                            callBack.sendLoadingProgress((integer - 1 == -1) ? 0 : integer -1);
                        }
                    });
                    while(!iniDone.isEmpty()) ;
                    callBack.sendLoadingProgress(100);
                }
            }
        }).start();

        return 0;
    }

    class loadPicBinder extends Binder{
        public Service getService(){
            return LoadPicService.this;
        }
    }

    public void setCallBack(PicCallBack callBack) {
        this.callBack = callBack;
    }

    public void setPicture(String[] picture){
        this.picture = picture;
    }

    public void setPicName(String[] picName) {
        this.picName = picName;
        if(picName == null)
            return;
        picPath = new String[picName.length];
        for (int i = 0; i < picName.length; i++) {
            String[] picPaths = picName[i].split("/");
            StringBuilder builder = new StringBuilder();
            builder.append("/");
            for (int j = 0; j < picPaths.length - 1; j++) {
                builder.append(picPaths[i]);
                builder.append("/");
            }
            picPath[i] = builder.toString();
        }
    }
}
