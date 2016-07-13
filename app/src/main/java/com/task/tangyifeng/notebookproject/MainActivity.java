package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.text.TextUtils;
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
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import dalvik.annotation.TestTarget;

public class MainActivity extends Activity implements CallBack {

    private static final int NOT_LOAD = 0;
    private static final int LOADING = 1;
    private static final int LOADED = 2;
    private static final int NOT_EDIT = 0;
    private static final int EDITING = 1;
    private static final int CLICKED = 2;
    private static final int MSG_LOADING = 0;
    private static final int MSG_LOADED = 1;
    private static final int MSG_FLASH = 2;

    private TextSwitcher mainActivityLabel;
    private TextSwitcher edit;
    private ImageSwitcher cloudButton;
    private ListView notesListView;
    private ImageView addNew;

    private List<Note> notes;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter notesAdapter;
    private int isLoading = NOT_LOAD;
    private int isEditing = NOT_EDIT;

    private Intent cloudIntent;
    private AlarmManager startManager;
    private CloudService cloudService;
    private ServiceConnection cloudConnection;
    private CloudService.msgBinder cloudBinder;
    private Handler loadHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_LOADING:{
                    isLoading = LOADING;
                    showLabel();
                    showCloudButton();
                    Log.d("loading","what?");
                    break;
                }
                case MSG_LOADED:{
                    isLoading = LOADED;
                    showLabel();
                    showCloudButton();
                    notesListView.setAdapter(notesAdapter);
                    notesListView.setOnItemClickListener(editNoteListener);
                    showCount();
                    Log.d("loaded","set");
                    break;
                }
                case MSG_FLASH:{
                    initialNotes();
                    break;
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialViews();

        

    }

    @Override
    public void sendLoadedMsg(){
        Message msg = new Message();
        msg.what = MSG_LOADED;
        loadHandler.sendMessage(msg);
    }

    @Override
    public void sendLoadingMsg(){
        Message msg = new Message();
        msg.what = MSG_LOADING;
        loadHandler.sendMessage(msg);
    }

    //initial all views
    private void initialViews(){
        initialLabel();
        initialCloudButton();
        initialNotes();
        initialEdit();
        initialAddNew();
    }

    //Add new's job
    private void initialAddNew(){
        addNew = (ImageView)findViewById(R.id.main_activity_add_button);
        addNew.setOnClickListener(addNewListener);
    }

    private View.OnClickListener addNewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addNew = new Intent(MainActivity.this, EditActivity.class);
            startActivity(addNew);
            overridePendingTransition(R.anim.from_right, R.anim.to_left);
            finish();
        }
    };

    //Count's jobs
    private void showCount(){
        TextView count = (TextView)findViewById(R.id.main_activity_count_text);
        count.setText(String.format(Locale.CHINA, "%d个备忘录", dataList.size()));
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
        edit.setInAnimation(MainActivity.this, android.R.anim.fade_in);
        edit.setOutAnimation(MainActivity.this, android.R.anim.fade_out);
        edit.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView editTextView = new TextView(MainActivity.this);
                editTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                editTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                editTextView.setGravity(Gravity.CENTER_VERTICAL);
                editTextView.setTextColor(Color.parseColor("#FFFFFF"));
                editTextView.setText(getString(R.string.main_activity_edit_button));
                return editTextView;
            }
        });
        edit.setOnClickListener(editListener);
    }

    //now cancel
    private View.OnClickListener editListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isEditing = EDITING;
            showEdit();
            notesListView.setOnItemClickListener(listViewListener);
            edit.setOnClickListener(editingListener);

        }
    };

    //now edit
    private View.OnClickListener editingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isEditing = NOT_EDIT;
            showEdit();
            notesListView.setOnItemClickListener(editNoteListener);
            edit.setOnClickListener(editListener);
        }
    };

    //now delete
    private View.OnClickListener clickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            for(Integer item : clicked){
                dataList.remove(dataList.get(item));
                notes.remove(notes.get(item));
            }
            clicked.clear();
            isEditing = NOT_EDIT;
            showEdit();
            notesListView.setOnItemClickListener(editNoteListener);
            edit.setOnClickListener(editListener);
            edit.setBackgroundColor(Color.parseColor("#F0A986"));
            notesListView.setAdapter(notesAdapter);
            //
            //// // FIXME: 16/7/12
            //delete
            //
        }
    };

    //Note's jobs
    private List<Integer> clicked;

    private void initialNotes(){
        dataList = new ArrayList<Map<String, Object>>();
        notesListView = (ListView)findViewById(R.id.main_activity_contents);
        notes = new ArrayList<Note>();
        initialCloudService();
        startService(cloudIntent);
    }

    public void setNotes(ArrayList<Note> currentNotes){
        notes = currentNotes;
        for(Note n: notes){
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("title", n.getTitle());
            item.put("date", n.getTime());
            item.put("des", n.getDescription());
            dataList.add(item);
        }
        while(dataList == null) ;
        clicked = new ArrayList<Integer>();
        notesAdapter = new SimpleAdapter(this, dataList, R.layout.note_listview_layout,
                new String[]{"title","date","des"},
                new int[]{R.id.note_list_view_title, R.id.note_list_view_date, R.id.note_list_view_des});
        sendLoadedMsg();
        unbindService(cloudConnection);
    }

    private void initialCloudService(){
        cloudIntent = new Intent(MainActivity.this, CloudService.class);
        cloudConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                cloudBinder = (CloudService.msgBinder) iBinder;
                cloudService =(CloudService) cloudBinder.getService();
                cloudService.setCallBack(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        bindService(cloudIntent, cloudConnection, BIND_AUTO_CREATE);
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
                edit.setBackgroundColor(Color.parseColor("#6A4028"));
                clicked.add(Integer.valueOf(i));
            }else{
                itemView.setBackgroundColor(Color.parseColor("#FFFFF0"));
                clicked.remove(Integer.valueOf(i));
                if(clicked.isEmpty()) {
                    isEditing = EDITING;
                    showEdit();
                    edit.setOnClickListener(editingListener);
                    edit.setBackgroundColor(Color.parseColor("#F0A986"));
                }
            }
        }
    };

    private AdapterView.OnItemClickListener editNoteListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent editIntent = new Intent(MainActivity.this, EditActivity.class);
            editIntent.putExtra("time",notes.get(i).getTime());
            editIntent.putExtra("content",notes.get(i).getContent());
            editIntent.putExtra("pictures",notes.get(i).getPictures());
            editIntent.putExtra("key",notes.get(i).getKey());
            startActivity(editIntent);
            overridePendingTransition(R.anim.from_right, R.anim.to_left);
            finish();
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
