package com.example.githubui.ui.screens.repositorios

import com.example.githubui.ui.components.BottomBar
import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.LinkBlue
import com.example.githubui.ui.theme.Muted
import com.example.githubui.viewmodel.GithubViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RepositoriosPrincipaisScreen(model: GithubViewModel, onCriar: () -> Unit, onDetalhes: (Int) -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            items(model.repositorios, key = { it.id }) { repo ->
                Card(
                    onClick = { onDetalhes(repo.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Color(0xFFF0F1F3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Muted, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("<user_name>", color = Muted, fontSize = 14.sp)
                            Text(repo.nome, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCriar,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(56.dp),
            shape = CircleShape,
            containerColor = LinkBlue,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Criar repositório", modifier = Modifier.size(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RepositoriosTopBar(onVoltar: () -> Unit) {
    TopAppBar(
        title = { Text("Repositórios Principais", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Ink, navigationIconContentColor = Ink)
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun RepositoriosPrincipaisScreenPreview() {
    GithubPreview { model ->
        Scaffold(
            topBar = { RepositoriosTopBar(onVoltar = {}) },
            bottomBar = { BottomBar() },
            containerColor = Color.White
        ) { padding ->
            Box(Modifier.padding(padding)) {
                RepositoriosPrincipaisScreen(model, onCriar = {}, onDetalhes = {})
            }
        }
    }
}
