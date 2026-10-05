package cl.duoc.vozvisible.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Person

private data class Prestacion(
    val titulo: String,
    val detalle: String,
    val icono: ImageVector,
    val disponible: Boolean = false
)

private val PRESTACIONES = listOf(
    Prestacion("Voz a texto", "Transcribe en vivo lo que dice la otra persona", Icons.Filled.Hearing),
    Prestacion("Texto a voz", "Escribe y el teléfono habla por ti", Icons.Filled.RecordVoiceOver),
    Prestacion("Frases rápidas", "Guarda, edita y reproduce tus frases del día a día", Icons.Filled.Bolt, disponible = true),
    Prestacion("Subtítulos", "Sigue videos y llamadas con texto en pantalla", Icons.Filled.Subtitles),
    Prestacion("Alertas hápticas", "Avisos por vibración en vez de sonido", Icons.Filled.Vibration),
    Prestacion("Teclado ampliado", "Escritura cómoda con texto grande", Icons.Filled.Keyboard)
)

@Composable
fun InicioScreen(
    sesion: SesionViewModel,
    alAbrirFrases: () -> Unit,
    alAbrirPerfil: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val usuario = sesion.estado.collectAsState().value.usuario

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
                            text = usuario?.correo.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    TextButton(onClick = alAbrirPerfil) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Perfil",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    TextButton(onClick = alCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Salir",
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
                    text = "¿Qué quieres hacer?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        items(PRESTACIONES) { prestacion ->
            TarjetaPrestacion(
                prestacion = prestacion,
                alPulsar = if (prestacion.disponible) alAbrirFrases else null
            )
        }
    }
}

@Composable
private fun TarjetaPrestacion(prestacion: Prestacion, alPulsar: (() -> Unit)?) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevacion = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val contenido: @Composable () -> Unit = {
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
            if (alPulsar == null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Próximamente",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    // las que ya funcionan se pueden tocar; las demás quedan como aviso de lo que viene
    if (alPulsar != null) {
        Card(
            onClick = alPulsar,
            colors = colores,
            elevation = elevacion,
            modifier = Modifier.heightIn(min = 208.dp)
        ) { contenido() }
    } else {
        Card(
            colors = colores,
            elevation = elevacion,
            modifier = Modifier.heightIn(min = 208.dp)
        ) { contenido() }
    }
}
