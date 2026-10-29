package com.example.githubui.db.dto

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

// Cópias dos dados no momento da ação. Sem chave estrangeira: o histórico
// deve continuar existindo depois que o item original for excluído.
@Entity(tableName = "atividades")
data class Atividade(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Embedded(prefix = "repo_")
    val repositorio: Repositorio,
    val acao: AcaoAtividade,
    val instante: Long = System.currentTimeMillis(),
    @Embedded(prefix = "issue_") val issue: Issue? = null
)
