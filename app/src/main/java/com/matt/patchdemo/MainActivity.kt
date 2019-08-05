package com.matt.patchdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.matt.downloader.openapi.DownloaderApi
import com.matt.patch.openapi.IPatch
import com.matt.patch.openapi.PatchApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : View.OnClickListener, AppCompatActivity() {

    //相机权限
    private val REQUEST_STORAGE_PERMISSIONS = 1
    private val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    var apkUrl:String = "http://inno72.oss.72solo.com/apk/test/test_detection1.1.2.apk"
    var patshJson:String = "{\"preVersion\": \"1.1.1\",  \"patchUrl\": \"http://inno72.oss.72solo.com/apk/test/test_detection1.1.2.patch\", \"preVersionApkNameMd5\": \"972df38636d1fa82c62a28c639444b5c\",  \"currentVersionApkNameMd5\":\"54e59c56965aec266cbfb4eef3553fba\",\"patchNameMd5\": \"bc907548f3e5f30b71742e7ea6d74c01\"}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PatchApi.init(applicationContext)
        btn_download_apk.setOnClickListener(this)
        btn_pause_apk.setOnClickListener(this)
        btn_merge_apk.setOnClickListener(this)
        btn_pause_apk.visibility = View.GONE
        initView()
        // 授权
        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions()
        }
    }

    private fun initView() {

    }

    /**
     * Requests permissions necessary to use camera and save pictures.
     */
    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, REQUEST_STORAGE_PERMISSIONS)
    }

    /**
     * Tells whether all the necessary permissions are granted to this app.
     *
     * @return True if all the required permissions are granted.
     */
    private fun hasAllPermissionsGranted(): Boolean {
        for (permission in STORAGE_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    override fun onClick(v: View?) {
        // 授权
        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions()
        }
        when (v?.id) {
            R.id.btn_download_apk -> consume {
                if(BuildConfig.DEBUG){
                    Log.i("MainActivity", "onClick, btn_download_apk")
                }
                println("onClick, btn_download_apk apkUrl = ${apkUrl}")
                btn_download_apk.visibility = View.GONE
                btn_pause_apk.visibility = View.VISIBLE

                PatchApi.downloadPatch(apkUrl,"com.inno72.detection",13,patshJson, patch = object : IPatch {
                    override fun success(apkPath: String) {
                        println("apkPath = [${apkPath}]")
                        if(BuildConfig.DEBUG){
                            Log.i("MainActivity", "success, ${apkPath}")
                        }
                    }

                    override fun fail(msg: String) {
                        println("msg = [${msg}]")
                        if(BuildConfig.DEBUG){
                            Log.i("MainActivity", "fail, ${msg}")
                        }
                    }

                    override fun progress(progress: Long, total: Long) {
                        println("progress = [${progress}], total = [${total}]")
                        if(BuildConfig.DEBUG){
                            Log.i("MainActivity", "progress, ${progress},total, ${total}")
                        }
                    }

                })

            }
            R.id.btn_pause_apk -> consume {
                println("onClick, btn_pause_apk apkUrl = ${apkUrl}")
                btn_download_apk.visibility = View.VISIBLE
                btn_pause_apk.visibility = View.GONE
                PatchApi.stopDownload(apkUrl)
            }
            R.id.btn_merge_apk -> consume {
                var oldFilePath = "/storage/emulated/0/Download/matt/download/origin/com.inno72.detection1.1.1.apk"
                var newFilePath = "/storage/emulated/0/Download/matt/download/test_detection1.1.2.apk"
                var patchPath = "/storage/emulated/0/Download/matt/download/test_detection1.1.2.patch"
                println("onClick, btn_merge_apk apkUrl = ${apkUrl}")
                PatchApi.mergeApkPatch(oldFilePath, newFilePath, patchPath)
            }
        }
    }

    inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
