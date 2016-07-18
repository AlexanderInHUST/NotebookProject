package com.task.tangyifeng.notebookproject;

import java.util.ArrayList;

/**
 * Created by tangyifeng on 16/7/12.
 */
public interface CallBack {

    public void sendLoadedMsg();
    public void sendLoadingMsg();
    public void sendLoadingProgress(int progress);
    public void setNotes(ArrayList<Note> currentNotes);
    public void unbindDelete();
    public void unbindCloud();

}
