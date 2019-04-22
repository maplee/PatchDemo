LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    :=merge_patch
LOCAL_SRC_FILES :=com_matt_patch_openapi_MergePatch.c

#liblog.so libGLESv2.so
LOCAL_LDLIBS += -lz -llog
include $(BUILD_SHARED_LIBRARY)