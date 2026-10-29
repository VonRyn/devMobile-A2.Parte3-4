package com.example.githubui.ui.screens.issues

import com.example.githubui.ui.components.BottomBar
import com.example.githubui.ui.components.ConfirmarExclusao
import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.ui.theme.Green
import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.Line
import com.example.githubui.ui.theme.LinkBlue
import com.example.githubui.ui.theme.Muted
import com.example.githubui.viewmodel.GithubViewModel

import com.example.githubui.ui.components.registroDoBanco
import com.example.githubui.ui.components.EstadoRegistro
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetalhesIssueScreen(
    model: GithubViewModel,
    id: Int,
    onRepositorio: (Int) -> Unit,
    onExcluido: () -> Unit,
    onEditar: (Int) -> Unit,
    onVoltar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var alterando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val preview = model.issues.find { it.id == id }?.let { it to model.repositorios.find { repo -> repo.id == it.repositorioId } }
    val estado = registroDoBanco(id, model.revisao, preview) {
        model.buscarIssue(id)?.let { it to model.buscarRepositorio(it.repositorioId) }
    }
    val issue = estado.valor?.first
    val repo = estado.valor?.second
    var menu by remember { mutableStateOf(false) }
    var confirmar by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Ink)
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                if (repo != null) {
                    Text(repo.nome, color = Muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("Issue #$id", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            if (issue != null) Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Outlined.MoreVert, "Opções da issue", tint = LinkBlue)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Editar issue") }, onClick = { menu = false; onEditar(id) })
                    DropdownMenuItem(text = { Text("Excluir issue") }, onClick = { menu = false; confirmar = true })
                }
            }
        }
        HorizontalDivider(color = Line.copy(alpha = 0.5f))
        if (issue == null) {
            EstadoRegistro(estado.carregando, estado.erro, onVoltar)
        } else {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(issue.titulo, color = Ink, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                    Row(
                        Modifier.background(if (issue.concluida) Color(0xFF8250DF) else Green, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(if (issue.concluida) Icons.Outlined.CheckCircleOutline else Icons.Outlined.Adjust,
                            null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(if (issue.concluida) "Concluída" else "Aberta", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(30.dp).background(Color(0xFFF0F1F3), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Person, null, tint = Muted, modifier = Modifier.size(20.dp))
                        }
                        Text("<user_name>", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Descrição", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(issue.descricao.ifBlank { "Sem descrição." }, color = Ink, fontSize = 16.sp, lineHeight = 24.sp)
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Column(
                    Modifier.fillMaxWidth().clickable(enabled = repo != null) { onRepositorio(issue.repositorioId) }
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Repositório", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(repo?.nome ?: "Repositório removido", color = if (repo != null) LinkBlue else Muted, fontSize = 16.sp)
                }
                OutlinedButton(
                    enabled = !alterando && !model.ocupado,
                    onClick = {
                        alterando = true
                        scope.launch {
                            try { erro = model.alternarIssue(id) }
                            finally { alterando = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(18.dp).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Line),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                ) {
                    Icon(if (issue.concluida) Icons.Outlined.Adjust else Icons.Outlined.CheckCircleOutline,
                        null, tint = if (issue.concluida) Green else Color(0xFF8250DF), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (issue.concluida) "Reabrir issue" else "Concluir issue")
                }
            }
        }
    }
    if (erro != null) AlertDialog(onDismissRequest = { erro = null }, title = { Text("Não foi possível alterar a issue") },
        text = { Text(erro.orEmpty()) }, confirmButton = { TextButton(onClick = { erro = null }) { Text("OK") } })
    if (confirmar && issue != null) ConfirmarExclusao("Excluir issue?", issue.titulo, { confirmar = false }, ocupado = alterando) {
        if (!alterando) {
            alterando = true
            scope.launch {
                try {
                    erro = model.excluirIssue(id)
                    confirmar = false
                    if (erro == null) onExcluido()
                } finally { alterando = false }
            }
        }
    }
}

@Preview(name = "Issue aberta", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun DetalhesIssueScreenPreview() {
    GithubPreview { model -> DetalhesIssuePreviewContent(model, id = 2) }
}

@Preview(name = "Issue concluída", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun DetalhesIssueConcluidaScreenPreview() {
    GithubPreview { model -> DetalhesIssuePreviewContent(model, id = 1) }
}

@Composable
private fun DetalhesIssuePreviewContent(model: GithubViewModel, id: Int) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Box(Modifier.weight(1f)) {
            DetalhesIssueScreen(model, id, onRepositorio = {}, onExcluido = {}, onEditar = {}, onVoltar = {})
        }
        BottomBar()
    }
}
