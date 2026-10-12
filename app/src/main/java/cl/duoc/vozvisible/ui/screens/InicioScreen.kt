package cl.duoc.vozvisible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.navigation.Rutas
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel

private data class Opcion(
    val titulo: String,
    val detalle: String,
    val icono: ImageVector,
    val ruta: String,
    val destacada: Boolean = false
)

// Hablar y Escribir van primero y destacadas: son las dos funciones principales
private val OPCIONES = listOf(
    Opcion("Hablar", "Escribe y el teléfono lo dice en voz alta por ti", Icons.Filled.RecordVoiceOver, Rutas.HABLAR, destacada = true),
    Opcion("Escribir", "El teléfono escribe en pantalla lo que te dicen", Icons.Filled.Hearing, Rutas.ESCRIBIR, destacada = true),
    Opcion("Frases rápidas", "Guarda, edita y reproduce tus frases del día a día", Icons.Filled.Bolt, Rutas.FRASES),
    Opcion("Historial", "Lo que dijiste y lo que te dijeron, guardado en tu cuenta", Icons.Filled.History, Rutas.HISTORIAL),
    Opcion("Ayuda", "Cómo usar la app y preguntas frecuentes", Icons.AutoMirrored.Filled.HelpOutline, Rutas.AYUDA)
)

// menú principal: saludo con el nombre y las funciones de la app
@Composable
fun InicioScreen(
    sesion: SesionViewModel,
    alAbrir: (String) -> Unit,
    alCerrarSesion: () -> Unit
) {
    val usuario = sesion.estado.collectAsState().value.usuario
    // el saludo entra deslizándose la primera vez que se muestra el menú
    val aparecer = remember { MutableTransitionState(false).apply { targetState = true } }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BotonCabecera("Perfil", Icons.Filled.Person) { alAbrir(Rutas.PERFIL) }
                    BotonCabecera("Salir", Icons.AutoMirrored.Filled.Logout, alCerrarSesion)
                }
                AnimatedVisibility(
                    visibleState = aparecer,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 2 }
                ) {
                    Column {
                        Text(
                            text = "Hola, ${usuario?.nombre?.ifBlank { null } ?: "de nuevo"}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = usuario?.correo.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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

        itemsIndexed(OPCIONES) { indice, opcion ->
            // cada tarjeta entra un poco después que la anterior
            val visible = remember { MutableTransitionState(false).apply { targetState = true } }
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(350, delayMillis = 80 * indice)) +
                    slideInVertically(tween(350, delayMillis = 80 * indice)) { it / 4 }
            ) {
                TarjetaOpcion(opcion = opcion, alPulsar = { alAbrir(opcion.ruta) })
            }
        }
    }
}

@Composable
private fun BotonCabecera(texto: String, icono: ImageVector, alPulsar: () -> Unit) {
    TextButton(onClick = alPulsar, modifier = Modifier.heightIn(min = 48.dp)) {
        Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TarjetaOpcion(opcion: Opcion, alPulsar: () -> Unit) {
    val fondo = if (opcion.destacada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val texto = if (opcion.destacada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val circulo = if (opcion.destacada) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primaryContainer
    val icono = if (opcion.destacada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Card(
        onClick = alPulsar,
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.heightIn(min = 190.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .background(circulo, CircleShape)
            ) {
                Icon(imageVector = opcion.icono, contentDescription = null, tint = icono, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = opcion.titulo, style = MaterialTheme.typography.titleMedium, color = texto)
            Spacer(Modifier.height(6.dp))
            Text(
                text = opcion.detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = texto.copy(alpha = 0.8f)
            )
        }
    }
}
