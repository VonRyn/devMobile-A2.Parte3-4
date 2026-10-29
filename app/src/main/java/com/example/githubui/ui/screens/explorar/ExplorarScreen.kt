package com.example.githubui.ui.screens.explorar

import com.example.githubui.db.dto.AcaoAtividade
import com.example.githubui.db.dto.Atividade
import com.example.githubui.ui.components.BottomBar
import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.ui.theme.Green
import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.Muted
import com.example.githubui.viewmodel.GithubViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val FundoAtividade = Color(0xFFF0F1F6)

@Composable
fun ExplorarScreen(model: GithubViewModel, onRepositorio: (Int) -> Unit, onIssue: (Int) -> Unit) {
    var filtro by rememberSaveable { mutableStateOf<AcaoAtividade?>(null) }
    var menuAberto by remember { mutableStateOf(false) }
    var agora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            agora = System.currentTimeMillis()
        }
    }
    val atividades = model.atividades.filter { filtro == null || it.acao == filtro }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(FundoAtividade),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp)) {
                Text("Explorar", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 32.dp))
                Text("Descubra", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 18.dp))
                DescobertaRow(Icons.Outlined.LocalFireDepartment, Color(0xFFC44752), "Repositórios em Alta")
                DescobertaRow(Icons.Outlined.SentimentSatisfiedAlt, Color(0xFF8965C7), "Listas Incríveis")
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Atividade", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                filtro?.let { Text(it.filtro, color = Muted, fontSize = 12.sp) }
                Box {
                    IconButton(onClick = { menuAberto = true }) {
                        Icon(Icons.Outlined.FilterList, "Filtrar atividades", tint = Muted)
                    }
                    DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                        DropdownMenuItem(text = { Text("Todas") }, onClick = { filtro = null; menuAberto = false })
                        AcaoAtividade.entries.forEach { acao ->
                            DropdownMenuItem(text = { Text(acao.filtro) }, onClick = { filtro = acao; menuAberto = false })
                        }
                    }
                }
            }
        }
        if (atividades.isEmpty()) item {
            Text("Nenhuma atividade encontrada.", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        }
        items(atividades, key = { it.id }) { atividade ->
            val issue = atividade.issue
            AtividadeCard(
                atividade = atividade,
                agora = agora,
                existe = if (issue == null) model.repositorios.any { it.id == atividade.repositorio.id }
                    else model.issues.any { it.id == issue.id },
                onAbrir = { if (issue == null) onRepositorio(atividade.repositorio.id) else onIssue(issue.id) }
            )
        }
    }
}

@Composable
private fun DescobertaRow(icon: ImageVector, cor: Color, texto: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(cor, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(18.dp))
        Text(texto, color = Ink, fontSize = 17.sp)
    }
}

@Composable
private fun AtividadeCard(atividade: Atividade, agora: Long, existe: Boolean, onAbrir: () -> Unit) {
    val repo = atividade.repositorio
    val issue = atividade.issue
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(Color(0xFFDDE3EB), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Person, null, tint = Muted, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("Você", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(" ${atividade.acao.texto} ${if (issue == null) "um repositório" else "uma issue"}", color = Muted, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(tempoDecorrido(atividade.instante, agora), color = Muted, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Card(
            onClick = onAbrir,
            enabled = existe,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White, disabledContainerColor = Color.White,
                contentColor = Ink, disabledContentColor = Ink)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (issue == null) Icons.Outlined.Book else if (issue.concluida) Icons.Outlined.CheckCircleOutline else Icons.Outlined.Adjust,
                        null, tint = if (issue == null) Muted else if (issue.concluida) Color(0xFF8250DF) else Green, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(issue?.titulo ?: repo.nome, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                val descricao = issue?.descricao ?: repo.descricao
                if (descricao.isNotBlank()) Text(descricao, color = Color(0xFF51565C), fontSize = 16.sp, lineHeight = 22.sp)
                if (issue != null) {
                    Text("${repo.nome} #${issue.id} · ${if (issue.concluida) "Concluída" else "Aberta"}", color = Muted, fontSize = 13.sp)
                } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (repo.linguagem.isNotBlank()) {
                        Box(Modifier.size(9.dp).background(Color(0xFF8965C7), CircleShape))
                        Text(repo.linguagem, color = Muted, fontSize = 13.sp)
                    }
                    Text(if (repo.privado) "Privado" else "Público", color = Muted, fontSize = 13.sp)
                }
                if (!existe) Text(if (issue == null) "Repositório excluído" else "Issue excluída", color = Muted, fontSize = 13.sp)
            }
        }
    }
}

internal fun tempoDecorrido(instante: Long, agora: Long): String {
    val minutos = ((agora - instante).coerceAtLeast(0) / 60_000)
    return when {
        minutos < 1 -> "agora"
        minutos < 60 -> "${minutos}min"
        minutos < 1440 -> "${minutos / 60}h"
        else -> "${minutos / 1440}d"
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun ExplorarScreenPreview() {
    GithubPreview { model ->
        Column(Modifier.fillMaxSize().background(Color.White)) {
            Box(Modifier.weight(1f)) { ExplorarScreen(model, onRepositorio = {}, onIssue = {}) }
            BottomBar()
        }
    }
}
