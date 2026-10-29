package com.example.githubui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.githubui.db.dto.Issue

@Dao
interface IssueDao {

    @Insert
    suspend fun inserir(issue: Issue): Long

    @Query("" +
            "SELECT * FROM issues " +
            "ORDER BY titulo ASC"
    )
    suspend fun listarTodos(): List<Issue>

    @Query("""
        SELECT * FROM issues
        WHERE repositorioId = :repositorioId
        ORDER BY titulo ASC
    """)
    suspend fun listarPorRepositorio(repositorioId: Int): List<Issue>

    @Query("" +
            "SELECT * FROM issues " +
            "WHERE id = :id"
    )
    suspend fun buscarPorId(id: Int): Issue?

    @Update
    suspend fun atualizar(issue: Issue)

    @Delete
    suspend fun excluir(issue: Issue)

}