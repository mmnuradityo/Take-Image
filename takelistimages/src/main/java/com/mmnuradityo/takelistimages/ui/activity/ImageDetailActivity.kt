package com.mmnuradityo.takelistimages.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.mmnuradityo.basestyle.base.BaseActivity
import com.mmnuradityo.basestyle.widget.showText
import com.mmnuradityo.takelistimages.R
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.INTENT_DATA
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.INTENT_POSITION_UPDATE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.RESULT_CODE_DATA_DELETE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.RESULT_CODE_DONE
import com.mmnuradityo.takelistimages.handler.CustomDialogOptions
import com.mmnuradityo.takelistimages.handler.CustomDialogOptionsView
import com.mmnuradityo.takelistimages.ui.adapter.RvAdapter.Holder.Companion.imageBtnActive
import com.mmnuradityo.takelistimages.util.ImageUtils
import kotlinx.android.synthetic.main.activity_image_detail.*
import java.io.File
import java.text.DecimalFormat

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ImageDetailActivity : BaseActivity(), CustomDialogOptionsView {

    companion object {
        private lateinit var view: DetailImageView

        fun setViewComponent(view: DetailImageView) {
            Companion.view = view
        }
    }

    private var position: Int = 0
    private lateinit var imageName: String
    private var bundle: Bundle? = null
    private lateinit var dataIntent: Uri
    private lateinit var dialog: CustomDialogOptions
    private lateinit var imageUtils: ImageUtils

    override fun setLayout(): Int = R.layout.activity_image_detail

    override fun initComponent(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        bundle = intent.extras
        dataIntent = bundle!!.getParcelable(INTENT_DATA)!!
        position = bundle!!.getInt(INTENT_POSITION_UPDATE)
        dialog = CustomDialogOptions(this).setComponent(this)
        imageUtils = ImageUtils(this)
    }

    override fun initView() {
        imageName = imageUtils.getNameFromUri(dataIntent)
        image_detail.setImageURI(dataIntent)
        tv_title.text = imageName
    }

    override fun loadView() {
        super.loadView()
        showFileSize()
    }

    private fun showFileSize() {
        val imageFile = File(dataIntent.path)
        val df = DecimalFormat("0.00")
        val fileSize = df.format(imageUtils.getImageSize(imageFile))
        showText(this, "Image Size : $fileSize MB")
    }

    override fun onStart() {
        super.onStart()
        imageBtnActive = true
    }

    override fun listener() {
        super.listener()
        btn_back.setOnClickListener {
            onBackPressed()
        }
        image_detail.setOnClickListener {
            when (toolbar_DetailImage.visibility) {
                View.VISIBLE -> {
                    toolbar_DetailImage.visibility = View.GONE
                    nav_bottom.visibility = View.GONE
                }
                else -> {
                    toolbar_DetailImage.visibility = View.VISIBLE
                    nav_bottom.visibility = View.VISIBLE
                }
            }
        }
        btn_done.setOnClickListener {
            onBackPressed()
        }
        btn_delete.setOnClickListener {
            showText(this, "Delete Image : $imageName")
            setResult(RESULT_CODE_DATA_DELETE, Intent().putExtra(INTENT_DATA, dataIntent))
            finish()
        }
        btn_change.setOnClickListener {
            dialog.show()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CODE_DONE)
        super.onBackPressed()
    }

    override fun onOnCameraPressed() {
        view.onDetailCameraClick(position)
        dialog.dismiss()
        onBackPressed()
    }

    override fun onOnGalleryPressed() {
        view.onDetailGalleryClick(position)
        dialog.dismiss()
        onBackPressed()
    }

}

interface DetailImageView {
    fun onDetailCameraClick(position: Int)
    fun onDetailGalleryClick(position: Int)
}