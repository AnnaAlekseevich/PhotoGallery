package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {
    private val flickrApi: FlickrApi
    val gSon = GsonBuilder().registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer()).create()


    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gSon))
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
        //val galleryItemList: List<GalleryItem> = gSon.fromJson(stringReader , Array<GalleryItem>::class.java).toList()

    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoResponse> = flickrApi.fetchPhotos()
        flickrRequest.enqueue(object : Callback<PhotoResponse> {
            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
            override fun onResponse(
                call: Call<PhotoResponse>,
                response: Response<PhotoResponse>
            ) {
                Log.d(TAG, "Response received")
                val flickrResponse: List<GalleryItem> = response.body()?.galleryItems!!
                Log.d(TAG, "Response received" + response.body()?.galleryItems!!)

                //val photoResponse: PhotoResponse? = //flickrResponse?.photos
                var galleryItems: List<GalleryItem> = flickrResponse
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                Log.d("PhotoGalleryFragment!!", "galleryItems" + galleryItems)
                responseLiveData.value = galleryItems
            }
        })
        return responseLiveData
    }

}