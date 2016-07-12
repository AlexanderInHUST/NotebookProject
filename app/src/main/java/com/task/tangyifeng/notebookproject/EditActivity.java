package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by tangyifeng on 16/7/12.
 */
public class EditActivity extends Activity {

    private static final int NOT_EDIT = 0;
    private static final int EDITING = 1;
    private static final int NEW = 0;
    private static final int NOT_NEW = 1;

    private EditText editText;
    private TextSwitcher addPicSwitcher;
    private RelativeLayout backButton;

    private Note note;
    private String content;
    private String[] pictures;
    private String localTime;

    private int isEditing = NOT_EDIT;
    private int isNew = NOT_NEW;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);
        initialViews();
    }

    @Override
    protected void onActivityResult(int requestId, int resultId, Intent data){
        super.onActivityResult(requestId, resultId, data);
        if(requestId == RESULT_OK){
            ContentResolver resolver = getContentResolver();
            Uri imageUri = data.getData();
            //// FIXME: 16/7/12 
        }
    }

    //initial views
    private void initialViews(){
        initialNote();
        initialEditText();
        initialAddPicButton();
        showAddPicButton();
        initialBackButton();
        //initialImage();
    }
    
    //Images' jobs
    //
    //// FIXME: 16/7/12 
    //
    //
    
    //Back button's jobs
    private void initialBackButton(){
        backButton = (RelativeLayout)findViewById(R.id.edit_activity_back_button);
        backButton.setOnClickListener(backButtonListener);
    }

    private View.OnClickListener backButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getNote();
            Intent backIntent = new Intent(EditActivity.this, MainActivity.class);
            backIntent.putExtra("content", note.getContent());
            backIntent.putExtra("pictures",note.getPictures());
            backIntent.putExtra("time",note.getTime());
            startActivity(backIntent);
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
        if(TextUtils.isEmpty(content)) {
            isNew = NEW;
            isEditing = EDITING;
        }
    }

    private void getNote(){
        content = editText.getText().toString();
        note = new Note(content, pictures);
        if(!TextUtils.isEmpty(localTime)){
            note.setTime(localTime);
        }
    }

    public void setPictures(String picture){
        String[] temp = new String[(pictures == null) ? 1 :pictures.length + 1];
        System.arraycopy(pictures, 0, temp, 0, pictures.length);
        temp[temp.length - 1] = picture;
        pictures = temp;
    }

    //Edit text's jobs
    private void initialEditText(){
        editText = (EditText)findViewById(R.id.edit_activity_note_edit);
        editText.setText(content);
    }
}
