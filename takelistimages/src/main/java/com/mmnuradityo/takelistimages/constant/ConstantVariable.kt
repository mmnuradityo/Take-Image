package com.mmnuradityo.takelistimages.constant

class ConstantVariable {

    companion object {
        const val AUTHORITY = "com.mmnuradityo.takelistimages"
        val CAMERA_PERMISSION = arrayOf(
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.hardware.camera2.full"
        )
        val FILE_PERMISSION = arrayOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
        )
        const val REQUEST_CAMERA_PERMISSION = 128
        const val REQUEST_CODE_TAKE_PICTURE = 11
        const val REQUEST_CODE_CHOOSE_PICTURE = 12
        const val REQUEST_FILE_PERMISSION = 2
        const val REQUEST_CODE_GALLERY = 1
        const val REQUEST_CODE_DATA_DELETE = 202

        const val RESULT_CODE_DATA_CHANGE = 300
        const val RESULT_CODE_DATA_DELETE = 101
        const val RESULT_CODE_DONE = 200

        const val INTENT_DATA = "data"
        const val INTENT_DATA_UPDATE =  "update data"
        const val INTENT_POSITION_UPDATE =  "update position"
        const val INTENT_REQUEST_CODE = "requet code"
        const val INTENT_RESULT_CODE = "result code"
        const val INTENT_FILE_PATH = "file path"
    }

}