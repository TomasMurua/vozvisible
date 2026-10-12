package cl.duoc.vozvisible.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.ui.rememberEscuchador
import cl.duoc.vozvisible.ui.viewmodel.ComunicacionViewModel

private val ANCHO_MAXIMO = 600.dp

// Escribir escucha a la otra persona y pone en pantalla lo que dijo
@Composable
fun EscribirScreen(
    vm: ComunicacionViewModel,
    alVolver: () -> Unit,
    alResponder: () -> Unit
) {
    val mensaje by vm.mensaje.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    // lo transcrito en esta conversación, lo más nuevo primero. es ArrayList para que no se pierda al rotar
    var transcritos by rememberSaveable { mutableStateOf(ArrayList<String>()) }
    var sinReconocedor by rememberSaveable { mutableStateOf(false) }

    val escuchador = rememberEscuchador { opciones ->
        vm.registrarTranscripcion(opciones)?.let { texto ->
            transcritos = ArrayList(listOf(texto) + transcritos)
        }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BarraSuperior(titulo = "Escribir", alVolver = alVolver)
                Text(
                    text = "Toca el micrófono y acerca el teléfono a la persona que te habla. " +
                        "Lo que diga aparecerá escrito aquí.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (!escuchador.disponible || sinReconocedor) {
                    Spacer(Modifier.height(12.dp))
                    MensajeEstado(
                        texto = "Este teléfono no tiene reconocimiento de voz. Activa la app de " +
                            "Google o «Servicios de voz de Google» en los ajustes.",
                        esError = true
                    )
                }

                Spacer(Modifier.height(24.dp))
                BotonMicrofono(
                    habilitado = escuchador.disponible,
                    alPulsar = { sinReconocedor = !escuchador.escuchar() }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Escuchar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(24.dp))
                val ultimo = transcritos.firstOrNull()
                AnimatedContent(
                    targetState = ultimo,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { it / 3 }) togetherWith fadeOut(tween(150))
                    },
                    label = "ultimo"
                ) { texto ->
                    if (texto == null) {
                        Text(
                            text = "Todavía no hay nada escrito.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Te dijeron",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = texto,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                                )
                            }
                        }
                    }
                }

                // lo anterior de la misma conversación, más chico
                transcritos.drop(1).forEach { anterior ->
                    Spacer(Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = anterior,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = alResponder,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("  Responder escribiendo")
                }
                Spacer(Modifier.height(8.dp))
                Vinculo(texto = "Terminar y volver al menú", alPulsar = alVolver)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// botón redondo grande con un círculo detrás que crece y se achica, para que se note dónde tocar
@Composable
private fun BotonMicrofono(habilitado: Boolean, alPulsar: () -> Unit) {
    val respiro = rememberInfiniteTransition(label = "respiro")
    val escala by respiro.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escala"
    )
    val opacidad by respiro.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "opacidad"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
        if (habilitado) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .scale(escala)
                    .alpha(opacidad)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        FilledIconButton(
            onClick = alPulsar,
            enabled = habilitado,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.size(112.dp)
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Escuchar a la otra persona", modifier = Modifier.size(52.dp))
        }
    }
}
