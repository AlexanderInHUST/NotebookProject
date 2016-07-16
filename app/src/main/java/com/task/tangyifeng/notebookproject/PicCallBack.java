package com.task.tangyifeng.notebookproject;

import android.graphics.Bitmap;

/**
 * Created by tangyifeng on 16/7/15.
 */
public interface PicCallBack {

    public void sendSetImage(Bitmap bitmap);
    public void setPicture(String picture);
    public void startUploadProgress();
    public void setUploadProgress(Integer progress);

}
