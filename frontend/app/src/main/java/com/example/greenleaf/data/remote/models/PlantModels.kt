package com.example.greenleaf.data.remote.models
import com.example.greenleaf.data.local.entities.PlantEntity
import com.google.gson.annotations.SerializedName

data class PlantRequest(
    @SerializedName("common_name") val commonName: String,
    @SerializedName("scientific_name") val scientificName: String,
    @SerializedName("habitat") val habitat: String,
    @SerializedName("origin") val origin: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("plant_image") val plantImage: String?  // URL or base64 string
)

data class PlantResponse(
    val id: Int,

    @SerializedName("plant_image")
    val plantImage: String?,

    @SerializedName("common_name")
    val commonName: String,

    @SerializedName("scientific_name")
    val scientificName: String,

    val habitat: String,

    val origin: String?,

    val description: String?,

    @SerializedName("created_by")
    val createdBy: Int
)

fun PlantResponse.toEntity(): PlantEntity = PlantEntity(
    id = this.id,
    plantImage = this.plantImage,
    commonName = this.commonName,
    scientificName = this.scientificName,
    habitat = this.habitat,
    origin = this.origin,
    description = this.description,
    isSynced = true // fetched from backend, so synced
)
