package com.task.tangyifeng.notebookproject;

import android.app.Activity;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class EditActivity extends Activity implements PicCallBack{

    private static final int NOT_EDIT = 0;
    private static final int EDITING = 1;
    private static final int NEW = 0;
    private static final int NOT_NEW = 1;
    private static final int MSG_SET_PIC = 0;

    private EditText editText;
    private TextSwitcher addPicSwitcher;
    private RelativeLayout backButton;
    private LinearLayout editView;
    private ArrayList<Bitmap> bitmaps;

    private int count = 0;
    private Note note;
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
                    count ++;
                    setImage((Bitmap) msg.obj);
                    if(count == pictures.length)
                        unbindService(picConnection);
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

        //
        // FIXME: 16/7/14
        bitmaps = new ArrayList<>();
        //
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
        initialEditText();
        initialAddPicButton();
        showAddPicButton();
        initialBackButton();
        initialImage();
    }
    
    private void initialImage(){
        picIntent = new Intent(EditActivity.this, LoadPicService.class);
        initialLoadPicService();
        startService(picIntent);
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

    private void setImage(Bitmap resource){
        ImageView addImage = new ImageView(EditActivity.this);
        editView = (LinearLayout)findViewById(R.id.edit_activity_edit_view);
        addImage.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addImage.setPadding(transDpToPx(20),transDpToPx(15),transDpToPx(20), transDpToPx(15));
        addImage.setImageBitmap(resource);
        editView.setGravity(Gravity.CENTER_HORIZONTAL);
        editView.addView(addImage);
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
