package com.example.larslb.triggertest;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static com.example.larslb.triggertest.R.styleable.MenuItem;

public class MainActivity extends AppCompatActivity {

    private String[] mNavTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Calendar calendar = new GregorianCalendar(Locale.GERMANY);
        Date trialTime = new Date();
        calendar.setTime(trialTime);

        int timeNow = calendar.get(Calendar.MILLISECOND);
        Log.d("MainActivity","Time: " + timeNow);

        mNavTitles = getResources().getStringArray(R.array.nav_titles_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item,mNavTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mTitle = mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open_drawer,R.string.close_drawer){

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //TODO: Implement Menu for drawer
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
        mDrawerToggle.onConfigurationChanged(configuration);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item){
        if (mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        switch (item.getItemId()){
            case R.id.action_websearch:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void selectItem(int pos) {

        String name = getResources().getStringArray(R.array.nav_titles_array)[pos];
        switch (name){
            case "HOME":
                mDrawerList.setItemChecked(pos,true);
                break;
            case "New Exercise":
                startScann();
                mDrawerList.setItemChecked(pos,true);
                break;
            case "Load Data":
                loadData();
                mDrawerList.setItemChecked(pos,true);
                break;
            case "Options":
                mDrawerList.setItemChecked(pos,true);
                break;
            default:
                mDrawerList.setItemChecked(pos,false);
                break;
        }
        setTitle(name);
        mDrawerLayout.closeDrawers();

    }

    public void setTitle(CharSequence title){
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
    public void loadData(){
        Intent loadIntent = new Intent(this,LoadDataActivity.class);
        startActivity(loadIntent);

    }

    public void startScann(){
        Intent scanIntent = new Intent(this,DeviceScanActivity.class);
        startActivity(scanIntent);
    }
}


