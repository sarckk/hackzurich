package com.example.addictionapp.data.models

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName="blacklist_table",
    indices = [Index("name")]
)
data class Application(
    @PrimaryKey
    val name: String,
    val packageName: String,
)