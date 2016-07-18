package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class EditActivity extends Activity implements PicCallBack{

    private static final int NOT_EDIT = 0;
    private static final int EDITING = 1;
    private static final int NEW = 0;
    private static final int NOT_NEW = 1;
    private static final int MSG_SET_PIC = 0;
    private static final int MSG_START_UPDATING = 1;
    private static final int MSG_UPDATING = 2;
    private static final int MSG_START_LOADING = 3;
    private static final int MSG_LOADING_PROGRESS =4;
    private static final int ID = 19970416;

    private EditText editText;
    private TextSwitcher addPicSwitcher;
    private RelativeLayout backButton;
    private LinearLayout editView;
    private ArrayList<Bitmap> bitmaps;
    private ImageView toDeleteImage;
    private ProgressDialog uploadProgress;
    private ProgressDialog loadingPicProgress;

    private int count = 0;
    private int pos = 0;
    private int toDeleteId;
    private ArrayList<Integer> hasDelete;
    private Note note;
    private PopupMenu popupMenu;
    private String content;
    private String[] pictures;
    private String[] picName;
    private String localTime;
    private String key;
    private String tempPic;
    private UploadService service;
    private UploadService.upLoadBinder binder;
    private ServiceConnection connection;
    private LoadPicService picService;
    private LoadPicService.loadPicBinder picBinder;
    private ServiceConnection picConnection;
    private UploadPicService uploadPicService;
    private UploadPicService.uploadPicBinder uploadPicBinder;
    private ServiceConnection uploadPicServiceConnection;
    private Intent picIntent;
    private Intent backIntent;
    private Intent addIntent;
    private Intent uploadPicIntent;
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_SET_PIC:{
                    Log.d("loadPic","6 msg send done");
                    count ++;
                    setImage((Bitmap) msg.obj);
                    if(count == pictures.length)
                        unbindService(picConnection);
                    break;
                }
                case MSG_START_UPDATING:{
                    uploadProgress = new ProgressDialog(EditActivity.this);
                    uploadProgress.setMessage("图片上传中");
                    uploadProgress.setMax(100);
                    uploadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    uploadProgress.setIndeterminate(false);
                    uploadProgress.setProgress(0);
                    uploadProgress.show();
                    break;
                }
                case MSG_UPDATING:{
                    uploadProgress.setProgress(msg.arg1);
                    if(msg.arg1 == 100){
                        uploadProgress.dismiss();
                    }
                    break;
                }
                case MSG_START_LOADING:{
                    loadingPicProgress = new ProgressDialog(EditActivity.this);
                    loadingPicProgress.setMessage("图片加载中");
                    loadingPicProgress.setProgress(0);
                    loadingPicProgress.setMax(100);
                    loadingPicProgress.setIndeterminate(false);
                    loadingPicProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    loadingPicProgress.show();
                    break;
                }
                case MSG_LOADING_PROGRESS:{
                    loadingPicProgress.setProgress(msg.arg1);
                    if(msg.arg1 == 100){
                        loadingPicProgress.dismiss();
                    }
                    break;
                }
            }
        }

    };

    private int isEditing = NOT_EDIT;
    private int isNew = NOT_NEW;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);
        initialViews();
    }


    //////Add a new picture
    @Override
    protected void onActivityResult(int requestId, int resultId, Intent data){
        super.onActivityResult(requestId, resultId, data);
        if(resultId == RESULT_OK){
            ContentResolver resolver = getContentResolver();
            Uri imageUri = data.getData();
            Cursor imageCursor = resolver.query(imageUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            imageCursor.moveToFirst();
            tempPic = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            BitmapFactory factory = new BitmapFactory();
            Bitmap tempBitmap = factory.decodeFile(tempPic);
            bitmaps.add(tempBitmap);
            setImage(tempBitmap);
            setPicName(tempPic);
            uploadPicIntent = new Intent(EditActivity.this, UploadPicService.class);
            initialUploadPicService();
            startService(uploadPicIntent);
        }
    }

    private void initialUploadPicService(){
        uploadPicServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                uploadPicBinder = (UploadPicService.uploadPicBinder) iBinder;
                uploadPicService = (UploadPicService) uploadPicBinder.getService();
                uploadPicService.setCallback(EditActivity.this);
                uploadPicService.setPicName(tempPic);
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        bindService(uploadPicIntent, uploadPicServiceConnection, BIND_AUTO_CREATE);
    }

    //initial views
    private void initialViews(){
        initialNote();
        Log.d("ini","1");
        initialEditText();
        Log.d("ini","2");
        initialAddPicButton();
        Log.d("ini","3");
        showAddPicButton();
        Log.d("ini","4");
        initialBackButton();
        Log.d("ini","5");
        initialImage();
        Log.d("ini","6");
    }
    
    private void initialImage(){
        bitmaps = new ArrayList<>();
        hasDelete = new ArrayList<>();
        picIntent = new Intent(EditActivity.this, LoadPicService.class);
        initialLoadPicService();
        startService(picIntent);
        Log.d("loadPic",""+picIntent);
    }

    private void initialLoadPicService(){
        picConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                picBinder = (LoadPicService.loadPicBinder) iBinder;
                picService = (LoadPicService) picBinder.getService();
                picService.setCallBack(EditActivity.this);
                picService.setPicName(picName);
                picService.setPicture(pictures);
                Log.d("load","set");
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("load","fail");
            }
        };
        bindService(picIntent, picConnection, Context.BIND_AUTO_CREATE);
        Log.d("bind","binded");
    }

    @Override
    public void sendSetImage(Bitmap bitmap){
        Message msg = new Message();
        msg.what = MSG_SET_PIC;
        msg.obj = bitmap;
        handler.sendMessage(msg);
    }

    @Override
    public void setPicture(String picture){
        setPictures(picture);
    }

    @Override
    public void setUploadProgress(Integer progress){
        Message msg = new Message();
        msg.what = MSG_UPDATING;
        msg.arg1 = progress;
        handler.sendMessage(msg);
    }

    @Override
    public void startUploadProgress(){
        Message msg = new Message();
        msg.what = MSG_START_UPDATING;
        handler.sendMessage(msg);
    }

    @Override
    public void sendStartLoadPic(){
        Message msg = new Message();
        msg.what = MSG_START_LOADING;
        handler.sendMessage(msg);
    }

    @Override
    public void sendLoadingProgress(int progress){
        Message msg = new Message();
        msg.what = MSG_LOADING_PROGRESS;
        msg.arg1 = progress;
        handler.sendMessage(msg);
    }

    private void setImage(Bitmap resource){
        final ImageView addImage = new ImageView(EditActivity.this);
        editView = (LinearLayout)findViewById(R.id.edit_activity_edit_view);
        addImage.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addImage.setPadding(transDpToPx(20),transDpToPx(15),transDpToPx(20), transDpToPx(15));
        addImage.setImageBitmap(resource);
        addImage.setId(pos + ID);
        pos++;
        if(hasDelete.isEmpty())
            hasDelete.add(0);
        else
            hasDelete.add(hasDelete.get(hasDelete.size() - 1));
        addImage.setOnLongClickListener(deleteClickListener);
        editView.setGravity(Gravity.CENTER_HORIZONTAL);
        editView.addView(addImage);
        Log.d("loadPic","7 pic set done");
    }

    private View.OnLongClickListener deleteClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            popupMenu = new PopupMenu(EditActivity.this, view);
            toDeleteImage = (ImageView) view;
            toDeleteId = toDeleteImage.getId();
            getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Log.d("menu","" + toDeleteImage + " " + (toDeleteId - ID));
                    String[] tempPictures = new String[pictures.length - 1];
                    String[] tempPicName = new String[picName.length - 1];
                    System.arraycopy(toDeleteString(pictures, toDeleteId - ID - hasDelete.get(toDeleteId - ID)),0,tempPictures,0,tempPictures.length);
                    System.arraycopy(toDeleteString(picName, toDeleteId - ID - hasDelete.get(toDeleteId - ID)),0,tempPicName,0,tempPicName.length);
                    pictures = new String[pictures.length - 1];
                    picName = new String[picName.length - 1];
                    System.arraycopy(tempPictures,0,pictures,0,pictures.length);
                    System.arraycopy(tempPicName,0,picName,0,picName.length);
                    toDeleteImage.setVisibility(View.GONE);
                    for(int i = toDeleteId - ID; i < hasDelete.size(); i++){
                        hasDelete.set(i, hasDelete.get(i) + 1);
                    }
                    for(Integer i : hasDelete){
                        Log.d("hasDelete","" + i);
                    }
                    return true;
                }
            });
            popupMenu.show();
            return true;
        }
    };

    private String[] toDeleteString(String[] resource, int index){
        LinkedList<String> temp = new LinkedList<>();
        for(String s : resource)
            temp.add(s);
        temp.remove(index);
        Iterator<String> it = temp.iterator();
        String[] result = new String[temp.size()];
        for(int i = 0 ; i < result.length; i++)
            result[i] = it.next();
        return result;
    }

    private int transDpToPx(int Dp){
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (Dp * scale + 0.5f);
    }

    ////
    private void initialService(){
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (UploadService.upLoadBinder) iBinder;
                service = (UploadService) binder.getService();
                service.setReadyToUpload(note);
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("asd","damn");
            }
        };
        bindService(addIntent, connection, Context.BIND_AUTO_CREATE);
    }

    //Back button's jobs
    private void initialBackButton(){
        backButton = (RelativeLayout)findViewById(R.id.edit_activity_back_button);
        backButton.setOnClickListener(backButtonListener);
    }

    private View.OnClickListener backButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getNote();
            backIntent = new Intent(EditActivity.this, MainActivity.class);
            addIntent = new Intent(EditActivity.this, UploadService.class);
            initialService();
            backIntent.putExtra("Edit","I'm from edit page");
            startActivity(backIntent);
            startService(addIntent);
            overridePendingTransition(R.anim.from_left, R.anim.to_right);
        }
    };

    //AddPic button's jobs
    private void initialAddPicButton(){
        addPicSwitcher = (TextSwitcher)findViewById(R.id.edit_activity_add_picture_button);
        addPicSwitcher.setInAnimation(EditActivity.this, android.R.anim.fade_in);
        addPicSwitcher.setOutAnimation(EditActivity.this, android.R.anim.fade_out);
        addPicSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView addPic = new TextView(EditActivity.this);
                addPic.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                addPic.setTextColor(Color.parseColor("#FFFFFF"));
                addPic.setGravity(Gravity.RIGHT);
                return addPic;
            }
        });
        addPicSwitcher.requestFocus();
        addPicSwitcher.requestFocusFromTouch();
        addPicSwitcher.setFocusable(true);
        addPicSwitcher.setFocusableInTouchMode(true);
        if(isNew == NEW){
            editText.requestFocus();
            editText.requestFocusFromTouch();
            addPicSwitcher.setOnClickListener(doneListener);
        }
        addPicSwitcher.setOnFocusChangeListener(addPicFocusListener);
    }

    private View.OnFocusChangeListener addPicFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if(b == true){
                isEditing = NOT_EDIT;
                showAddPicButton();
                addPicSwitcher.setOnClickListener(addPicListener);
                InputMethodManager methodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                methodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }else{
                isEditing = EDITING;
                showAddPicButton();
                addPicSwitcher.setOnClickListener(doneListener);
            }
        }
    };

    private View.OnClickListener addPicListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent forImage = new Intent(Intent.ACTION_GET_CONTENT);
            forImage.setType("image/*");
            startActivityForResult(forImage, 0);
        }
    };

    private View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            addPicSwitcher.requestFocus();
        }
    };

    private void showAddPicButton(){
        switch (isEditing){
            case NOT_EDIT:{
                addPicSwitcher.setText(getString(R.string.edit_activity_add_picture_button));
                break;
            }
            case EDITING:{
                addPicSwitcher.setText(getString(R.string.edit_activity_done));
                break;
            }
        }
    }

    //Note's jobs
    private void initialNote(){
        Intent sender = getIntent();
        localTime = sender.getStringExtra("time");
        content = sender.getStringExtra("content");
        pictures = sender.getStringArrayExtra("pictures");
        picName = sender.getStringArrayExtra("picName");
        key = sender.getStringExtra("key");
        if(TextUtils.isEmpty(content)) {
            isNew = NEW;
            isEditing = EDITING;
        }
    }

    private void getNote(){
        content = editText.getText().toString();
        note = new Note(content, key, pictures, picName);
        if(!TextUtils.isEmpty(localTime)){
            note.setTime(localTime);
        }
    }

    public void setPicName(String picture){
        if(picName != null) {
            String[] temp = new String[picName.length + 1];
            System.arraycopy(picName, 0, temp, 0, picName.length);
            temp[temp.length - 1] = picture;
            picName = new String[temp.length];
            System.arraycopy(temp, 0, picName, 0, temp.length);
        }else{
            picName = new String[1];
            picName[0] = picture;
        }
    }

    public void setPictures(String picture){
        if(pictures != null){
            String[] temp = new String[pictures.length + 1];
            System.arraycopy(pictures, 0, temp, 0, pictures.length);
            temp[temp.length - 1] = picture;
            pictures = new String[temp.length];
            System.arraycopy(temp, 0, pictures, 0, temp.length);
        }else{
            pictures = new String[1];
            pictures[0] = picture;
        }
        unbindService(uploadPicServiceConnection);
    }

    //Edit text's jobs
    private void initialEditText(){
        editText = (EditText)findViewById(R.id.edit_activity_note_edit);
        editText.setText(content);
    }
}
