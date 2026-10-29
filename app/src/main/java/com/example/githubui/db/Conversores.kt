package com.example.githubui.db

import androidx.room.TypeConverter
import com.example.githubui.db.dto.AcaoAtividade

class Conversores {
    @TypeConverter
    fun acaoParaTexto(acao: AcaoAtividade): String = acao.name

    @TypeConverter
    fun textoParaAcao(texto: String): AcaoAtividade = AcaoAtividade.valueOf(texto)
}
