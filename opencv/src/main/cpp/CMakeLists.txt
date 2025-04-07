cmake_minimum_required(VERSION 3.22.1)

project(opencv_android)

set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../opencv-4.8.0-android-sdk/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)

add_library(
    opencv_java4
    SHARED
    IMPORTED
)

set_target_properties(
    opencv_java4
    PROPERTIES
    IMPORTED_LOCATION "${OpenCV_DIR}/${ANDROID_ABI}/libopencv_java4.so"
)

include_directories(${OpenCV_INCLUDE_DIRS})