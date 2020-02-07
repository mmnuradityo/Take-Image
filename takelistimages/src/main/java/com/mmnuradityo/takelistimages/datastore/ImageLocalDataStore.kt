package com.mmnuradityo.takelistimages.datastore

import com.mmnuradityo.takelistimages.model.data.DataImage

class ImageLocalDataStore : ImageDataStore {

    private var cache = mutableListOf<DataImage>()

    override suspend fun getAllData(): MutableList<DataImage>? {
        return when {
            cache.isNotEmpty() -> {
                cache
            }
            else -> null
        }
    }

    override suspend fun addData(dataImage: DataImage?) {
        dataImage?.let {
            cache.add(0, it)
        }
    }

    override suspend fun replaceImage(dataImage: DataImage?, position: Int) {
        dataImage?.let {
            cache.removeAt(position)
            cache.add(position, it)
        }
    }

    override suspend fun deleteData(position: Int) {
        cache.removeAt(position)
    }

}

interface ImageDataStore {
    suspend fun getAllData(): MutableList<DataImage>?
    suspend fun addData(dataImage: DataImage?)
    suspend fun replaceImage(dataImage: DataImage?, position: Int)
    suspend fun deleteData(position: Int)
}