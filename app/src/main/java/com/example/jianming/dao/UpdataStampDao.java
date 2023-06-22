package com.example.jianming.dao;


import com.example.jianming.beans.UpdateStamp;

import java.util.List;

//@Dao
public interface UpdataStampDao {
//    @Query("select * from UpdateStamp where tableName = :tableName")
    UpdateStamp getUpdateStampByTableName(String tableName);

//    @Update
    void update(UpdateStamp updateStamp);

//    @Insert
    void save(UpdateStamp albumStamp);

//    @Delete
    void deleteAll(List<UpdateStamp> updateStampList);
}
