package com.example.githubui.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.githubui.db.dto.*
import com.example.githubui.ui.theme.GithubuiTheme
import com.example.githubui.viewmodel.GithubViewModel

// Apenas o Preview cria um ViewModel sem Repository. Nunca abre ou grava o banco.
@Composable
internal fun GithubPreview(content: @Composable (GithubViewModel) -> Unit) {
    val model = remember {
        GithubViewModel(null).apply {
            val repo = Repositorio(1, "meu-app", "Aplicativo de estudos com Kotlin e Jetpack Compose.", "Kotlin", false)
            repositorios.addAll(listOf(repo, Repositorio(2, "anotacoes", "Anotações das aulas.", "Markdown", true)))
            val concluida = Issue(1, 1, "Criar navegação", "Conectar as telas com Navigation Compose.", true)
            val aberta = Issue(2, 1, "Revisar formulário", "Verificar os campos obrigatórios.")
            issues.addAll(listOf(concluida, aberta))
            atividades.addAll(listOf(
                Atividade(3, repo, AcaoAtividade.CONCLUIDO, issue = concluida),
                Atividade(2, repo, AcaoAtividade.CRIADO, issue = aberta),
                Atividade(1, repo, AcaoAtividade.CRIADO)
            ))
        }
    }
    GithubuiTheme(darkTheme = false, dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize()) { content(model) }
    }
}
