package com.example.githubui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.githubui.db.dto.Repositorio

@Dao
interface RepositorioDao {

    @Insert
    suspend fun inserir(repositorio: Repositorio): Long

    @Query("" +
            "SELECT * FROM repositorios " +
            "ORDER BY nome ASC"
    )
    suspend fun listarTodos(): List<Repositorio>

    @Query("" +
            "SELECT * FROM repositorios " +
            "WHERE id = :id"
    )
    suspend fun buscarPorId(id: Int): Repositorio?

    @Query("SELECT * FROM repositorios WHERE nome COLLATE NOCASE = :nome LIMIT 1")
    suspend fun buscarPorNome(nome: String): Repositorio?

    @Update
    suspend fun atualizar(repositorio: Repositorio)

    @Delete
    suspend fun excluir(repositorio: Repositorio)

}