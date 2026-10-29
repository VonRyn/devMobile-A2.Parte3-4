package com.example.githubui.db.dto

enum class AcaoAtividade(val texto: String, val filtro: String) {
    CRIADO("criou", "Criados"),
    EDITADO("editou", "Editados"),
    EXCLUIDO("excluiu", "Excluídos"),
    CONCLUIDO("concluiu", "Concluídas"),
    REABERTO("reabriu", "Reabertas")
}
