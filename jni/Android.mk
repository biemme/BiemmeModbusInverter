LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := com_biemme_inverter_ModbusLib.c
LOCAL_MODULE := com_biemme_inverter_ModbusLib
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

