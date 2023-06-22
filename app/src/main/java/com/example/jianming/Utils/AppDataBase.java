package com.example.jianming.Utils;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.jianming.beans.PicAlbumBean;
import com.example.jianming.beans.PicInfoBean;
import com.example.jianming.beans.UpdateStamp;
import com.example.jianming.dao.PicAlbumDao;
import com.example.jianming.dao.PicInfoDao;
import com.example.jianming.dao.UpdataStampDao;

@Database(entities = {UpdateStamp.class}, version = 2)
public abstract class AppDataBase extends RoomDatabase {
    public PicAlbumDao picAlbumDao() {return null;}

    public PicInfoDao picInfoDao() {return null;}

    public abstract UpdataStampDao updataStampDao();
}
