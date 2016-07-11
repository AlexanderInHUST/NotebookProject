package com.task.tangyifeng.notebookproject;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by tangyifeng on 16/7/11.
 */
public class MyLeanCloudApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"B8omQYPYP22CTDNLOclLpTD9-gzGzoHsz","fWYkd9dMiPr4Ul3puGt7ST5l");
    }
}

