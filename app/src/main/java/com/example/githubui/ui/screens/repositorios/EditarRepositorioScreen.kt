package com.example.githubui.ui.screens.repositorios

import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.viewmodel.GithubViewModel

import com.example.githubui.ui.components.registroDoBanco
import com.example.githubui.ui.components.EstadoRegistro
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EditarRepositorioScreen(model: GithubViewModel, id: Int, onFechar: () -> Unit, onSalvo: () -> Unit) {
    val estado = registroDoBanco(id, model.revisao, model.repositorios.find { it.id == id }) { model.buscarRepositorio(id) }
    val repo = estado.valor
    if (repo == null) {
        EstadoRegistro(estado.carregando, estado.erro, onFechar)
    } else FormularioRepositorio(model, repo, onFechar, onSalvo)
}

@Preview(name = "Editar repositório", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun EditarRepositorioScreenPreview() {
    GithubPreview { model -> EditarRepositorioScreen(model, id = 1, onFechar = {}, onSalvo = {}) }
}
