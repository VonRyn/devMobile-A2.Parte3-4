package com.example.githubui.ui.screens.issues

import com.example.githubui.db.dto.Issue
import com.example.githubui.ui.components.CampoSemBorda
import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.Muted
import com.example.githubui.viewmodel.GithubViewModel

import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CriarIssueScreen(
    model: GithubViewModel,
    repositorioId: Int? = null,
    onFechar: () -> Unit,
    onSalvo: (Int) -> Unit
) {
    FormularioIssue(model, repositorioId, null, onFechar, onSalvo)
}

@Composable
internal fun FormularioIssue(model: GithubViewModel, repositorioId: Int?, issue: Issue?, onFechar: () -> Unit, onSalvo: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var salvando by remember { mutableStateOf(false) }
    BackHandler(enabled = salvando) {}
    var repoId by rememberSaveable(issue?.id) { mutableStateOf(issue?.repositorioId ?: repositorioId ?: model.repositorios.firstOrNull()?.id) }
    var titulo by rememberSaveable(issue?.id) { mutableStateOf(issue?.titulo ?: "") }
    var descricao by rememberSaveable(issue?.id) { mutableStateOf(issue?.descricao ?: "") }
    var menu by remember { mutableStateOf(false) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    val selecionado = model.repositorios.find { it.id == repoId }

    Column(Modifier.fillMaxSize().background(Color.White).imePadding()) {
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 8.dp)
            .size(width = 50.dp, height = 4.dp).background(Color(0xFF94959C), RoundedCornerShape(2.dp)))
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(enabled = !salvando, onClick = onFechar) { Icon(Icons.Default.Close, "Fechar", tint = Ink) }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(if (issue == null) "Nova issue" else "Editar issue", color = Muted, fontSize = 16.sp)
                Text("Geral", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(enabled = !salvando && !model.ocupado, onClick = {
                erro = when {
                    selecionado == null -> "Selecione um repositório."
                    titulo.isBlank() -> "Informe o título da issue."
                    else -> null
                }
                if (erro == null && selecionado != null) {
                    salvando = true
                    scope.launch {
                        try {
                            erro = if (issue == null) model.adicionarIssue(selecionado.id, titulo, descricao)
                                else model.editarIssue(issue.id, selecionado.id, titulo, descricao)
                            if (erro == null) onSalvo(selecionado.id)
                        } finally { salvando = false }
                    }
                }
            }) { Text(if (salvando) "SALVANDO" else if (issue == null) "CRIAR" else "SALVAR", color = Color(0xFF2863B6), fontSize = 14.sp) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box {
                Row(
                    Modifier.fillMaxWidth().clickable { menu = true }.padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Repositório", color = Ink, fontSize = 16.sp)
                    Spacer(Modifier.width(18.dp))
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).background(Color(0xFFF0F1F3), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Book, null, tint = Muted, modifier = Modifier.size(22.dp))
                        }
                        Text(selecionado?.nome ?: "Selecionar", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp).weight(1f, fill = false))
                        Icon(Icons.Outlined.KeyboardArrowDown, "Selecionar repositório", tint = Muted)
                    }
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (model.repositorios.isEmpty()) DropdownMenuItem(text = { Text("Nenhum repositório disponível") }, onClick = {}, enabled = false)
                    model.repositorios.forEach { repo ->
                        DropdownMenuItem(text = { Text(repo.nome) }, onClick = { repoId = repo.id; menu = false; erro = null })
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(Modifier.padding(horizontal = 18.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Título da issue", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                CampoSemBorda(titulo, { if (it.length <= 100) { titulo = it; erro = null } }, "Insira um título para a issue", "Título da issue", singleLine = true)
                Text("${titulo.length}/100 caracteres", color = Muted, fontSize = 14.sp)
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(Modifier.padding(horizontal = 18.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Descrição (opcional)", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                CampoSemBorda(descricao, { if (it.length <= 350) { descricao = it; erro = null } }, "Insira uma descrição", "Descrição")
                Text("${descricao.length}/350 caracteres", color = Muted, fontSize = 14.sp)
            }
            erro?.let { Text(it, color = Color(0xFFB42318), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) }
        }
    }
}

@Preview(name = "Nova issue - Geral", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun CriarIssueScreenPreview() {
    GithubPreview { model -> CriarIssueScreen(model, repositorioId = 1, onFechar = {}, onSalvo = {}) }
}
