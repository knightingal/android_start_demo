package com.example.jianming.listAdapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.jianming.beans.PicInfoBean;
import com.example.jianming.myapplication.PicAlbumActivity;
import com.example.jianming.myapplication.R;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.List;


public class PicAlbumContentAdapter extends BaseAdapter {
    private LayoutInflater mInflater;

    private List<PicInfoBean> dataArray;

    PicAlbumActivity context;

    int sreamWidth;

    public PicAlbumContentAdapter(PicAlbumActivity context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.sreamWidth = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
    }

    public void setDataArray(List<PicInfoBean> dataArray) {
        this.dataArray = dataArray;
    }


    @Override
    public int getCount() {
        return dataArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.vlist, parent, false);
            holder.img = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String imgUrl = ImageDownloader.Scheme.FILE.wrap((String) dataArray.get(position).getAbsolutePath());
        int width = dataArray.get(position).getWidth();
        int height = dataArray.get(position).getHeight();

        float div = (float)height / (float)width;

        ViewGroup.LayoutParams lp = holder.img.getLayoutParams();

        lp.height = (int)(div * (float)sreamWidth);
        lp.width = sreamWidth;

        holder.img.setLayoutParams(lp);
        holder.img.setImageBitmap(BitmapFactory.decodeFile(dataArray.get(position).getAbsolutePath()));
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Activity4List", (String) dataArray.get(position).getAbsolutePath());
                String imgs[] = new String[dataArray.size()];
                for (int i = 0; i < dataArray.size(); i++) {
                    imgs[i] = dataArray.get(i).getAbsolutePath();
                }

                context.startPicContentActivity(imgs, position);
            }
        });

        return convertView;
    }

    public final class ViewHolder {
        public ImageView img;
    }
}