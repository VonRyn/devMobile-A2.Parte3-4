package com.example.githubui.ui.screens.issues

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
fun EditarIssueScreen(model: GithubViewModel, id: Int, onFechar: () -> Unit, onSalvo: () -> Unit) {
    val estado = registroDoBanco(id, model.revisao, model.issues.find { it.id == id }) { model.buscarIssue(id) }
    val issue = estado.valor
    if (issue == null) {
        EstadoRegistro(estado.carregando, estado.erro, onFechar)
    } else FormularioIssue(model, issue.repositorioId, issue, onFechar) { onSalvo() }
}

@Preview(name = "Editar issue", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun EditarIssueScreenPreview() {
    GithubPreview { model -> EditarIssueScreen(model, id = 1, onFechar = {}, onSalvo = {}) }
}
