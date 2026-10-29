package com.example.githubui.db.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val repositorioId: Int,
    val titulo: String,
    val descricao: String,
    val concluida: Boolean = false
)
