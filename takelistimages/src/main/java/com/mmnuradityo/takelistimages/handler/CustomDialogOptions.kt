package com.mmnuradityo.takelistimages.handler

import android.app.Activity
import android.os.Bundle
import com.mmnuradityo.basestyle.base.BaseCustomDialog
import com.mmnuradityo.takelistimages.R
import kotlinx.android.synthetic.main.custom_dialog_option.*

class CustomDialogOptions(activity: Activity) : BaseCustomDialog(activity) {

    private lateinit var view: CustomDialogOptionsView

    override fun setResourceBackground(): Int = 0

    override fun setLayout(): Int = R.layout.custom_dialog_option

    fun setComponent(view: CustomDialogOptionsView): CustomDialogOptions {
        this.view = view
        return this
    }

    override fun initComponent(savedInstanceState: Bundle?) {

    }

    override fun initView() {
        btn_openCamera.setOnClickListener {
            view.onOnCameraPressed()
        }
        btn_openGallery.setOnClickListener {
            view.onOnGalleryPressed()
        }
    }

}

interface CustomDialogOptionsView {
    fun onOnCameraPressed()
    fun onOnGalleryPressed()
}