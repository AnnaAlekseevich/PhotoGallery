package com.bignerdranch.android.photogallery


import android.util.Log
import androidx.paging.PositionalDataSource
import com.bignerdranch.android.photogallery.domain.models.GalleryItem


class MyPositionalDataSource(private val photos: List<GalleryItem>) :
    PositionalDataSource<GalleryItem>() {

    private val TAG = "MyPositionalDataSource"

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<GalleryItem>
    ) {
        Log.d(
            TAG, "loadInitial, requestedStartPosition = " + params.requestedStartPosition +
                    ", requestedLoadSize = " + params.requestedLoadSize
        )
        val resultInitial = mutableListOf<GalleryItem>()
        for(n in params.requestedStartPosition..params.requestedLoadSize){
            resultInitial.add(photos[n])
        }
        Log.d(TAG, "resultInitial = " + resultInitial)
        callback.onResult(resultInitial as List<GalleryItem>, 0)
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<GalleryItem>
    ) {
        Log.d(
            TAG,
            "loadRange, startPosition = " + params.startPosition + ", loadSize = " + params.loadSize
        )
        val resultRange = mutableListOf<GalleryItem>()
        var positionLoadEnd: Int = params.loadSize + params.startPosition
        if (positionLoadEnd>99) {
            positionLoadEnd = 99
        }
        Log.d(TAG,"positionLoadEnd = " + positionLoadEnd)
        //Log.d(TAG, "positionLoadEnd = " + positionLoadEnd)
        for(n in params.startPosition..(positionLoadEnd)){
            resultRange.add(photos[n])
        }
        //val result: List<GalleryItem?> = photos.getData(params.startPosition, params.loadSize)
        Log.d(TAG, "resultRange = " + resultRange)
        callback.onResult(resultRange as List<GalleryItem>)
    }

}

