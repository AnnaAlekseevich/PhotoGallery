package com.bignerdranch.android.photogallery


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList

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
class PhotoGalleryViewModel : ViewModel() {
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }

}