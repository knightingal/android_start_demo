package com.example.jianming.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity
data class PicAlbumBean (
    @JsonProperty("name")
    var name: String,

    @JsonProperty("index")
    var serverIndex: Int = 0,

    var exist: Int = 0,

    @PrimaryKey
    var innerIndex: Long? = null,
    var mtime: String? = null,
    var cover: String? = null,
    var coverWidth: Int = 0,
    var coverHeight: Int = 0
)

