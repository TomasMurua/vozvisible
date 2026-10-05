package cl.duoc.vozvisible.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.ui.esCorreoValido
import cl.duoc.vozvisible.ui.validarCampo
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

@Composable
fun RecuperarScreen(sesion: SesionViewModel, alVolver: () -> Unit) {
    val estado by sesion.estado.collectAsState()
    var correo by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { sesion.limpiarMensaje() }

    fun enviar() {
        error = validarCampo(correo, "Escribe un correo con formato válido") { it.esCorreoValido() }
        if (error != null) return
        // Firebase envía el correo; los errores (sin red, etc.) vuelven como AuthException
        sesion.recuperar(correo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al inicio de sesión"
                )
            }
            Text(
                text = "Recuperar contraseña",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Indícanos el correo con el que creaste tu cuenta y te enviaremos " +
                        "un enlace para definir una nueva contraseña.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(20.dp))

                CampoTexto(
                    valor = correo,
                    alCambiar = { correo = it; error = null; sesion.limpiarMensaje() },
                    etiqueta = "Correo electrónico",
                    error = error,
                    tecladoCorreo = true
                )

                estado.mensaje?.let {
                    Spacer(Modifier.height(16.dp))
                    MensajeEstado(texto = it.texto, esError = it.esError)
                }

                Spacer(Modifier.height(20.dp))
                BotonPrincipal(
                    texto = if (estado.cargando) "Enviando…" else "Enviar instrucciones",
                    alPulsar = { enviar() },
                    habilitado = !estado.cargando
                )

                Spacer(Modifier.height(8.dp))
                Vinculo(
                    texto = "Volver al inicio de sesión",
                    alPulsar = alVolver,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
