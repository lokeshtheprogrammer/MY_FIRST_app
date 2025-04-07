package org.opencv

class OpenCVLibrary {
    companion object {
        init {
            System.loadLibrary("opencv_java4")
        }
    }

    external fun getVersion(): String

    external fun initializeOpenCV(): Boolean
}