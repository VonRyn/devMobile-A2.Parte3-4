package com.example.githubui.ui.screens.home

import com.example.githubui.ui.components.BottomBar
import com.example.githubui.ui.components.MenuRow
import com.example.githubui.ui.components.SectionHeader
import com.example.githubui.ui.theme.Accent
import com.example.githubui.ui.theme.Green
import com.example.githubui.ui.theme.Ink

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onRepositorios: () -> Unit = {},
    onIssues: () -> Unit = {},
    onCriarRepositorio: () -> Unit = {},
    onCriarIssue: () -> Unit = {},
    exibirBarraInferior: Boolean = true
) {
    var menuCriarAberto by remember { mutableStateOf(false) }
    Surface(color = Color.White) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Home", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { menuCriarAberto = true }) {
                        Icon(Icons.Filled.AddCircleOutline, "Criar", tint = Accent, modifier = Modifier.size(24.dp))
                    }
                    DropdownMenu(expanded = menuCriarAberto, onDismissRequest = { menuCriarAberto = false }) {
                        DropdownMenuItem(
                            text = { Text("Criar issue") },
                            leadingIcon = { Icon(Icons.Outlined.Circle, null, tint = Green) },
                            onClick = { menuCriarAberto = false; onCriarIssue() }
                        )
                        DropdownMenuItem(
                            text = { Text("Criar repositório") },
                            leadingIcon = { Icon(Icons.Outlined.Book, null, tint = Ink) },
                            onClick = { menuCriarAberto = false; onCriarRepositorio() }
                        )
                    }
                }
            }
            SectionHeader(title = "Meu Trabalho")
            MenuRow(Icons.Outlined.Circle, Green, "Issues", onClick = onIssues)
            MenuRow(Icons.Outlined.Book, Ink, "Repositórios Principais", onClick = onRepositorios)
            Spacer(Modifier.weight(1f))
            if (exibirBarraInferior) BottomBar()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme { HomeScreen() }
}
