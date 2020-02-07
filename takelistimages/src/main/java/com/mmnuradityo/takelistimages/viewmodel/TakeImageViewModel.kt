package com.mmnuradityo.takelistimages.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.mmnuradityo.takelistimages.R
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.AUTHORITY
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.CAMERA_PERMISSION
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.FILE_PERMISSION
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.INTENT_DATA
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CAMERA_PERMISSION
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CODE_DATA_DELETE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CODE_GALLERY
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_CODE_TAKE_PICTURE
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.REQUEST_FILE_PERMISSION
import com.mmnuradityo.takelistimages.constant.ConstantVariable.Companion.RESULT_CODE_DATA_DELETE
import com.mmnuradityo.takelistimages.model.data.DataImage
import com.mmnuradityo.takelistimages.model.viewstate.TakeImageViewState
import com.mmnuradityo.takelistimages.repository.ImageRepository
import com.mmnuradityo.takelistimages.util.ImageUtils
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TakeImageViewModel(private val imageRepository: ImageRepository) : ViewModel() {

    private lateinit var fragment: Fragment
    private var maxImageSize: Int = 1200
    private var imageQuality: Int = 90
    private var data: MutableList<DataImage>? = null
    private lateinit var imageUtils: ImageUtils

    private val viewStateCondition = MutableLiveData<TakeImageViewState>().apply {
        value = TakeImageViewState()
    }
    val viewState: LiveData<TakeImageViewState> get() = viewStateCondition

    private fun setViewState(
        data: MutableList<DataImage>?,
        exception: Exception? = null,
        takeCancel: Boolean = false,
        dataImageFile: MutableList<File>? = null
    ) {
        viewStateCondition.value =
            viewStateCondition.value?.copy(
                data = data,
                exception = exception,
                takeCancel = takeCancel,
                dataImageFile = dataImageFile
            )
    }

    fun getAllData() = viewModelScope.launch {
        try {
            data = imageRepository.getAllData()
            setViewState(data)
        } catch (ex: Exception) {
            setViewState(data, ex)
        }
    }

    private fun addData(dataImage: DataImage?) = viewModelScope.launch {
        imageRepository.addData(dataImage)
    }

    private fun replaceData(dataImage: DataImage, position: Int) = viewModelScope.launch {
        imageRepository.replaceData(dataImage, position)
    }

    private fun deleteData(uri: Uri) = viewModelScope.launch {
        imageRepository.deleteData(uri)
    }

    fun setDataDefault() = viewModelScope.launch {
        when {
            imageRepository.getAllData() == null -> {
                val drawable = ContextCompat.getDrawable(fragment.activity!!, R.drawable.ic_camera)
                drawable?.let {
                    addData(DataImage(imageUtils.drawableToBitmap(it), null))
                }
            }
        }
    }

    fun getDataImageFile() = viewModelScope.launch {
        val dataList = imageRepository.getDataImageList()

        try {
            if (dataList.equals(null) || dataList.size == 0) {
                setViewState(data, Exception("data empty"), dataImageFile = ArrayList())
            }
            val listFile = mutableListOf<File>()
            for (i in dataList.indices) {
                listFile.add(File(dataList[i].uri!!.path))
            }
            setViewState(data, dataImageFile = listFile)
        } catch (ex: Exception) {
            setViewState(data, ex)
        }
    }

    fun setComponent(fragment: Fragment, imageQuality: Int, maxImageSize: Int) {
        this.imageUtils = ImageUtils(fragment.activity!!)
        this.fragment = fragment
        this.imageQuality = imageQuality
        this.maxImageSize = maxImageSize
    }

    fun permissionChecker() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        if (ContextCompat.checkSelfPermission(fragment.activity!!, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                fragment.activity!!,
                CAMERA_PERMISSION,
                REQUEST_CAMERA_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(
                fragment.activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                fragment.activity!!,
                FILE_PERMISSION,
                REQUEST_FILE_PERMISSION
            )
        }
    }

    fun resultChecker(
        filePath: Uri?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        cropper: Boolean,
        position: Int
    ) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                when {
                    cropper -> {
                        withCropperImage(requestCode, filePath, data, position)
                    }
                    requestCode == REQUEST_CODE_TAKE_PICTURE && !cropper -> {
                        setDataImage(filePath, position)
                    }
                    requestCode == REQUEST_CODE_GALLERY && !cropper -> {
                        setDataImage(data!!.data!!, position)
                    }
                    else -> {
                        setViewState(this.data, Exception("error from take image"), false)
                    }
                }
            }
            RESULT_CODE_DATA_DELETE -> {
                when (requestCode) {
                    REQUEST_CODE_DATA_DELETE -> {
                        deleteData(data?.extras?.get(INTENT_DATA) as Uri)
                    }
                }
            }
            else -> setViewState(this.data, Exception("cancel"), true)
        }
    }

    private fun setDataImage(dataFile: Uri?, position: Int) {

        var image = imageUtils.uriToBitmap(dataFile!!)
        image.compress(Bitmap.CompressFormat.JPEG, imageQuality, ByteArrayOutputStream())

        val bitmap = when {
            maxImageSize > 0 -> {
                imageUtils.bitmapResize(image, maxImageSize)
            }
            else -> null
        }

        val thumbnail = imageUtils.bitmapResize(image, 260)
        imageToData(image, bitmap, thumbnail, position)
    }

    private fun imageToData(
        image: Bitmap,
        bitmap: Bitmap?,
        thumbnail: Bitmap?,
        position: Int
    ) {
        when {
            bitmap == null && position == -1 -> {
                val imageUri = imageUtils.bitmapToUri(image)
                addData(
                    DataImage(
                        thumbnail,
                        imageUri
                    )
                )
            }
            bitmap != null && position == -1 -> {
                val imageUri = imageUtils.bitmapToUri(bitmap)
                addData(
                    DataImage(
                        thumbnail,
                        imageUri
                    )
                )
            }
            bitmap != null && position >= 0 -> {
                val imageUri = imageUtils.bitmapToUri(bitmap)
                replaceData(
                    DataImage(
                        thumbnail,
                        imageUri
                    ),
                    position
                )
            }
            bitmap == null && position >= 0 -> {
                val imageUri = imageUtils.bitmapToUri(image)
                replaceData(
                    DataImage(
                        thumbnail,
                        imageUri
                    ),
                    position
                )
            }
        }
    }

    private fun withCropperImage(
        requestCode: Int,
        filePath: Uri?,
        data: Intent?,
        position: Int
    ) {
        when (requestCode) {
            REQUEST_CODE_TAKE_PICTURE -> {
                cropImage(filePath!!)
            }
            REQUEST_CODE_GALLERY -> {
                cropImage(data!!.data!!)
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                setDataImage(CropImage.getActivityResult(data).uri, position)
            }
            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                val result = CropImage.getActivityResult(data)
                setViewState(this.data, Exception(result.toString()), false)
            }
        }
    }

    private fun cropImage(imagePath: Uri) {
        CropImage.activity(imagePath)
            .setAspectRatio(1, 1)
            .setFixAspectRatio(true)
            .setInitialCropWindowPaddingRatio(0f)
            .setAutoZoomEnabled(true)
            .setCropMenuCropButtonIcon(R.drawable.ic_crop)
            .setBorderLineThickness(3f)
            .setOutputCompressQuality(100)
            .start(fragment.context!!, fragment)
    }

    fun getCacheImagePath(fileName: String): Uri {
        val path = File(fragment.activity!!.externalCacheDir, "camera")
        if (!path.exists()) path.mkdirs()
        val image = File(path, fileName)
        return FileProvider.getUriForFile(fragment.activity!!, AUTHORITY, image)
    }

    fun clearCache() {
        val path = fragment.activity!!.cacheDir
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()!!) {
                child.delete()
            }
            path.delete()
        }
    }

    fun saveViewInstance() {
    }

}

@Suppress("UNCHECKED_CAST")
class TakeImageFactory(private val imageRepository: ImageRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TakeImageViewModel::class.java)
            -> TakeImageViewModel(imageRepository) as T
            else -> throw IllegalAccessException()
        }
    }

}