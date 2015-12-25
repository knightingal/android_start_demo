package com.example.jianming.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.example.jianming.services.DownloadService;
import com.example.jianming.views.YImageSlider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import com.example.jianming.Utils.DIOptionsNoneScaled;

import butterknife.Bind;
import butterknife.ButterKnife;


public class PicContentActivity extends Activity implements YImageSlider.ImgChangeListener {

    @Bind(R.id.image)
    public YImageSlider mImageSlider;

    private String[] imgArray;

    private int position;


    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    DownloadService mService;

    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            mService = binder.getService();
            mService.callFromActivity();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_content);

        ButterKnife.bind(this);

        mImageSlider.setImgChangeListener(this);
        imgArray = getIntent().getStringArrayExtra("imgArray");
        position = getIntent().getIntExtra("position", 0);
        if (imgArray != null && imgArray.length != 0) {
            index = position;
        }
        mImageSlider.setHideLeftSrc(index);
        mImageSlider.setContentSrc(index);
        mImageSlider.setHideRightSrc(index);
    }

    private String getImgByIndex(int index) {
        if (imgArray != null) {
            if (index >= 0 && index < imgArray.length) {
                return imgArray[index];
            } else {
                return null;
            }
        } else {
            if (index >= 0 && index < pics.length) {
                return pics[index] + "";
            } else {
                return null;
            }
        }
    }

    int index = 0;
    int pics[] = {R.drawable.f14_1, R.drawable.f14_2, R.drawable.f14_3, R.drawable.f14_4,};

    public String onGetBackImg(YImageSlider yImageSlider) {
        index--;
        return getImgSrcByIndex(index - 1, yImageSlider);
    }

    public String onGetNextImg(YImageSlider yImageSlider) {
        index++;
        return getImgSrcByIndex(index + 1, yImageSlider);
    }

    @Override
    public String getImgSrcByIndex(int index, YImageSlider yImageSlider) {
        String img = getImgByIndex(index);
        if (img != null) {
            if (imgArray != null) {
                return ImageDownloader.Scheme.FILE.wrap(img);
            } else {
                return ImageDownloader.Scheme.DRAWABLE.wrap(img);
            }
        } else {
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("position", index);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
