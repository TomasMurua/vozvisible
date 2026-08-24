package cl.duoc.vozvisible.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.UsuarioRepository
import cl.duoc.vozvisible.ui.theme.VozVisibleTheme

private data class Prestacion(
    val titulo: String,
    val detalle: String,
    val icono: ImageVector
)

private val PRESTACIONES = listOf(
    Prestacion("Voz a texto", "Transcribe en vivo lo que dice la otra persona", Icons.Filled.Hearing),
    Prestacion("Texto a voz", "Escribe y el telefono habla por ti", Icons.Filled.RecordVoiceOver),
    Prestacion("Frases rapidas", "Respuestas guardadas para el dia a dia", Icons.Filled.Bolt),
    Prestacion("Subtitulos", "Sigue videos y llamadas con texto en pantalla", Icons.Filled.Subtitles),
    Prestacion("Alertas hapticas", "Avisos por vibracion en vez de sonido", Icons.Filled.Vibration),
    Prestacion("Teclado ampliado", "Escritura comoda con texto grande", Icons.Filled.Keyboard)
)

@Composable
fun InicioScreen(
    correo: String,
    alCerrarSesion: () -> Unit
) {
    val usuario = UsuarioRepository.registrados()
        .firstOrNull { it.correo.equals(correo, ignoreCase = true) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, ${usuario?.nombre ?: "de nuevo"}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = correo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    TextButton(onClick = alCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(0.dp))
                        Text(
                            text = " Salir",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (usuario != null && usuario.preferencias.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Apoyos activos: ${usuario.preferencias.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Que quieres hacer?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        items(PRESTACIONES) { prestacion ->
            TarjetaPrestacion(prestacion)
        }
    }
}

@Composable
private fun TarjetaPrestacion(prestacion: Prestacion) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.height(168.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = prestacion.icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = prestacion.titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = prestacion.detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioPreview() {
    VozVisibleTheme {
        InicioScreen(correo = "demo@vozvisible.cl", alCerrarSesion = {})
    }
}
