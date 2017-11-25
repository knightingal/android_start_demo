package com.example.jianming.listAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jianming.Utils.EnvArgs;
import com.example.jianming.Utils.FileUtil;
import com.example.jianming.beans.PicAlbumBean;
import com.example.jianming.beans.PicAlbumData;
import com.example.jianming.myapplication.PicAlbumActivity;
import com.example.jianming.myapplication.PicAlbumListActivityMD;
import com.example.jianming.myapplication.R;

import org.nanjing.knightingal.processerlib.view.ProcessBar;

import java.io.File;
import java.util.List;

public class PicAlbumListAdapter extends RecyclerView.Adapter<PicAlbumListAdapter.ViewHolder> {
    private final static String TAG = "PicAlbumListAdapter";
    private List<PicAlbumData> dataArray;

    private Context context;

    public PicAlbumListAdapter(Context context) {
        this.context = context;
    }

    public void setDataArray(List<PicAlbumData> dataArray) {
        this.dataArray = dataArray;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pic_list_content, parent, false);

        ViewHolder vh = new ViewHolder(v);
        v.setTag(vh);
        return vh;

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.textView.setText(dataArray.get(position).getPicAlbumData().getName());
        if (dataArray.get(position).getPicAlbumData().getExist() == 1) {
            viewHolder.textView.setTextColor(Color.rgb(0, 255, 0));
            viewHolder.downloadProcessBar.setVisibility(View.INVISIBLE);
            viewHolder.exist = true;
        } else {
            viewHolder.textView.setTextColor(Color.rgb(0, 128, 0));
            viewHolder.downloadProcessBar.setVisibility(View.INVISIBLE);
            viewHolder.exist = false;
        }
        viewHolder.serverIndex = dataArray.get(position).getPicAlbumData().getServerIndex();
        viewHolder.localPosition = position;
        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "you clicked " + PicAlbumListAdapter.this.dataArray.get(position).getPicAlbumData().getName() + " delete_btn");
                AlertDialog.Builder builder = new AlertDialog.Builder(PicAlbumListAdapter.this.context);
                builder.setMessage("delete this dir?");
                builder.setTitle("");
                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FileUtil.removeDir(PicAlbumListAdapter.this.context, PicAlbumListAdapter.this.dataArray.get(position).getPicAlbumData().getName());
                        viewHolder.textView.setTextColor(Color.rgb(0, 128, 0));
                        PicAlbumBean.deletePicAlbumFromDb(viewHolder.serverIndex);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return this.dataArray.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;

        public ImageView deleteBtn;

        public int serverIndex;

        public int localPosition;

        public boolean exist = false;

        public ProcessBar downloadProcessBar;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.pic_text_view);
            this.deleteBtn = itemView.findViewById(R.id.delete_btn);
            this.downloadProcessBar = itemView.findViewById(R.id.customer_view1);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //PicAlbumListAdapter.ViewHolder holder = (PicAlbumListAdapter.ViewHolder) view.getTag();
            final String name = this.textView.getText().toString();
            int serverIndex = this.serverIndex;
            if (this.exist) {
                Log.i(TAG, "you click " + serverIndex + "th item, name = " + name);
                Intent intent = new Intent(context, PicAlbumActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("serverIndex", serverIndex);
                context.startActivity(intent);
            } else {
                this.downloadProcessBar.setVisibility(View.VISIBLE);
                File file = FileUtil.getAlbumStorageDir(context, name);
                if (file.mkdirs()) {
                    Log.i(TAG, file.getAbsolutePath() + " made");
                }
                int innerIndex = dataArray.get(getAdapterPosition())
                        .getPicAlbumData()
                        .getInnerIndex()
                        .intValue();
                ((PicAlbumListActivityMD)context).asyncStartDownload(innerIndex, getAdapterPosition());
            }
        }
    }
}
