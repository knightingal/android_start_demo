package com.example.jianming.myapplication;

import android.content.Intent;
import android.os.Bundle;

import com.example.jianming.util.AppDataBase;
import com.example.jianming.dao.PicSectionDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.view.MenuItem;
import android.widget.Toast;


import com.example.jianming.beans.UpdateStamp;



public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private PicSectionDao picSectionBeanDao;

    public void btnClicked(View v) {
        if (v.getId() == R.id.picIndexBtn) {
            this.startActivity(new Intent().setClassName(this, Local1KActivity.class.getName()));
        }

    }

    AppDataBase db;

    public Toolbar toolbar;

    public FloatingActionButton fab;

    public DrawerLayout drawer;

    public NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDataBase.class, "database-flow1000").allowMainThreadQueries().build();


        picSectionBeanDao = db.picSectionDao();
        setContentView(R.layout.activity_main2);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        findViewById(R.id.picIndexBtn).setOnClickListener(this::btnClicked);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        initDB();

    }

    private void initDB() {
        UpdateStamp sectionStamp = this.db.updateStampDao().getUpdateStampByTableName("PIC_ALBUM_BEAN");
        if (sectionStamp == null) {
            sectionStamp = new UpdateStamp();
            sectionStamp.setTableName("PIC_ALBUM_BEAN");
            sectionStamp.setUpdateStamp("20000101000000");
            this.db.updateStampDao().save(sectionStamp);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.settings) {
        } else if (id == R.id.clear_database) {
            clearDB();
            Toast.makeText(this, "DB cleared", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.set_timestamp) {
//            startActivity(new Intent(this, TimestampActivity.class));
        } else if (id == R.id.QR_code) {
            //QrcodeActivity
        } else if (id == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static final int I_CAP_ACTIVITY = 1;

    private void clearDB() {
        this.db.updateStampDao().deleteAll(null);
        picSectionBeanDao.deleteAll(null);
        initDB();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == I_CAP_ACTIVITY) {
            String url = data.getStringExtra("data");
            Toast.makeText(this, url, Toast.LENGTH_LONG).show();
        }

    }
}
