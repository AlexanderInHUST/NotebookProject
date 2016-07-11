package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int NOT_LOAD = 0;
    private static final int LOADING = 1;
    private static final int LOADED = 2;
    private static final int NOT_EDIT = 0;
    private static final int EDITING = 1;
    private static final int CLICKED = 2;

    private TextSwitcher mainActivityLabel;
    private TextSwitcher edit;
    private ImageSwitcher cloudButton;
    private ListView notesListView;

    private List<Note> notes;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter notesAdapter;
    private int isLoading = NOT_LOAD;
    private int isEditing = NOT_EDIT;

    int test = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialViews();

        //something to do with edit button


        /*AVObject testObject = new AVObject("TestObject");
        //testObject.put("words","Hello World!");
        testObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e == null){
                    Log.d("saved","success!");
                }
            }
        });*/

    }

    //initial all views
    private void initialViews(){
        initialLabel();
        initialCloudButton();
        initialNotes();
        initialEdit();
    }

    //Edit's jobs
    private void showEdit(){
        switch (isEditing){
            case NOT_EDIT:{
                edit.setText(getString(R.string.main_activity_edit_button));
                break;
            }
            case EDITING:{
                edit.setText(getString(R.string.main_activity_cancel_button));
                break;
            }
            case CLICKED:{
                edit.setText(getString(R.string.main_activity_delete_button));
                break;
            }
        }
    }

    private void initialEdit(){
        edit = (TextSwitcher)findViewById(R.id.main_activity_edit);
        edit.setInAnimation(this,android.R.anim.fade_in);
        edit.setOutAnimation(this,android.R.anim.fade_out);
        edit.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView editTextView = new TextView(MainActivity.this);
                editTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                editTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                editTextView.setTextColor(Color.parseColor("#FFFFFF"));
                editTextView.setText(getString(R.string.main_activity_edit_button));
                return editTextView;
            }
        });
        edit.setOnClickListener(editListener);
    }

    private View.OnClickListener editListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isEditing = EDITING;
            showEdit();
            notesListView.setOnItemClickListener(listViewListener);
            edit.setOnClickListener(editingListener);
            //
            //
            //
        }
    };

    private View.OnClickListener editingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isEditing = NOT_EDIT;
            showEdit();
            notesListView.setOnItemClickListener(null);
            edit.setOnClickListener(editListener);
            //
            //
            //
        }
    };

    private View.OnClickListener clickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //
            //
            //
        }
    };

    //Note's jobs
    private List<Integer> clicked;

    private void initialNotes(){
        dataList = new ArrayList<Map<String, Object>>();
        setNotes();
        clicked = new ArrayList<Integer>();
        notesListView = (ListView)findViewById(R.id.main_activity_contents);
        notesAdapter = new SimpleAdapter(this, dataList, R.layout.note_listview_layout,
                new String[]{"title","date","des"},
                new int[]{R.id.note_list_view_title, R.id.note_list_view_date, R.id.note_list_view_des});
        notesListView.setAdapter(notesAdapter);
    }

    private void setNotes(){
        //
        //get notes from somewhere
        //
        notes = new ArrayList<Note>();
        notes.add(new Note("asdb","grihjeoirjhoieytmhklwrretewebgfnftdafregrthetrhertgerwfwergtgrihjeoirjhoieytmhklwrretewebgfnftdafregrthetrhertgerwfwergtyuykyuiuylyuykyuiuyl"));
        notes.add(new Note("fhgrhtyrj","boriwjboiwmeklrnwkjgtrkjbhkjertgrihjeoirjhoieytmhklwrretewebgfnftdafregrthetrhertgerwfwergtyuykyuiuylhwrth"));
        //
        //

        for(Note n: notes){
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("title", n.getTitle());
            item.put("date", n.getTime());
            item.put("des", n.getDescription());
            dataList.add(item);
        }
    }

    private AdapterView.OnItemClickListener listViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            View itemView = view;
            if(!clicked.contains(Integer.valueOf(i))) {
                itemView.setBackgroundColor(Color.parseColor("#FFFACD"));
                isEditing = CLICKED;
                showEdit();
                edit.setOnClickListener(clickedListener);
                clicked.add(Integer.valueOf(i));
                //
                //do something more
                //
            }else{
                itemView.setBackgroundColor(Color.parseColor("#FFFFF0"));
                clicked.remove(Integer.valueOf(i));
                if(clicked.isEmpty()) {
                    isEditing = EDITING;
                    showEdit();
                    edit.setOnClickListener(editingListener);
                }
                //
                //do something more
                //
            }
        }
    };

    //label's jobs
    private void showLabel(){
        if(isLoading == LOADING){
            mainActivityLabel.setText(getString(R.string.main_activity_label_loading));
        }else{
            mainActivityLabel.setText(getString(R.string.main_activity_label));
        }
    }

    private void initialLabel(){
        mainActivityLabel = (TextSwitcher)findViewById(R.id.main_activity_label);
        mainActivityLabel.setInAnimation(this,android.R.anim.fade_in);
        mainActivityLabel.setOutAnimation(this,android.R.anim.fade_out);
        mainActivityLabel.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView label = new TextView(MainActivity.this);
                label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                label.setGravity(Gravity.CENTER_HORIZONTAL);
                label.setTextColor(Color.parseColor("#FFFFFF"));
                label.setText(getString(R.string.main_activity_label));
                return label;
            }
        });
    }

    //cloud button's job
    private void showCloudButton(){
        switch (isLoading){
            case NOT_LOAD:{
                cloudButton.setImageResource(R.drawable.cloud_button);
                break;
            }
            case LOADING:{
                cloudButton.setImageResource(R.drawable.cloud_button_loading);
                break;
            }
            case LOADED:{
                cloudButton.setImageResource(R.drawable.cloud_button_done);
                break;
            }
        }
    }

    private void initialCloudButton(){
        cloudButton = (ImageSwitcher)findViewById(R.id.main_activity_cloud_button);
        cloudButton.setInAnimation(this,android.R.anim.fade_in);
        cloudButton.setOutAnimation(this,android.R.anim.fade_out);
        cloudButton.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView cloud = new ImageView(MainActivity.this);
                cloud.setImageResource(R.drawable.cloud_button);
                return cloud;
            }
        });
    }

}
