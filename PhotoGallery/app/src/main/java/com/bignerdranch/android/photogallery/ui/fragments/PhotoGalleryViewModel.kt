package com.bignerdranch.android.photogallery.ui.fragments


import android.app.Application
import androidx.lifecycle.*
import com.bignerdranch.android.photogallery.FlickrFetchr
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.domain.api.models.GalleryItem

//class PhotoGalleryViewModel : ViewModel() {
//    val galleryItemLiveData: LiveData<List<GalleryItem>>
//
//    init {
//        galleryItemLiveData = FlickrFetchr().fetchPhotos()
//    }
//
//}
//class ConcertViewModel(concertDao: ConcertDao) : ViewModel() {
//    val concertList: LiveData<PagedList<Concert>> =
//        concertDao.concertsByDate().toLiveData(pageSize = 50)
//}
class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    val galleryItemLiveData: LiveData<List<GalleryItem>>
    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)//"planets"
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            } else {
                flickrFetchr.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

}