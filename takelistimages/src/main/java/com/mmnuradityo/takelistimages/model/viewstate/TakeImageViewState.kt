package com.mmnuradityo.takelistimages.model.viewstate

import com.mmnuradityo.takelistimages.model.data.DataImage
import java.io.File

data class TakeImageViewState(
    val data: MutableList<DataImage>? = null,
    val exception: Exception? = null,
    val takeCancel: Boolean = false,
    val dataImageFile: MutableList<File>? = null)