package com.example.jianming.services

import SERVER_IP
import SERVER_PORT
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.room.Room
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.example.jianming.Tasks.ConcurrencyJsonApiTask
import com.example.jianming.beans.PicInfoBean
import com.example.jianming.util.AppDataBase
import com.example.jianming.beans.PicSectionBean
import com.example.jianming.beans.PicSectionData
import com.example.jianming.beans.SectionInfoBean
import com.example.jianming.beans.UpdateStamp
import com.example.jianming.dao.PicSectionDao
import com.example.jianming.dao.PicInfoDao
import com.example.jianming.dao.UpdataStampDao
import com.example.jianming.myapplication.getSectionConfig
import com.example.jianming.util.Decryptor
import com.example.jianming.util.FileUtil.getSectionStorageDir
import com.example.jianming.util.NetworkUtil
import com.example.jianming.util.TimeUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.Request
import org.nanjing.knightingal.processerlib.RefreshListener
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class DownloadService : Service() {
    companion object {
        var refreshListener: MutableSet<RefreshListener> = mutableSetOf()
        var pendingSectionBeanList: MutableList<PicSectionData> = mutableListOf()
        val workerQueue: BlockingQueue<PicSectionBean> = LinkedBlockingQueue()
        val existSectionId: MutableSet<Long> = mutableSetOf()
        val sectionThreadPool: ThreadPoolExecutor = ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
            LinkedBlockingQueue())
        val imageThreadPool: ThreadPoolExecutor = ThreadPoolExecutor(10, 10, 30, TimeUnit.SECONDS,
            LinkedBlockingQueue())

    }

    private val binder: IBinder = LocalBinder()

    private lateinit var db: AppDataBase
    private lateinit var picSectionDao : PicSectionDao
    private lateinit var updateStampDao: UpdataStampDao
    private lateinit var picInfoDao : PicInfoDao



    fun setRefreshListener(refreshListener: RefreshListener?) {
        if (refreshListener != null) {
            DownloadService.refreshListener.add(refreshListener)
        }
    }


    fun removeRefreshListener(refreshListener: RefreshListener) {
        DownloadService.refreshListener.remove(refreshListener)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "database-flow1000"
        ).allowMainThreadQueries().build()
        picSectionDao = db.picSectionDao()
        picInfoDao = db.picInfoDao()
        updateStampDao = db.updateStampDao()
    }
    private var allPicSectionBeanList:List<PicSectionData> = listOf()

    private fun checkSectionWorkerExist(sectionId: Long): Boolean {
        if (existSectionId.contains(sectionId)) {
            return true
        }
        val picSectionBean = picSectionDao.getByServerIndex(sectionId)
        return picSectionBean.exist == 1
    }

    fun fetchAllSectionList() {
        val updateStamp = updateStampDao.getUpdateStampByTableName("PIC_ALBUM_BEAN") as UpdateStamp
        val stringUrl = "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?time_stamp=${updateStamp.updateStamp}"
        Log.d("startDownloadWebPage", stringUrl)
        ConcurrencyJsonApiTask.startGet(stringUrl) { allBody ->
            val mapper = jacksonObjectMapper()
            db.runInTransaction {
                updateStamp.updateStamp = TimeUtil.currentTimeFormat()
                val picSectionBeanList: List<PicSectionBean> = mapper.readValue(allBody)
                updateStampDao.update(updateStamp)
                picSectionBeanList.forEach { picSectionDao.insert(it) }
            }

            allPicSectionBeanList = picSectionDao.getAll().toList()
                .map { bean -> PicSectionData(bean, 0).apply { this.process = 0 } }
            refreshListener.forEach {
                it.notifyListReady()
            }
        }

    }

    fun startDownloadSectionList() {
        thread {
            val mapper = jacksonObjectMapper()
            val updateStamp = updateStampDao.getUpdateStampByTableName("PIC_ALBUM_BEAN") as UpdateStamp
            val stringUrl = "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?time_stamp=${updateStamp.updateStamp}"
            Log.d("startDownloadWebPage", stringUrl)

            var request = Request.Builder().url(stringUrl).build()

            var body = NetworkUtil.okHttpClient.newCall(request).execute().body.string()

            db.runInTransaction {
                updateStamp.updateStamp = TimeUtil.currentTimeFormat()
                val picSectionBeanList: List<PicSectionBean> = mapper.readValue(body)
                updateStampDao.update(updateStamp)
                picSectionBeanList.forEach { picSectionDao.insert(it) }
            }
            allPicSectionBeanList = picSectionDao.getAll().toList().map { bean -> PicSectionData(bean, 0).apply { this.process = 0 } }
            val pendingUrl =
                "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?client_status=PENDING"

            val picIndexResp = NetworkUtil.okHttpClient.newCall(Request.Builder().url(pendingUrl).build()).execute().body.string()
            var picSectionBeanList: List<PicSectionBean> = mapper.readValue(picIndexResp)
            pendingSectionBeanList = mutableListOf<PicSectionData>()

            db.runInTransaction {
                picSectionBeanList = picSectionBeanList.filter {
                    val byServerIndex = picSectionDao.getByServerIndex(it.id)
                    byServerIndex.clientStatus != PicSectionBean.ClientStatus.LOCAL
                }
            }

            pendingSectionBeanList.addAll(picSectionBeanList.map { bean -> PicSectionData(bean, 0).apply { this.process = 0 } })
            pendingSectionBeanList.sortBy { it.picSectionBean.id }
            db.runInTransaction {
                pendingSectionBeanList.forEach {
                    picSectionDao.updateClientStatusByServerIndex(
                        it.picSectionBean.id,
                        PicSectionBean.ClientStatus.PENDING
                    )
                }
            }
            MainScope().launch {
                refreshListener.forEach {
                    it.notifyListReady()
                }
            }
            pendingSectionBeanList.forEach { pendingSectionBean->
                val clientStatus = picSectionDao.getByServerIndex(pendingSectionBean.picSectionBean.id).clientStatus
                if (clientStatus == PicSectionBean.ClientStatus.LOCAL) {
                    return@forEach
                }
                val sectionConfig = getSectionConfig(pendingSectionBean.picSectionBean.album)
                val url = "http://${SERVER_IP}:${SERVER_PORT}/local1000/picDetailAjax?id=${pendingSectionBean.picSectionBean.id}"
                sectionThreadPool.execute {
                    val picContentResp = NetworkUtil.okHttpClient.newCall(Request.Builder().url(url).build()).execute().body.string()
                    val sectionInfoBean = mapper.readValue<SectionInfoBean>(picContentResp)
                    if (ProcessCounter.initCounter(pendingSectionBean.picSectionBean.id, sectionInfoBean.pics.size) != null) {
                        return@execute
                    }
                    Log.d("DownloadService", "download section $url")
                    picInfoDao.deleteBySectionInnerIndex(pendingSectionBean.picSectionBean.id)
                    val latch = CountDownLatch(sectionInfoBean.pics.size)
                    sectionInfoBean.pics.forEach { pic ->
                        val imgUrl = "http://${SERVER_IP}:${SERVER_PORT}" +
                                "/linux1000/${sectionConfig.baseUrl}/${sectionInfoBean.dirName}/${if (sectionConfig.encryped) pic.name else pic.name}"
                        imageThreadPool.execute {
//                            Log.d("DownloadService", "download image $imgUrl")
                            var bytes = NetworkUtil.okHttpClient.newCall(Request.Builder().url(imgUrl).build()).execute().body.bytes()
                            val options: BitmapFactory.Options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            if (sectionConfig.encryped) {
                                bytes = Decryptor.decrypt(bytes)
                            }
                            val directory =
                                getSectionStorageDir(applicationContext, sectionInfoBean.dirName)
                            val dest = File(directory, pic.name)
                            val fileOutputStream = FileOutputStream(dest, true)
                            fileOutputStream.write(bytes)
                            fileOutputStream.close()
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                            val width = options.outWidth
                            val height = options.outHeight
                            val absolutePath = dest.absolutePath
                            val picInfoBean: PicInfoBean = PicInfoBean(
                                pic.id,
                                pic.name,
                                pendingSectionBean.picSectionBean.id,
                                absolutePath,
                                height,
                                width,
                            )
                            picInfoDao.insert(picInfoBean)
                            val currCounter = ProcessCounter.addCounter(pendingSectionBean.picSectionBean.id)
                            if (currCounter != null) {
                                refreshListener.forEach { it.doRefreshProcess(pendingSectionBean.picSectionBean.id, 0, currCounter.getProcess(), currCounter.max) }
                            }
                            latch.countDown()
                        }
                    }
                    latch.await()
                    refreshListener.forEach { it.doRefreshProcess(pendingSectionBean.picSectionBean.id, 0, sectionInfoBean.pics.size, sectionInfoBean.pics.size) }
                    picSectionDao.updateClientStatusByServerIndex(
                        pendingSectionBean.picSectionBean.id,
                        PicSectionBean.ClientStatus.LOCAL
                    )
                    ProcessCounter.remove(pendingSectionBean.picSectionBean.id)
                }
            }

        }

//        ConcurrencyJsonApiTask.startGet(stringUrl) { allBody ->
//            val mapper = jacksonObjectMapper()
//            db.runInTransaction {
//                updateStamp.updateStamp = TimeUtil.currentTimeFormat()
//                val picSectionBeanList: List<PicSectionBean> = mapper.readValue(allBody)
//                updateStampDao.update(updateStamp)
//                picSectionBeanList.forEach { picSectionDao.insert(it) }
//            }
//
//            allPicSectionBeanList = picSectionDao.getAll().toList().map { bean -> PicSectionData(bean, 0).apply { this.process = 0 } }
//
//            val pendingUrl =
//                "http://${SERVER_IP}:${SERVER_PORT}/local1000/picIndexAjax?client_status=PENDING"
//            val pendingJob = ConcurrencyJsonApiTask.startGet(pendingUrl) { pendingBody ->
//                var picSectionBeanList: List<PicSectionBean> = mapper.readValue(pendingBody)
//                val pendingSectionBeanList = mutableListOf<PicSectionData>()
//                pendingSectionBeanList.addAll(picSectionBeanList.map { bean -> PicSectionData(bean, 0).apply { this.process = 0 } })
//                pendingSectionBeanList.sortBy { it.picSectionBean.id }
//                db.runInTransaction {
//                    pendingSectionBeanList.forEach {
//                        picSectionDao.updateClientStatusByServerIndex(
//                            it.picSectionBean.id,
//                            PicSectionBean.ClientStatus.PENDING
//                        )
//                    }
//                }
//
//
//
//            }
//            MainScope().launch {
//                listOf(pendingJob, ).joinAll()
//                pendingSectionBeanList.sortBy { it.picSectionBean.id }
//
//                launch {
//                    db.runInTransaction {
//                        pendingSectionBeanList.forEach {
//                            picSectionDao.updateClientStatusByServerIndex(
//                                it.picSectionBean.id,
//                                PicSectionBean.ClientStatus.PENDING
//                            )
//                        }
//                    }
//                }
//
//                val workQuery = WorkQuery.Builder
//                    .fromStates(listOf(WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED))
//                    .addTags(listOf(
//                        "sectionTag"
//                    ))
//                    .build()
//
//                val workInfoList = WorkManager.getInstance(applicationContext)
//                    .getWorkInfos(workQuery).get()
//                val currentSectionCount = workInfoList.size
//                Log.i("DownloadService", "current section count $currentSectionCount")
//                var i = 0
//                while (i < 2 - currentSectionCount) {
//                    while (true) {
//                        val worker = workerQueue.poll()
//                        if (checkWorkerExist(worker)) {
//                            TaskManager.startWork(worker!!.id, applicationContext)
//                            break
//                        } else if (worker == null) {
//                            break
//                        }
//                    }
//                    i++
//                }
//                TaskManager.viewWork(applicationContext)
//            }
//            refreshListener.forEach {
//                it.notifyListReady()
//            }
//        }
    }

    fun getPendingSectionList(): List<PicSectionData> {
        return pendingSectionBeanList.toList()
    }

    fun getAllSectionList(): List<PicSectionData> {
        return allPicSectionBeanList
    }

    inner class LocalBinder : Binder() {
        fun getService(): DownloadService {
            return this@DownloadService
        }
    }

}


class Counter(val max: Int) {
    private val process: AtomicInteger = AtomicInteger(0)

    fun setProcess(value: Int) {
        process.set(value)
    }

    fun add() {
        process.incrementAndGet()
    }

    fun getProcess() = process.get()
}
