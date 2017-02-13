package com.example.larslb.triggertest;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class GraphingActivity extends Activity {
    private static final String TAG = GraphingActivity.class.getSimpleName();

    String mDeviceName;
    String mDeviceAddress;
    TextView mTextDeviceName;
    TextView mTextDeviceAddress;


    ArrayList<Entry> mGraphSeries;
    ArrayList<String> mList;
    ArrayList<Integer> mTime;
    LineChart mGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphing);
        Log.d(TAG,"onCreate!");

        Intent intent = getIntent();
        mList = intent.getStringArrayListExtra("Data");
        mTime = intent.getIntegerArrayListExtra("Time");


        mGraph = (LineChart) findViewById(R.id.graph);
        mGraphSeries = makeList();
        XAxis xAxis = mGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(0);
        xAxis.setAxisMaximum(4);


        CreateView();

    }

    public ArrayList<Entry> makeList(){
        ArrayList<Entry> dataPointLineGraphSeries = new ArrayList<>();
        Entry p;
        float max_data = 0;
        for (int i = 0; i < mList.size();i++){
            float data = Float.parseFloat(mList.get(i));
            float t = mTime.get(i)/1000;
            Log.d(TAG,"DATA: " + data + "    TIMESTAMP: " + t);
            if (data > max_data){
                max_data = data;
            }

            p = new Entry(t,data);
            dataPointLineGraphSeries.add(p);
        }

        return dataPointLineGraphSeries;
    }




    public void CreateView(){
        Log.d(TAG,"CreateView");


        LineDataSet dataSet = new LineDataSet(mGraphSeries,"Data");
        LineData lineData = new LineData(dataSet);
        mGraph.setData(lineData);
        mGraph.invalidate();
    }


}
