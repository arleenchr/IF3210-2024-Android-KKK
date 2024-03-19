package com.example.bondoman.room.Converter

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

class PlaceTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromPlace(place: Place): String {
        val placeJson = JSONObject().apply {
            put("name", place.name)
            put("address", place.address ?: "") // Default value is an empty string
            put("latitude", place.latLng?.latitude ?: 0.0) // Default value is 0.0
            put("longitude", place.latLng?.longitude ?: 0.0) // Default value is 0.0
        }
        println(placeJson.toString())
        return placeJson.toString()
    }

    @TypeConverter
    fun toPlace(placeJson: String): Place {
        val jsonObject = gson.fromJson(placeJson, JsonObject::class.java)
        val placeBuilder = Place.builder()

        val name = jsonObject.get("name").asString
        val address = jsonObject.get("address").asString
        val latitude = jsonObject.get("latitude").asDouble
        val longitude = jsonObject.get("longitude").asDouble

        return placeBuilder
            .setName(name)
            .setAddress(address)
            .setLatLng(LatLng(latitude, longitude))
            .build()
    }
}