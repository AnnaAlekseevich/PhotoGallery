package com.bignerdranch.android.photogallery

import android.util.Log
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.google.gson.*
import java.lang.reflect.Type

private const val TAG = "PhotoDeserializer"

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
//        json as JsonObject
//        val photos = json.get("photos").asJsonObject
        val photos = json.asJsonObject.getAsJsonObject("photos")

        Log.d(TAG, "Response photo - deserialize" + Gson().fromJson(json.asJsonObject.getAsJsonObject("photos"),
            PhotoResponse::class.java).galleryItems)


// Вытяните объект фотографий из JsonElement
// и преобразуйте его в объект PhotoResponse

        return Gson().fromJson(photos, PhotoResponse::class.java)
    }
}