package com.example.githubui.ui.screens.issues

import com.example.githubui.ui.components.BottomBar
import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.ui.theme.Green
import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.Line
import com.example.githubui.ui.theme.LinkBlue
import com.example.githubui.ui.theme.Muted
import com.example.githubui.viewmodel.GithubViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IssuesScreen(
    model: GithubViewModel,
    repositorioId: Int? = null,
    onCriar: () -> Unit,
    onVoltar: () -> Unit,
    onDetalhes: (Int) -> Unit
) {

    val repositorio = model.repositorios.find { it.id == repositorioId }
    val issuesVisiveis = model.issues.filter { repositorioId == null || it.repositorioId == repositorioId }
    Column(Modifier.fillMaxSize().background(Color(0xFFF0F1F6))) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Ink) }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                repositorio?.let { Text(it.nome, color = Muted, fontSize = 14.sp) }
                Text("Issues", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onCriar) {
                Icon(Icons.Outlined.AddCircleOutline, "Nova issue", tint = LinkBlue, modifier = Modifier.size(26.dp))
            }
        }
        HorizontalDivider(color = Line.copy(alpha = 0.5f))
        LazyColumn(Modifier.fillMaxSize()) {
            items(issuesVisiveis, key = { it.id }) { issue ->
                Card(
                    onClick = { onDetalhes(issue.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.Top) {
                        Icon(if (issue.concluida) Icons.Outlined.CheckCircleOutline else Icons.Outlined.Adjust,
                            contentDescription = if (issue.concluida) "Concluída" else "Aberta",
                            tint = if (issue.concluida) Color(0xFF8250DF) else Green,
                            modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(issue.titulo, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            val nome = model.repositorios.find { it.id == issue.repositorioId }?.nome.orEmpty()
                            Text(if (repositorioId == null) "$nome #${issue.id}" else "#${issue.id}", color = Muted, fontSize = 13.sp)
                        }
                    }
                }
                HorizontalDivider(color = Line.copy(alpha = 0.5f))
            }
        }
    }

}

@Preview(name = "Issues", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun IssuesScreenPreview() {
    GithubPreview { model ->
        Column(Modifier.fillMaxSize().background(Color.White)) {
            Box(Modifier.weight(1f)) { IssuesScreen(model, repositorioId = 1, onCriar = {}, onVoltar = {}, onDetalhes = {}) }
            BottomBar()
        }
    }
}

@Preview(name = "Issues - lista vazia", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun IssuesVaziasScreenPreview() {
    GithubPreview { model ->
        Column(Modifier.fillMaxSize().background(Color.White)) {
            Box(Modifier.weight(1f)) { IssuesScreen(model, repositorioId = 2, onCriar = {}, onVoltar = {}, onDetalhes = {}) }
            BottomBar()
        }
    }
}
