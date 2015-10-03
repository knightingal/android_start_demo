package com.example.jianming.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jianming.views.YImageSlider;
import com.example.jianming.views.YImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import com.example.jianming.Utils.DIOptionsNoneScaled;


public class XrxActivity extends Activity {

    private YImageSlider mImageSlider;
    private YImageView mImageContentView;
    private ImageView hideLeft, hideRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xrx);

        mImageSlider = (YImageSlider) findViewById(R.id.image);

        mImageContentView = mImageSlider.getContentView();
        hideLeft = mImageSlider.getHideLeft();
        hideRight = mImageSlider.getHideRight();

        String url = getIntent().getStringExtra("imgUrl");
        String imageUrl;
        if (url == null || url.equals("")) {
            //String sdcard = Environment.getExternalStorageDirectory().getPath();
            String imagePath = "/storage/sdcard1/BaiduNetdisk/xrx/[PureJapan]Vivian_Hsu/135.JPG";
            //String imageUrl = ImageDownloader.Scheme.FILE.wrap(imagePath);
            imageUrl = ImageDownloader.Scheme.DRAWABLE.wrap(R.drawable.su27long + "");
        }
        else {
            imageUrl = url;
        }
        Log.d("onCreate", "imageUrl = " + imageUrl);
        DisplayImageOptions options = DIOptionsNoneScaled.getInstance().getOptions();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_xrx, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.show_pic_size) {
            Toast.makeText(this, mImageContentView.picSize(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
