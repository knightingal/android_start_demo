package com.example.jianming.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
data class PicSectionBean (
    @JsonProperty("name")
    var name: String,

    @PrimaryKey
    @JsonProperty("index")
    var id: Long = 0,

    var exist: Int = 0,

    var mtime: String? = null,
    var cover: String? = null,
    var coverWidth: Int = 0,
    var coverHeight: Int = 0,

    var album: String? = null,

    var clientStatus: ClientStatus
) {
    enum class ClientStatus {
        NONE,
        PENDING,
        LOCAL
    }
}



