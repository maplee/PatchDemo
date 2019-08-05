package com.matt.patch.openapi

import android.content.Context
import android.util.Log
import com.matt.patch.inner.CompileConfig
import com.matt.patch.inner.ICallback
import com.matt.patch.inner.PatchHelper
import com.matt.patch.inner.Utils

/**
 * Author:Created by matt on 2019/4/16.
 * Email:jiagfone@163.com
 */
class PatchApi {

    companion object {

        lateinit var sConext: Context;

        fun init(context: Context,cachePath:String){
            sConext = context;
            Utils.DOWNLOAD_PATH = cachePath;
            if(CompileConfig.DEBUG){
                Log.i("PatchApi", "init, ${sConext}")
            }
        }

        fun downloadPatch(apkUrl:String,packageName:String,version:Int,patchJson:String,patch: IPatch){
            PatchHelper.getInstance().asyncPatchDownload(apkUrl,packageName,version,patchJson,object :ICallback{
                override fun progress(progress: Long, total: Long) {
                    patch.progress(progress, total)
                }

                override fun success(apkPath:String) {
                    patch.success(apkPath)
                }

                override fun fail(msg: String) {
                    patch.fail(msg)
                }

            })
        }

        fun stopDownload(apkUrl: String){
            PatchHelper.getInstance().stopDownload(apkUrl)
        }

        fun mergeApkPatch(oldFilePath: String,newFilePath: String, patchPath: String){
            PatchHelper.getInstance().mergePatch(oldFilePath, newFilePath, patchPath)

        }
    }
}