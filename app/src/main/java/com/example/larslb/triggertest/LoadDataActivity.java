package com.example.larslb.triggertest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

public class LoadDataActivity extends Activity {
    private static final String TAG = LoadDataActivity.class.getSimpleName();

    private ListView mLoadList;
    private TextView mSelectText;
    private File rootDir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);

        mLoadList = (ListView) findViewById(R.id.Load_list);
        mSelectText = (TextView) findViewById(R.id.select_name);

        rootDir = getDir("Shootings",MODE_PRIVATE);
        String[] filenames = rootDir.list();
        Log.d(TAG,"List of Directories/Files --> " + filenames.length);
        /*if (rootDir.list() == null){
            Log.d(TAG,"Directory Empty");
            mSelectText.setText(R.string.no_data);
            finish();
        }*/
    }

}
