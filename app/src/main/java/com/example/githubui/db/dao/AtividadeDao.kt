package com.example.githubui.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.githubui.db.dto.Atividade

@Dao
interface AtividadeDao {
    @Insert
    suspend fun inserir(atividade: Atividade): Long

    @Query("SELECT * FROM atividades ORDER BY id DESC")
    suspend fun listarTodos(): List<Atividade>
}
