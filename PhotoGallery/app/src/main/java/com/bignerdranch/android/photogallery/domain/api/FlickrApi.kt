package com.bignerdranch.android.photogallery.domain.api

import com.bignerdranch.android.photogallery.domain.models.PhotosSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

const val FLICKR_API_KEY = "83db7d1f79298a5a5b43d6cdcaa02dd3"

interface ApiService {
    @GET("?method=flickr.photos.search&format=json&nojsoncallback=1&text=dogs&api_key=$FLICKR_API_KEY")
    suspend fun fetchImages(): PhotosSearchResponse

    @GET("?method=flickr.photos.search&format=json&nojsoncallback=1&api_key=$FLICKR_API_KEY")
    suspend fun fetchImages(@Query(value = "text") searchTerm: String): PhotosSearchResponse
}

//interface FlickrApi {
//
////    @GET("/")
////    fun fetchContents(): Call<String>
//
////    @GET(
////        "services/rest/?method=flickr.interestingness.getList" +
////                "&api_key=83db7d1f79298a5a5b43d6cdcaa02dd3" +
////                "&format=json" +
////                "&nojsoncallback=1" +
////                "&extras=url_s"
////    )
////    fun fetchPhotos(): Call<FlickrResponse>
//    @GET("services/rest?method=flickr.interestingness.getList")
//    fun fetchPhotos(): Call<FlickrResponse>
//
//    @GET
//    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
//
//    @GET("services/rest?method=flickr.photos.search")
//    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
//}