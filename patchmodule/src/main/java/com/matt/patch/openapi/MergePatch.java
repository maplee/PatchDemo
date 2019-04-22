package com.matt.patch.openapi;

import android.support.annotation.Keep;

/**
 * Author:Created by matt on 2019/4/18.
 * Email:jiagfone@163.com
 */
@Keep
public class MergePatch {

    public native static int merge(String oldFile,String newFile,String patchFile);
    static{
        System.loadLibrary("merge_patch");
    }
}
