package com.bignerdranch.android.photogallery.domain.api

import com.bignerdranch.android.photogallery.domain.models.FlickrResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {

//    @GET("/")
//    fun fetchContents(): Call<String>

//    @GET(
//        "services/rest/?method=flickr.interestingness.getList" +
//                "&api_key=83db7d1f79298a5a5b43d6cdcaa02dd3" +
//                "&format=json" +
//                "&nojsoncallback=1" +
//                "&extras=url_s"
//    )
//    fun fetchPhotos(): Call<FlickrResponse>
    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
}