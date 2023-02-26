package com.bignerdranch.android.photogallery.ui.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.domain.api.WebClient
import com.bignerdranch.android.photogallery.domain.models.Photo
import com.bignerdranch.android.photogallery.ui.fragments.adapter.PhotosAdapter
import kotlinx.coroutines.launch

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val mutableSearchTerm = MutableLiveData<String>()
    private val mutablePhotosListLiveData = MutableLiveData<List<Photo>>()
    private val photosListLiveData: LiveData<List<Photo>> = mutablePhotosListLiveData

    var photosAdapter = PhotosAdapter()

    fun loadPhotos(): LiveData<List<Photo>> {

        viewModelScope.launch {

            val searchResponse = WebClient.client.fetchImages()
            val photosList = searchResponse.photos.photo.map { photo ->
                Photo(
                    id = photo.id,
                    url = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
                    title = photo.title
                )
            }
            mutablePhotosListLiveData.postValue(photosList)
        }
        return photosListLiveData
    }

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

    suspend fun fetchImages(searchTerm: String): List<Photo> {
        if (searchTerm.isBlank()) {
            return emptyList()
        }
        val searchResponse = WebClient.client.fetchImages(searchTerm)
        return searchResponse.photos.photo.map { photo ->
            Photo(
                id = photo.id,
                url = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
                title = photo.title
            )
        }
    }

}