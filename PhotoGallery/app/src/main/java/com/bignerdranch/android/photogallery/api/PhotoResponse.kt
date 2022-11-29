package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.GalleryItem
import com.google.gson.annotations.SerializedName

data class PhotoResponse(
    @SerializedName("photo")
    val galleryItems: List<GalleryItem>
)