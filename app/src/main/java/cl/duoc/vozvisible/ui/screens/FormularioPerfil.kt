package cl.duoc.vozvisible.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.PerfilUsuario

// estas partes las usan el registro y la pantalla de perfil, por eso están aparte

val REGIONES = listOf(
    "Región Metropolitana",
    "Valparaíso",
    "Biobío",
    "La Araucanía",
    "Antofagasta",
    "Los Lagos"
)

val APOYOS = listOf(
    "Subtítulos automáticos",
    "Alerta por vibración",
    "Alto contraste",
    "Texto ampliado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorRegion(region: String, alCambiar: (String) -> Unit) {
    var desplegado by remember { mutableStateOf(false) }

    Text(
        text = "Región",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(8.dp))
    ExposedDropdownMenuBox(
        expanded = desplegado,
        onExpandedChange = { desplegado = !desplegado }
    ) {
        OutlinedTextField(
            value = region,
            onValueChange = {},
            readOnly = true,
            label = { Text("Selecciona tu región") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
        ExposedDropdownMenu(
            expanded = desplegado,
            onDismissRequest = { desplegado = false }
        ) {
            REGIONES.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = { alCambiar(opcion); desplegado = false }
                )
            }
        }
    }
}

@Composable
fun SelectorPerfil(perfil: PerfilUsuario, alCambiar: (PerfilUsuario) -> Unit) {
    Text(
        text = "¿Cómo usarás VozVisible?",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(4.dp))
    Column(modifier = Modifier.selectableGroup()) {
        PerfilUsuario.entries.forEach { opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .selectable(
                        selected = perfil == opcion,
                        onClick = { alCambiar(opcion) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = perfil == opcion, onClick = null)
                Text(
                    text = opcion.etiqueta,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SelectorApoyos(elegidos: List<String>, alAlternar: (String) -> Unit) {
    Text(
        text = "Apoyos que quieres activar",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(10.dp))
    // los dejo en dos columnas, se ven mejor que en lista
    APOYOS.chunked(2).forEach { fila ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fila.forEach { apoyo ->
                CeldaApoyo(
                    texto = apoyo,
                    marcado = apoyo in elegidos,
                    alPulsar = { alAlternar(apoyo) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (fila.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CeldaApoyo(
    texto: String,
    marcado: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borde = if (marcado) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .toggleable(value = marcado, onValueChange = { alPulsar() }, role = Role.Checkbox),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (marcado) 2.dp else 1.dp, borde),
        colors = CardDefaults.cardColors(
            containerColor = if (marcado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = marcado, onCheckedChange = null)
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
