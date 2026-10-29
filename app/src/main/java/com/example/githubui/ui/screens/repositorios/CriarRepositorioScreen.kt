package com.example.githubui.ui.screens.repositorios

import com.example.githubui.db.dto.Repositorio
import com.example.githubui.ui.components.CampoSemBorda
import com.example.githubui.ui.preview.GithubPreview
import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.LinkBlue
import com.example.githubui.ui.theme.Muted
import com.example.githubui.viewmodel.GithubViewModel

import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CriarRepositorioScreen(model: GithubViewModel, onFechar: () -> Unit, onSalvo: () -> Unit) {
    FormularioRepositorio(model, null, onFechar, onSalvo)
}

@Composable
internal fun FormularioRepositorio(model: GithubViewModel, repo: Repositorio?, onFechar: () -> Unit, onSalvo: () -> Unit) {
    val scope = rememberCoroutineScope()
    var salvando by remember { mutableStateOf(false) }
    var etapa by rememberSaveable { mutableIntStateOf(0) }
    var nome by rememberSaveable(repo?.id) { mutableStateOf(repo?.nome ?: "") }
    var descricao by rememberSaveable(repo?.id) { mutableStateOf(repo?.descricao ?: "") }
    var linguagem by rememberSaveable(repo?.id) { mutableStateOf(repo?.linguagem ?: "") }
    var privado by rememberSaveable(repo?.id) { mutableStateOf(repo?.privado ?: false) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = etapa == 1) { etapa = 0; erro = null }
    BackHandler(enabled = salvando) {}

    Column(Modifier.fillMaxSize().background(Color.White).imePadding()) {
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 8.dp)
            .size(width = 50.dp, height = 4.dp).background(Color(0xFF94959C), RoundedCornerShape(2.dp)))
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(enabled = !salvando, onClick = { if (etapa == 0) onFechar() else { etapa = 0; erro = null } }) {
                Icon(if (etapa == 0) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (etapa == 0) "Fechar" else "Voltar", tint = Ink)
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(if (repo == null) "Novo repositório" else "Editar repositório", color = Muted, fontSize = 16.sp)
                Text(if (etapa == 0) "Geral" else "Configuração", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(enabled = !salvando && !model.ocupado, onClick = {
                erro = model.validarRepositorio(repo?.id, nome, descricao)
                if (erro == null) {
                    if (etapa == 0) etapa = 1
                    else {
                        salvando = true
                        scope.launch {
                            try {
                                erro = model.salvarRepositorio(repo?.id, nome, descricao, linguagem, privado)
                                if (erro == null) onSalvo()
                            } finally { salvando = false }
                        }
                    }
                }
            }) {
                Text(if (salvando) "SALVANDO" else if (etapa == 0) "AVANÇAR" else if (repo == null) "CRIAR" else "SALVAR", color = Color(0xFF2863B6), fontSize = 14.sp)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (etapa == 0) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Proprietário", color = Ink, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Box(Modifier.size(36.dp).background(Color(0xFFF0F1F3), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Muted, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("<user_name>", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Column(Modifier.padding(horizontal = 18.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Nome do repositório", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    CampoSemBorda(nome, { if (it.length <= 100) { nome = it; erro = null } }, "Insira um nome de repositório", "Nome do repositório", singleLine = true)
                    Text("${nome.length}/100 caracteres", color = Muted, fontSize = 14.sp)
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Column(Modifier.padding(horizontal = 18.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Descrição (opcional)", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    CampoSemBorda(descricao, { if (it.length <= 350) { descricao = it; erro = null } }, "Insira uma descrição", "Descrição")
                    Text("${descricao.length}/350 caracteres", color = Muted, fontSize = 14.sp)
                }
            } else {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(nome, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("Visibilidade", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Column(Modifier.selectableGroup()) {
                        OpcaoVisibilidade("Público", "Qualquer pessoa pode ver este repositório.", !privado) { privado = false }
                        OpcaoVisibilidade("Privado", "Você escolhe quem pode ver este repositório.", privado) { privado = true }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Text("Linguagem (opcional)", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    CampoSemBorda(linguagem, { linguagem = it }, "Ex.: Kotlin", "Linguagem", singleLine = true)
                }
            }
            erro?.let { Text(it, color = Color(0xFFB42318), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
            repeat(2) { indice ->
                Box(Modifier.size(10.dp).background(if (indice == etapa) Color.Black else Color(0xFF94959C), CircleShape)
                    .semantics { contentDescription = "Etapa ${indice + 1} de 2${if (indice == etapa) ", atual" else ""}" })
            }
        }
    }
}

@Composable
private fun OpcaoVisibilidade(titulo: String, descricao: String, selecionado: Boolean, onSelecionar: () -> Unit) {
    Row(Modifier.fillMaxWidth().selectable(selected = selecionado, onClick = onSelecionar, role = Role.RadioButton)
        .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selecionado, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = LinkBlue))
        Column(Modifier.padding(start = 12.dp)) {
            Text(titulo, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(descricao, color = Muted, fontSize = 14.sp)
        }
    }
}

@Preview(name = "Novo repositório - Geral", showBackground = true, widthDp = 393, heightDp = 820)
@Composable
private fun CriarRepositorioScreenPreview() {
    GithubPreview { model -> CriarRepositorioScreen(model, onFechar = {}, onSalvo = {}) }
}
