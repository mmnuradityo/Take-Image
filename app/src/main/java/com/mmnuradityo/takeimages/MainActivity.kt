package com.mmnuradityo.takeimages

import android.net.Uri
import android.os.Bundle
import com.mmnuradityo.basestyle.base.BaseActivity
import com.mmnuradityo.basestyle.widget.showText
import com.mmnuradityo.takelistimages.ui.fragment.TakeImage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity() {

    private var listFile = mutableListOf<File>()

    override fun setLayout(): Int = R.layout.activity_main

    override fun initComponent(savedInstanceState: Bundle?) {
        TakeImage.apply {
            setImageDimension(1500)
            setLimitList(5)
            setQuality(50)
            setSquareCropper(false)
            start(this@MainActivity)
        }
    }

    override fun initView() {
        switch_cropper.isChecked = false
    }


    override fun listener() {

        switch_cropper.setOnCheckedChangeListener { _, b ->
            // Activated Cropper Image for Custom
            TakeImage.setSquareCropper(b)
        }

        btn_testListFile.setOnClickListener {
            //get data result list File
            listFile = TakeImage.getDataTakeImageListFile()

            if (listFile.size > 0) {
                showText(this, "List File : " + listFile.size.toString())
                image_test.setImageURI(Uri.fromFile(listFile[0]))
            } else {
                showText(this, "no image file")
            }
        }
    }
}
