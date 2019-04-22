package com.matt.patch.openapi

/**
 * Author:Created by matt on 2019/4/17.
 * Email:jiagfone@163.com
 */
interface IPatch {
    abstract fun success(apkPath:String);
    abstract fun fail(msg: String)
    abstract fun progress(progress: Long, total: Long)
}
