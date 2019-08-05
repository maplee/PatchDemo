package com.matt.patch.inner

import android.content.pm.PackageInfo
import android.text.TextUtils
import android.util.Log
import com.matt.downloader.openapi.DownloadCallback
import com.matt.downloader.openapi.DownloaderApi
import com.matt.patch.openapi.MergePatch
import com.matt.patch.openapi.PatchApi
import org.json.JSONObject
import java.io.File
import java.util.concurrent.*

/**
 * Author:Created by matt on 2019/4/16.
 * Email:jiagfone@163.com
 */
class PatchHelper {

    private val TAG = "PatchHelper"

    private var mExecutorService: ExecutorService? = null

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val THREAD_SIZE = Math.max(3, Math.min(CPU_COUNT - 1, 5))
    /**
     * 核心线程数
     */
    private val CORE_POOL_SIZE = THREAD_SIZE


    private object mHolder {
        val instance = PatchHelper()
    }

    companion object {
        fun getInstance(): PatchHelper {
            return mHolder.instance
        }
    }

    /**
     * 创建线程池
     *
     * @return mExecutorService
     */
    @Synchronized
    fun execute(runnable: Runnable) {
        if (mExecutorService == null) {
            mExecutorService = ThreadPoolExecutor(CORE_POOL_SIZE, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                SynchronousQueue(), ThreadFactory { r ->
                    val thread = Thread(r)
                    thread.isDaemon = false
                    thread
                })
        }
        mExecutorService?.execute(runnable);
    }


    fun stopDownload(apkUrl: String){
        DownloaderApi.stop(apkUrl)
    }

    fun asyncPatchDownload(apkUrl: String, packageName: String, version: Int, patchJson: String, callback: ICallback){
        if(DownloaderApi.containTask(apkUrl)){
            callback.fail("The task is exists")
            return
        }
        execute(Runnable {
            patchDownload(apkUrl, packageName, version, patchJson, callback)
        })
    }


    fun patchDownload(apkUrl: String, packageName: String, version: Int, patchJson: String, callback: ICallback) {
        //1.是否已安装，版本是否一致
        //3.版本不一致，获取当前版本是否有可更新patch包
        //4.有patch包，复制当前已安装包到固定路径，开始下载patch
        //4.1 合并patch到已安装包的apk,检验合成后的apk的md5与服务端给定的md5
        //5.无patch包，下载apk
        if(CompileConfig.DEBUG){
            Log.i("PatchHelper", "patchDownload, apkUrl = [${apkUrl}], packageName = [${packageName}], version = [${version}], patchJson = [${patchJson}], callback = [${callback}]")
        }
        var packageInfo: PackageInfo? = Utils.getInstalledApkPackageInfo(PatchApi.sConext, packageName)
        if (packageInfo == null) {
            download(apkUrl, callback)
            return
        }
        if (packageInfo.versionCode >= version) {
            callback.fail("The app is latest edition")
            return
        }


        var jsonObject = JSONObject(patchJson)
        var preVersionApkNameMd5: String = jsonObject.getString("preVersionApkNameMd5")
        // 复制到指定路径下
        var distDir = Utils.getDownloadPath().absolutePath + "/origin/"+packageName+packageInfo.versionName+".apk"
        Utils.copyFile(packageInfo.applicationInfo.sourceDir, distDir)
        var prePathMd5 = Utils.getFileMD5(File(distDir))
        if(CompileConfig.DEBUG){
            Log.i("PatchHelper", "patchDownload, preVersionApkNameMd5 = [${preVersionApkNameMd5}], oldFilePath = [${distDir}], prePathMd5 = [${prePathMd5}]")
        }
        if (!TextUtils.equals(prePathMd5, preVersionApkNameMd5)) {
            // verify old file md5
            download(apkUrl,callback)
            return
        }
        var preVersion: String = jsonObject.getString("preVersion")
        if (TextUtils.equals(packageInfo.versionName, preVersion)) {
            var patchUrl: String = jsonObject.getString("patchUrl")

            var currentVersionApkNameMd5: String = jsonObject.getString("currentVersionApkNameMd5")
            var patchNameMd5: String = jsonObject.getString("patchNameMd5")

            // download patch
            download(patchUrl, object : ICallback {
                override fun success(path: String) {
                    if(CompileConfig.DEBUG){
                        Log.i("PatchHelper", "success, ${path}")
                    }
                    var pathMd5 = Utils.getFileMD5(File(path))
                    if(CompileConfig.DEBUG){
                        Log.i("PatchHelper", "success, pathMd5:${pathMd5},patchNameMd5:${patchNameMd5}")
                    }
                    if (!TextUtils.equals(patchNameMd5, pathMd5)) {
                        // verify patch md5
                        callback.fail("The patch md5 is error")
                        return
                    }

                    var apkName = apkUrl.substring(apkUrl.lastIndexOf("/") + 1,apkUrl.length);
                    if(CompileConfig.DEBUG){
                        Log.i("PatchHelper", "mergePatch ready-- , distDir:${distDir}, apkName:${apkName}")
                    }
                    var newFilePath: String = mergePatch(distDir,Utils.getApkFile(apkName), path)
//                    if(TextUtils.isEmpty(newFilePath)){
//                        // Merge patch fail
//                        callback.fail("Merge patch fail")
//                        return
//                    }
                    pathMd5 = Utils.getFileMD5(File(newFilePath))
                    if(CompileConfig.DEBUG){
                        Log.i("PatchHelper", "success, merge file md5:${pathMd5},currentVersionApkNameMd5:${currentVersionApkNameMd5}")
                    }
                    if (!TextUtils.equals(currentVersionApkNameMd5, pathMd5)) {
                        // verify merge file md5
                        callback.fail("The merge file md5 is error")
                        return
                    }
                    callback.success(newFilePath!!)
                }

                override fun fail(msg: String) {
                    if(CompileConfig.DEBUG){
                        Log.i("PatchHelper", "fail, ${msg}")
                    }
                    callback.fail(msg)
                }

                override fun progress(progress: Long, total: Long) {
                    callback.progress(progress, total + 1)
                }

            })

            return
        }
        download(apkUrl, callback)

    }


    /**
     * 下载
     */
    private fun download(url: String, callback: ICallback) {
        if (TextUtils.isEmpty(url)) {
            callback.fail("Url is null")
        }

        var nameTemp: String = "temp" + System.currentTimeMillis()
        try {
            nameTemp = url.substring(url.lastIndexOf("/") + 1,url.length);
        } catch (e: Exception) {

        }
        if(CompileConfig.DEBUG){
            Log.i("PatchHelper", "download, url = [${url}],name = [${nameTemp}]")
        }
        DownloaderApi.start(nameTemp,url, object : DownloadCallback {
            override fun onFailure(e: Exception) {
                callback.fail("error:" + e.message)
            }

            override fun onPause(progress: Long, total: Long) {
            }

            override fun onProgress(progress: Long, total: Long) {
                callback.progress(progress, total)
            }

            override fun onSuccess(file: File) {
                callback.success(file.absolutePath)
            }

        })
    }


    /**
     * 合并patch
     */
    public fun mergePatch(oldFilePath: String,newFilePath: String, patchPath: String): String {
        if(CompileConfig.DEBUG){
            Log.i("PatchHelper", "mergePatch, oldFilePath = [${oldFilePath}], newFilePath = [${newFilePath}], patchPath = [${patchPath}]")
        }
        try {

            MergePatch.merge(oldFilePath,newFilePath,patchPath);
//            BigNews.make(oldFilePath,newFilePath,patchPath);
        }catch (e:Exception){
            e.printStackTrace();
        }
        return newFilePath
    }
}