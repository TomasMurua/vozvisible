package cl.duoc.vozvisible.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.Registro
import cl.duoc.vozvisible.data.TipoRegistro
import cl.duoc.vozvisible.data.comoFecha
import cl.duoc.vozvisible.ui.rememberHablador
import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel

private val ANCHO_MAXIMO = 600.dp

// lo que se dijo y se escuchó, guardado en Firestore. se puede filtrar, repetir y borrar
@Composable
fun HistorialScreen(vm: ComunicacionViewModel, alVolver: () -> Unit) {
    val historial by vm.historial.collectAsState()
    val resumen by vm.resumen.collectAsState()
    val filtro by vm.filtro.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val hablador = rememberHablador()
    val snackbar = remember { SnackbarHostState() }
    var confirmarBorrado by remember { mutableStateOf(false) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it.texto)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth()) {
                BarraSuperior(titulo = "Historial", alVolver = alVolver) {
                    if (resumen.values.sum() > 0) {
                        IconButton(onClick = { confirmarBorrado = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Borrar todo el historial")
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    TipoRegistro.entries.forEach { tipo ->
                        Contador(
                            titulo = tipo.etiqueta,
                            valor = resumen[tipo] ?: 0,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filtro == null,
                        onClick = { vm.filtrar(null) },
                        label = { Text("Todo") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                    TipoRegistro.entries.forEach { tipo ->
                        FilterChip(
                            selected = filtro == tipo,
                            onClick = { vm.filtrar(tipo) },
                            label = { Text(tipo.etiqueta) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            val lista = historial
            when {
                lista == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                lista.isEmpty() -> Text(
                    text = if (filtro == null) "Aún no hay nada. Lo que digas con Hablar y lo que " +
                        "escuches con Escribir quedará guardado aquí."
                    else "No hay registros de ${filtro?.etiqueta}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.widthIn(max = ANCHO_MAXIMO).padding(12.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(lista, key = { it.id }) { registro ->
                        TarjetaRegistro(
                            registro = registro,
                            puedeHablar = hablador.disponible,
                            alRepetir = { hablador.hablar(registro.textoPrincipal()) },
                            alEliminar = { vm.eliminar(registro.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Borrar todo el historial?") },
            text = { Text("Se eliminarán todos los mensajes guardados de tu cuenta. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { vm.eliminarTodo(); confirmarBorrado = false }) { Text("Borrar todo") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

// en Hablar lo importante es lo que escribió la persona; en Escribir, lo que se transcribió
private fun Registro.textoPrincipal(): String =
    if (tipo == TipoRegistro.HABLAR) solicitud else respuesta

@Composable
private fun Contador(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TarjetaRegistro(
    registro: Registro,
    puedeHablar: Boolean,
    alRepetir: () -> Unit,
    alEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esHablar = registro.tipo == TipoRegistro.HABLAR
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (esHablar) Icons.Filled.RecordVoiceOver else Icons.Filled.Hearing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${registro.tipo.etiqueta} · ${registro.fecha.comoFecha()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = registro.textoPrincipal(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (esHablar) registro.respuesta else "Transcrito de lo que te dijeron",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (puedeHablar) {
                    IconButton(onClick = alRepetir, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Decir en voz alta: ${registro.textoPrincipal()}"
                        )
                    }
                }
                IconButton(onClick = alEliminar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar del historial: ${registro.textoPrincipal()}")
                }
            }
        }
    }
}
