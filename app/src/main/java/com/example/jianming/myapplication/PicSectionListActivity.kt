package com.example.jianming.myapplication

import SERVER_IP
import SERVER_PORT
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room.databaseBuilder
import com.example.jianming.Tasks.ConcurrencyJsonApiTask
import com.example.jianming.util.AppDataBase
import com.example.jianming.util.NetworkUtil
import com.example.jianming.util.TimeUtil
import com.example.jianming.beans.PicSectionBean
import com.example.jianming.beans.PicSectionData
import com.example.jianming.beans.UpdateStamp
import com.example.jianming.dao.PicSectionDao
import com.example.jianming.dao.PicInfoDao
import com.example.jianming.dao.UpdataStampDao
import com.example.jianming.listAdapters.PicSectionListAdapter
import com.example.jianming.listAdapters.PicSectionListAdapter.CounterProvider
import com.example.jianming.services.Counter
import com.example.jianming.services.DownloadService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.nanjing.knightingal.processerlib.RefreshListener

class PicSectionListActivity : AppCompatActivity(), RefreshListener {


    private val TAG = "PicSectionListActivityMD"
    private lateinit var db: AppDataBase

    private lateinit var picSectionDao: PicSectionDao

    private lateinit var picInfoDao: PicInfoDao

    private lateinit var updataStampDao: UpdataStampDao


    private lateinit var picSectionListAdapter: PicSectionListAdapter

    private lateinit var listView: RecyclerView

    private var picSectionDataList: MutableList<PicSectionData> = mutableListOf();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val offline = intent.getBooleanExtra("offline", false)
        isNotExistItemShown = !offline

        db = databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "database-flow1000"
        ).allowMainThreadQueries().build()

        picSectionDao = db.picSectionDao()
        picInfoDao = db.picInfoDao()
        updataStampDao = db.updateStampDao()

        setContentView(R.layout.activity_pic_section_list_activity_md)


        listView = findViewById(R.id.list_view11)
        listView.setHasFixedSize(true)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        listView.layoutManager = mLayoutManager

        picSectionListAdapter =
            PicSectionListAdapter(this, counterProvider)
        picSectionListAdapter.setDataArray(picSectionDataList)
        listView.adapter = picSectionListAdapter


    }

    val counterProvider: CounterProvider =
        CounterProvider { sectionId -> downLoadService?.processCounter?.get(sectionId) }

    var downLoadService: DownloadService? = null


    var isBound = false
    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "onServiceConnected")
            isBound = true
            downLoadService = (binder as DownloadService.LocalBinder).getService()
            downLoadService!!.setRefreshListener(
                this@PicSectionListActivity
            )
            if (isNotExistItemShown && NetworkUtil.isNetworkAvailable(this@PicSectionListActivity)) {
                startDownloadPicIndex()
            } else {
                refreshFrontPage.invoke()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            downLoadService = null
            isBound = false
        }
    }

    override fun onPause() {
        super.onPause()
        downLoadService?.removeRefreshListener()
        downLoadService = null
        unbindService(conn)
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, DownloadService::class.java), conn, BIND_AUTO_CREATE)
    }

    private fun startDownloadPicIndex() {
        val updateStamp = updataStampDao.getUpdateStampByTableName("PIC_ALBUM_BEAN") as UpdateStamp
        val stringUrl = "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?time_stamp=${updateStamp.updateStamp}"
        Log.d("startDownloadWebPage", stringUrl)
        ConcurrencyJsonApiTask.startDownload(stringUrl) { allBody ->
            val mapper = jacksonObjectMapper()
            db.runInTransaction() {
                updateStamp.updateStamp = TimeUtil.currentTimeFormat()
                updataStampDao.update(updateStamp)
                val picSectionBeanList: List<PicSectionBean> = mapper.readValue(allBody)
                picSectionBeanList.forEach { picSectionDao.insert(it) }
            }

            refreshFrontPage.invoke()

            val pendingUrl = "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?client_status=PENDING"
            ConcurrencyJsonApiTask.startDownload(pendingUrl) { pendingBody ->
                val picSectionBeanList: List<PicSectionBean> = mapper.readValue(pendingBody)
                if (picSectionBeanList.isNotEmpty()) {
                    picSectionBeanList.forEach {
                        picSectionDao.update(it)
                        asyncStartDownload(it.id, picSectionDataList.indexOf(picSectionDataList.stream().filter { item ->
                            item.picSectionBean.id == it.id
                        }.findFirst().get()));
                    }
                }
            }
            val localUrl = "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?client_status=LOCAL"
            ConcurrencyJsonApiTask.startDownload(localUrl) { pendingBody ->
                val picSectionBeanList: List<PicSectionBean> = mapper.readValue(pendingBody)
                if (picSectionBeanList.isNotEmpty()) {
                    picSectionBeanList.forEach {
                        val existSection = picSectionDao.getByInnerIndex(it.id)
                        if (existSection.exist != 1) {
                            picSectionDao.update(it)
                            asyncStartDownload(
                                it.id,
                                picSectionDataList.indexOf(picSectionDataList.stream().filter { item ->
                                    item.picSectionBean.id == it.id
                                }.findFirst().get())
                            );
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private val refreshFrontPage: () -> Unit = {
        picSectionDataList.clear()
        val picSectionBeanList = getDataSourceFromJsonFile()
        for (picSectionBean in picSectionBeanList) {
            val picSectionData = PicSectionData(picSectionBean)
            picSectionDataList.add(picSectionData)
        }
        picSectionListAdapter.notifyDataSetChanged()

    }

    private var isNotExistItemShown = true

    private fun getDataSourceFromJsonFile(): List<PicSectionBean> {
        return if (isNotExistItemShown && NetworkUtil.isNetworkAvailable(this)) {
            picSectionDao.getAll().toList()
        } else {
            picSectionDao.getAllExist().toList()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun doRefreshProcess(sectionId: Long, position: Int, currCount: Int, max: Int) {
        val viewHolder =
            listView.findViewHolderForAdapterPosition(position) as PicSectionListAdapter.ViewHolder?
        if (currCount == max) {
            picSectionDataList[position].picSectionBean.exist = 1
            picSectionDao.update(picSectionDataList[position].picSectionBean)
            picSectionListAdapter.notifyDataSetChanged()
        }
        if (viewHolder != null) {
            MainScope().launch {
                viewHolder.downloadProcessBar.visibility = View.VISIBLE
                viewHolder.downloadProcessBar.isIndeterminate = false
                viewHolder.downloadProcessBar.setProgressCompat(currCount, false)
                viewHolder.downloadProcessBar.max = max
                Log.d(TAG, "current = $currCount max = $max")
            }
        }
    }

    override fun doRefreshList(picSectionBeanList: List<PicSectionBean>) {
        TODO("Not yet implemented")
    }

    override fun notifyListReady() {
        TODO("Not yet implemented")
    }


    fun asyncStartDownload(index: Long, position: Int) {
        downLoadService?.startDownloadSection(index, position)
    }

}