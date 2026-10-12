package cl.duoc.vozvisible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.LARGO_MAXIMO_MENSAJE
import cl.duoc.vozvisible.ui.rememberHablador
import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel

private val ANCHO_MAXIMO = 600.dp

// lo que más se repite al hablar con alguien que no conoce lengua de señas
private val SUGERENCIAS = listOf(
    "Hola, soy sordo/a. ¿Me puede escribir?",
    "¿Puede hablar más lento, por favor?",
    "Gracias por su ayuda",
    "Necesito ayuda",
    "¿Cuánto cuesta?"
)

private enum class Velocidad(val etiqueta: String, val valor: Float) {
    NORMAL("Normal", 1.0f),
    LENTA("Lenta", 0.75f)
}

// Hablar: la persona escribe y el teléfono lo dice en voz alta y lo muestra en grande
@Composable
fun HablarScreen(
    vm: ComunicacionViewModel,
    alVolver: () -> Unit,
    textoInicial: String = ""
) {
    val hablador = rememberHablador()
    val mensaje by vm.mensaje.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var texto by rememberSaveable { mutableStateOf(textoInicial) }
    var intento by rememberSaveable { mutableStateOf(false) }
    var mostrado by rememberSaveable { mutableStateOf<String?>(null) }
    var velocidad by rememberSaveable { mutableStateOf(Velocidad.NORMAL) }
    val error = if (intento) vm.validar(texto) else null

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it.texto)
            vm.limpiarMensaje()
        }
    }

    fun decir() {
        intento = true
        if (vm.registrarHablado(texto, hablador.disponible)) {
            hablador.hablar(texto.trim(), velocidad.valor)
            mostrado = texto.trim()
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth()) {
                BarraSuperior(titulo = "Hablar", alVolver = alVolver)
                Text(
                    text = "Escribe lo que quieres decir. El teléfono lo dirá en voz alta y lo " +
                        "mostrará en grande para que la otra persona también lo lea.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (!hablador.disponible) {
                    Spacer(Modifier.height(12.dp))
                    MensajeEstado(
                        texto = "Este teléfono no tiene voz en español instalada. Igual puedes " +
                            "mostrar tu mensaje en pantalla grande.",
                        esError = true
                    )
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = texto,
                    onValueChange = { if (it.length <= LARGO_MAXIMO_MENSAJE) texto = it },
                    label = { Text("Tu mensaje") },
                    minLines = 3,
                    isError = error != null,
                    supportingText = {
                        Text(error ?: "${texto.length} de $LARGO_MAXIMO_MENSAJE letras")
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Sugerencias", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SUGERENCIAS) { sugerencia ->
                        SuggestionChip(
                            onClick = { texto = sugerencia; intento = false },
                            label = { Text(sugerencia) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Velocidad de la voz", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(12.dp))
                    Velocidad.entries.forEach { opcion ->
                        FilterChip(
                            selected = velocidad == opcion,
                            onClick = { velocidad = opcion },
                            label = { Text(opcion.etiqueta) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                BotonPrincipal(
                    texto = if (hablador.disponible) "Decir en voz alta" else "Mostrar en grande",
                    alPulsar = ::decir
                )

                AnimatedVisibility(
                    visible = mostrado != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PantallaGrande(
                        texto = mostrado.orEmpty(),
                        hablando = hablador.hablando,
                        puedeRepetir = hablador.disponible,
                        alRepetir = { hablador.hablar(mostrado.orEmpty(), velocidad.valor) },
                        alNuevo = {
                            hablador.callar()
                            texto = ""
                            intento = false
                            mostrado = null
                        },
                        alTerminar = alVolver
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// el mensaje en letra grande para mostrarle el teléfono a la otra persona
@Composable
private fun PantallaGrande(
    texto: String,
    hablando: Boolean,
    puedeRepetir: Boolean,
    alRepetir: () -> Unit,
    alNuevo: () -> Unit,
    alTerminar: () -> Unit
) {
    // el parlante late mientras el teléfono está hablando
    val latido = rememberInfiniteTransition(label = "latido")
    val escala by latido.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escala"
    )

    Column(modifier = Modifier.padding(top = 20.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(if (hablando) escala else 1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (hablando) "Hablando…" else "Muéstrale esto a la otra persona",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = texto,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (puedeRepetir) {
                OutlinedButton(onClick = alRepetir, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Repetir")
                }
            }
            OutlinedButton(onClick = alNuevo, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                Text("Nuevo mensaje")
            }
        }
        Spacer(Modifier.height(8.dp))
        Vinculo(texto = "Terminar y volver al menú", alPulsar = alTerminar)
    }
}
