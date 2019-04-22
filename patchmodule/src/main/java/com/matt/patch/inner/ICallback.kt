package com.matt.patch.inner

/**
 * Author:Created by matt on 2019/4/16.
 * Email:jiagfone@163.com
 */
interface ICallback {

    abstract fun success(path:String);
    abstract fun fail(msg: String)
    abstract fun progress(progress: Long, total: Long)
}