package com.matt.patch.inner

import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import com.matt.patch.BuildConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Author:Created by matt on 2019/4/11.
 * Email:jiagfone@163.com
 */
class Utils {

    companion object {
        val PATCH = ".patch";
        val JSON = ".json";
        val APK = ".apk";
        var DOWNLOAD_PATH = "/matt/download"

        private fun getRootFile(): String {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        }

        public fun getDownloadPath(): File {
            val parent = File(getRootFile() + DOWNLOAD_PATH)
            if (!parent.exists()) {
                parent.mkdirs()
            }
            return parent
        }

        fun getApkFile(name: String): String {
            //final String filename = url.substring(url.lastIndexOf("/") + 1);
            val parent = getDownloadPath()

            val file = File(parent.absolutePath + "/" + name)

            return file.absolutePath
        }

        fun getPatchFile(name: String): String {

            val parent = getDownloadPath()
            var nameTemp = name.substring(name.lastIndexOf("/")+1,name.lastIndexOf(".")+1);

            val file = File(parent.absolutePath + "/" + nameTemp+ PATCH)

            return file.absolutePath
        }

        fun isDownloadPatch(name: String,md5Value:String): Boolean {
            val parent = getDownloadPath()
            var nameTemp = name.substring(name.lastIndexOf("/")+1,name.lastIndexOf(".")+1);
            val file = File(parent.absolutePath + "/" + nameTemp+ APK)
            return file.exists()
        }

        fun getFileMD5(file: File): String?{
            if (!file.isFile) {
                return null
            }
            var digest: MessageDigest? = null
            var mFileInputStream: FileInputStream? = null
            val buffer = ByteArray(1024)
            var len: Int = -1
            try {
                digest = MessageDigest.getInstance("MD5")
                mFileInputStream = FileInputStream(file)

                while (mFileInputStream.read(buffer).also { len = it } != -1) {
                    digest?.update(buffer, 0, len)
                }
                mFileInputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            val bigInt = BigInteger(1, digest?.digest())
            return bigInt.toString(16)
        }

        /**
         * 获取json地址
         */
        fun getJsonUrl(url: String): String {
            var nameTemp = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf(".")+1);
            return nameTemp+ JSON
        }

        fun getInstalledApkPackageInfo(context:Context,packageName:String):PackageInfo?{
            var list:MutableList<PackageInfo> = context.packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
            list.forEach {
                if(BuildConfig.DEBUG){
                    Log.i(TAG,"getInstalledApkVersion--packageName:"+it.applicationInfo.packageName
                            +",versionName:"+it.versionCode
                            +",sourceDir:"+it.applicationInfo.sourceDir)
                }
                if (packageName.equals(it.applicationInfo.packageName)){
                    return it
                }
            }
            return null
        }

        fun getInstalledApk(context:Context,packageName:String):String?{
            var list:MutableList<PackageInfo> = context.packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
            list.forEach {
                if(BuildConfig.DEBUG){
                    Log.i(TAG,"getInstalledApk--packageName:"+it.applicationInfo.packageName
                            +",versionName:"+it.versionName
                            +",sourceDir:"+it.applicationInfo.sourceDir)
                }

                if (packageName.equals(it.applicationInfo.packageName)){
                    // 复制到指定路径下
                    var distDir = Utils.getDownloadPath().absolutePath +"/origin/"+packageName+it.versionName+".apk"
                    copyFile(it.applicationInfo.sourceDir,distDir)
                    return distDir
                }
            }
            return null
        }


        /**
         * 复制文件
         */
        public fun copyFile(sourceDir:String,distDir:String):Boolean{
            if(BuildConfig.DEBUG){
                Log.i("Utils", "copyFile, sourceDir:${sourceDir},distDir:${distDir}")
            }
            var sourceFile = File(sourceDir)
            if (!sourceFile.exists() || !sourceFile.isFile){
                return false
            }
            var distPath = distDir.substring(0,distDir.lastIndexOf("/")+1)
            var distPathFile = File(distPath)
            if (!distPathFile.exists()){
                distPathFile.mkdirs()
            }
            var distFile = File(distDir)
            if (distFile.exists()){
                distFile.delete()
            }
            distFile.createNewFile()

            var inputStream: InputStream? = null
            var fileOutputStream:FileOutputStream? = null
            try {
                inputStream = FileInputStream(sourceDir)
                fileOutputStream = FileOutputStream(distDir);
                var buffer = ByteArray(10*1024)
                var length: Int = -1
                while (inputStream?.read(buffer).also { length = it!! }  != -1) {
                    fileOutputStream.write(buffer,0,length)
                }

            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                try {
                    inputStream?.close()
                    fileOutputStream?.close()
                }catch (e:Exception){

                }
            }
            return true
        }

    }


}