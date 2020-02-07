package com.mmnuradityo.takelistimages.ui.fragment

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.mmnuradityo.basestyle.base.BaseFragment
import com.mmnuradityo.basestyle.widget.showText
import com.mmnuradityo.takelistimages.R
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.INTENT_DATA
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.INTENT_POSITION_UPDATE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CODE_DATA_DELETE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CODE_GALLERY
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CODE_TAKE_PICTURE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.RESULT_CODE_DONE
import com.mmnuradityo.takelistimages.datastore.ImageLocalDataStore
import com.mmnuradityo.takelistimages.handler.CustomDialogOptions
import com.mmnuradityo.takelistimages.handler.CustomDialogOptionsView
import com.mmnuradityo.takelistimages.model.data.DataImage
import com.mmnuradityo.takelistimages.model.viewstate.TakeImageViewState
import com.mmnuradityo.takelistimages.repository.ImageRepository
import com.mmnuradityo.takelistimages.ui.activity.DetailImageView
import com.mmnuradityo.takelistimages.ui.activity.ImageDetailActivity
import com.mmnuradityo.takelistimages.ui.adapter.RvAdapter
import com.mmnuradityo.takelistimages.ui.adapter.RvAdapterView
import com.mmnuradityo.takelistimages.viewmodel.TakeImageFactory
import com.mmnuradityo.takelistimages.viewmodel.TakeImageViewModel
import kotlinx.android.synthetic.main.take_image_fragment.*
import java.io.File

open class TakeImage : BaseFragment(), RvAdapterView, CustomDialogOptionsView,
    DetailImageView {

    companion object {

        private val instance by lazy { TakeImage() }

        private var maxImageDimension: Int = 1400

        private var limitListSize: Int = 5

        private var squareImageCropper: Boolean = false

        private var outputQuality: Int = 90

        private var dataImageFile = mutableListOf<File>()

        fun setImageDimension(ImageDimension: Int): TakeImage {
            maxImageDimension = ImageDimension
            return instance
        }

        fun setLimitList(limit: Int): TakeImage {
            limitListSize = limit
            return instance
        }

        fun setSquareCropper(cropper: Boolean): TakeImage {
            squareImageCropper = cropper
            return instance
        }

        fun setQuality(quality: Int): TakeImage {
            outputQuality = quality
            return instance
        }

        fun start(activity: AppCompatActivity) {
            activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.list_container,
                    instance
                )
                .commit()
        }

        fun getDataTakeImageListFile(): MutableList<File> {
            instance.viewModel.getDataImageFile()
            return when {
                dataImageFile.size > 0 -> dataImageFile
                else -> ArrayList()
            }
        }

        private val factory: TakeImageFactory = TakeImageFactory(
            ImageRepository.instances.apply {
                init(ImageLocalDataStore())
            })

    }

    private lateinit var viewModel: TakeImageViewModel
    private lateinit var rvAdapter: RvAdapter
    private lateinit var dialog: CustomDialogOptions
    private var colorBackground: Int = Color.WHITE
    private var position: Int = -1
    private var filePath: Uri? = null
    private lateinit var fileName: String

    override fun setLayout(): Int = R.layout.take_image_fragment

    override fun initComponent(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        rvAdapter = RvAdapter(activity!!).apply {
            setComponent(this@TakeImage)
            setLimitList(limitListSize)
        }
        dialog = CustomDialogOptions(activity!!).setComponent(this)
        ImageDetailActivity.setViewComponent(
            this
        )
    }

    override fun initView() {
        list_image.layoutManager = GridLayoutManager(activity!!, 4)
        list_image.setBackgroundColor(colorBackground)
        list_image.clipToPadding = false
        list_image.overScrollMode = View.OVER_SCROLL_NEVER
        list_image.adapter = rvAdapter
    }

    override fun loadView() {
        viewModel =
            ViewModelProvider(this,
                factory
            ).get(TakeImageViewModel::class.java).apply {
                viewState.observe(this@TakeImage, Observer(this@TakeImage::handleState))
                setComponent(this@TakeImage,
                    outputQuality,
                    maxImageDimension
                )
                permissionChecker()
                setDataDefault()
            }
    }

    private fun handleState(viewState: TakeImageViewState?) {
        viewState?.let {
            it.data?.let { data -> onSuccess(data) }
            it.exception?.let { ex -> onError(ex) }
            setResultCancelled(it.takeCancel)
            it.dataImageFile?.let { dataImageFile -> Companion.dataImageFile = dataImageFile }
        }
    }

    private fun onSuccess(data: MutableList<DataImage>) {
        rvAdapter.addDataList(data)
    }

    private fun onError(exception: Exception) {
        showText(activity!!, exception.message!!)
    }

    override fun onItemCameraClick() {
        dialog.show()
    }

    override fun onItemImageClick(data: DataImage, position: Int) {
        filePath = null
        val b = Bundle()
        b.putParcelable(INTENT_DATA, data.uri)
        b.putInt(INTENT_POSITION_UPDATE, position)
        val i = Intent(activity!!, ImageDetailActivity::class.java)
        i.putExtras(b)
        startActivityForResult(i, REQUEST_CODE_DATA_DELETE)
    }

    override fun onOnCameraPressed() {
        this.position = -1
        takeCameraImage()
        dialog.dismiss()
    }

    override fun onOnGalleryPressed() {
        this.position = -1
        getImageGallery()
        dialog.dismiss()
    }

    private fun takeCameraImage() {
        fileName = System.currentTimeMillis().toString() + ".jpg"
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(ContextWrapper(activity!!).packageManager) != null) {
            filePath = viewModel.getCacheImagePath(fileName)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath)
            takePictureIntent.putExtra(
                MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            )
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE)
        }
    }

    private fun getImageGallery() {
        filePath = null
        val takePictureIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        takePictureIntent.type = "image/*"
        startActivityForResult(takePictureIntent, REQUEST_CODE_GALLERY)
    }

    override fun onDetailCameraClick(position: Int) {
        this.position = position
        takeCameraImage()
    }

    override fun onDetailGalleryClick(position: Int) {
        this.position = position
        getImageGallery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != RESULT_CODE_DONE -> {
                viewModel.resultChecker(
                    filePath,
                    requestCode,
                    resultCode,
                    data,
                    squareImageCropper,
                    position
                )
            }
        }
    }

    private fun setResultCancelled(cancel: Boolean) {
        when {
            cancel -> {
                val intent = Intent()
                activity!!.setResult(Activity.RESULT_CANCELED, intent)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveViewInstance()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllData()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearCache()
    }

}
