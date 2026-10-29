package com.example.githubui.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

internal data class RegistroState<T>(val valor: T? = null, val carregando: Boolean = true, val erro: String? = null)

// Cada entrada nos detalhes/edição consulta o DAO por ID. A revisão faz a
// consulta rodar novamente após uma gravação; só o Preview usa dados de exemplo.
@Composable
internal fun <T> registroDoBanco(id: Int, revisao: Long, preview: T?, buscar: suspend () -> T?): RegistroState<T> {
    val emPreview = LocalInspectionMode.current
    var estado by remember(id) { mutableStateOf(RegistroState(preview.takeIf { emPreview }, !emPreview)) }
    LaunchedEffect(id, revisao) {
        if (!emPreview) {
            estado = estado.copy(carregando = true, erro = null)
            estado = try {
                RegistroState(valor = buscar(), carregando = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RegistroState(carregando = false, erro = "Não foi possível carregar este item.")
            }
        }
    }
    return estado
}

@Composable
internal fun EstadoRegistro(carregando: Boolean, erro: String?, onVoltar: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (carregando) CircularProgressIndicator()
        else Text(erro ?: "Item não encontrado.")
        TextButton(onClick = onVoltar) { Text("Voltar") }
    }
}
