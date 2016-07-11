package com.task.tangyifeng.notebookproject;

import android.app.Activity;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;

import org.w3c.dom.Text;

import java.util.Calendar;

public class MainActivity extends Activity {

    private static final int NOT_LOAD = 0;
    private static final int LOADING = 1;
    private static final int LOADED = 2;

    private TextSwitcher mainActivityLabel;
    private ImageSwitcher cloudButton;

    private int isLoading = NOT_LOAD;

    int test = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        initialViews();

        TextView edit = (TextView)findViewById(R.id.main_activity_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoading = (++test)%3;
                showLabel();
                showCloudButton();
                Calendar calendar = Calendar.getInstance();

                Log.d("calender",""+calendar.get(Calendar.YEAR));
            }
        });

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
    }

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
