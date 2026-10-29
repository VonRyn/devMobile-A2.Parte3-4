package com.example.githubui.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.githubui.db.dao.AtividadeDao
import com.example.githubui.db.dao.IssueDao
import com.example.githubui.db.dao.RepositorioDao
import com.example.githubui.db.dto.Atividade
import com.example.githubui.db.dto.Issue
import com.example.githubui.db.dto.Repositorio

@Database(entities = [Repositorio::class, Issue::class, Atividade::class], version = 2, exportSchema = true)
@TypeConverters(Conversores::class)
abstract class GithubDatabase : RoomDatabase() {
    abstract fun repositorioDao(): RepositorioDao
    abstract fun issueDao(): IssueDao
    abstract fun atividadeDao(): AtividadeDao

    companion object {
        @Volatile
        private var INSTANCE: GithubDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS atividades (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        repo_id INTEGER NOT NULL,
                        repo_nome TEXT NOT NULL,
                        repo_descricao TEXT NOT NULL,
                        repo_linguagem TEXT NOT NULL,
                        repo_privado INTEGER NOT NULL,
                        acao TEXT NOT NULL,
                        instante INTEGER NOT NULL,
                        issue_id INTEGER,
                        issue_repositorioId INTEGER,
                        issue_titulo TEXT,
                        issue_descricao TEXT,
                        issue_concluida INTEGER
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): GithubDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                GithubDatabase::class.java,
                "github_database"
            ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
        }
    }
}
