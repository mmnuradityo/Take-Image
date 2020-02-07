package com.mmnuradityo.takelistimages.repository

import android.net.Uri
import com.mmnuradityo.takelistimages.datastore.ImageDataStore
import com.mmnuradityo.takelistimages.model.data.DataImage

class ImageRepository private constructor() {

    private var localDataStore: ImageDataStore? = null

    companion object {
        val instances by lazy { ImageRepository() }
    }

    fun init(imageLocalDataStore: ImageDataStore) {
        this.localDataStore = imageLocalDataStore
    }

    suspend fun getAllData(): MutableList<DataImage>? {
        val cache = localDataStore?.getAllData()
        return when {
            cache != null -> cache
            else -> null
        }
    }

    suspend fun addData(dataImage: DataImage?) {
        localDataStore?.addData(dataImage)
    }

    suspend fun replaceData(dataImage: DataImage?, position: Int) {
        localDataStore?.replaceImage(dataImage, position)
    }

    suspend fun deleteData(uri: Uri) {
        localDataStore?.deleteData(findPosition(uri))
    }

    private suspend fun findPosition(uri: Uri): Int {
        var position = -1
        val data = localDataStore?.getAllData()!!
        for (i in data.indices) {
            if (data[i].uri == uri) {
                position = i
            }
        }
        return position
    }

    suspend fun getDataImageList(): MutableList<DataImage> {
        val data = mutableListOf<DataImage>()
        data.addAll(localDataStore?.getAllData()!!)
        data.let { it.removeAt(it.size - 1) }
        return data
    }
}