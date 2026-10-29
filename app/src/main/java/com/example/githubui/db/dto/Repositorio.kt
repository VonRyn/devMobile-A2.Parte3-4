package com.example.githubui.db.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repositorios")
data class Repositorio(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val descricao: String,
    val linguagem: String,
    val privado: Boolean
)
