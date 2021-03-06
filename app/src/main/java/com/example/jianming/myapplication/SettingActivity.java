package com.example.jianming.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;

import com.example.jianming.Utils.EnvArgs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.ip_edit)
    public EditText ipEditText;

    @BindView(R.id.port_edit)
    public EditText portEditText;

//    @BindView(R.id.tl_custom)
//    Toolbar toolbar;

    @OnClick(R.id.button_done)
    public void doDoneBtn() {
        String ip = ipEditText.getText().toString();
        String port = portEditText.getText().toString();
        EnvArgs.serverIP = ip;
        EnvArgs.serverPort = port;
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

//        setSupportActionBar(toolbar);

//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        ipEditText.setFocusable(true);
        ipEditText.setFocusableInTouchMode(true);
        ipEditText.requestFocus();
        ipEditText.requestFocusFromTouch();
    }





}
