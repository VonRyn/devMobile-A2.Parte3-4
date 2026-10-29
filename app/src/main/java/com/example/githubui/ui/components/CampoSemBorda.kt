package com.example.githubui.ui.components

import com.example.githubui.ui.theme.Ink
import com.example.githubui.ui.theme.LinkBlue
import com.example.githubui.ui.theme.Muted

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CampoSemBorda(valor: String, onValor: (String) -> Unit, placeholder: String, rotulo: String, singleLine: Boolean = false) {
    BasicTextField(
        value = valor,
        onValueChange = onValor,
        modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp).semantics { contentDescription = rotulo },
        singleLine = singleLine,
        textStyle = TextStyle(color = Ink, fontSize = 16.sp, lineHeight = 22.sp),
        cursorBrush = SolidColor(LinkBlue),
        decorationBox = { campo ->
            Box(Modifier.padding(vertical = 4.dp)) {
                if (valor.isEmpty()) Text(placeholder, color = Muted, fontSize = 16.sp)
                campo()
            }
        }
    )
}
