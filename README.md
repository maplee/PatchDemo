# PatchModule

androidapk增量包下载合并模块

添加仓库

```
allprojects {
    repositories {
        google()
        jcenter()
        maven {url 'https://raw.github.com/maplee/mvn-repo/master'}
    }
}

```

添加依赖

```
implementation 'com.matt.module:patch:1.0.0'
implementation 'com.matt.module:downloader:1.0.0'

```


集成模块

## 初始化
在Application中的onCreate中添加

```
PatchApi.init(applicationContext)
DownloaderApi.init(applicationContext)

```

## 使用

### 开始patch下载
```
var apkUrl:String = "http://test.optthis.com/apk/test/test_1.1.1.apk"

// 该json是为了验证下载包的正确以及完整性
// preVersion patch根版本
// patchUrl patch下载链接
// preVersionApkNameMd5 patch根版本apk的md5值
// currentVersionApkNameMd5 当前下载链接版本apk的md5值
// patchNameMd5  patch的md5值
var patshJson:String = "{\"preVersion\": \"1.1.0\",  \"patchUrl\": \"http://test.optthis.com/apk/test/test_1.1.1.patch\", \"preVersionApkNameMd5\": \"972df38636d1fa82c62a28c639444b5c\",  \"currentVersionApkNameMd5\":\"54e59c56965aec266cbfb4eef3553fba\",\"patchNameMd5\": \"bc907548f3e5f30b71742e7ea6d74c01\"}"


//name 文件名称
//url 下载链接
 PatchApi.downloadPatch(apkUrl,"com.test.opt",12,patshJson, patch = object : IPatch {
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
```
### 停止下载
```
//url 下载链接
PatchApi.stopDownload(url);
```
### 合并指定包和patch
```
// oldFilePath 旧的apk路径
// newFilePath 合成的apk路径
// patchPath patch的路径
PatchApi.mergeApkPatch(oldFilePath, newFilePath, patchPath)
```
