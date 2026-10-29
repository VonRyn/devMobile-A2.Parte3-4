package com.example.githubui.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun ConfirmarExclusao(titulo: String, mensagem: String, onCancelar: () -> Unit, ocupado: Boolean = false, onConfirmar: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!ocupado) onCancelar() }, title = { Text(titulo) }, text = { Text(mensagem) },
        confirmButton = { TextButton(enabled = !ocupado, onClick = onConfirmar) { Text(if (ocupado) "Excluindo" else "Excluir") } },
        dismissButton = { TextButton(enabled = !ocupado, onClick = onCancelar) { Text("Cancelar") } })
}
