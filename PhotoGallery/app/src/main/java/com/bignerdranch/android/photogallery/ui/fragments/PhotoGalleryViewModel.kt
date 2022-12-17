package com.bignerdranch.android.photogallery.ui.fragments


import android.app.Application
import androidx.lifecycle.*
import com.bignerdranch.android.photogallery.FlickrFetcher
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.domain.models.GalleryItem

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
    private val flickrFetcher = FlickrFetcher()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)//"planets"
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetcher.fetchPhotos()
            } else {
                flickrFetcher.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

}