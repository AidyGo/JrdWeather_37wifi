LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := TctWeather

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_MODULE_PATH := $(TARGET_OUT)/app/custpack

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v13

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
