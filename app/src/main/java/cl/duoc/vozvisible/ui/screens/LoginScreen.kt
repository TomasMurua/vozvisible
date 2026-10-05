package cl.duoc.vozvisible.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
fun LoginScreen(
    sesion: SesionViewModel,
    alIngresar: () -> Unit,
    alRegistrarse: () -> Unit,
    alRecuperar: () -> Unit
) {
    val estado by sesion.estado.collectAsState()
    val guardado = remember { sesion.correoRecordado }
    LaunchedEffect(Unit) { sesion.limpiarMensaje() }

    var correo by rememberSaveable { mutableStateOf(guardado.orEmpty()) }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var recordar by rememberSaveable { mutableStateOf(guardado != null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }

    fun intentarIngreso() {
        errorCorreo = validarCampo(correo, "Escribe un correo con formato válido") { it.esCorreoValido() }
        errorContrasena = validarCampo(contrasena, "Ingresa tu contraseña") { it.isNotBlank() }
        if (errorCorreo != null || errorContrasena != null) return

        sesion.ingresar(correo, contrasena, recordar, alIngresar)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        EncabezadoMarca(
            titulo = "VozVisible",
            bajada = "Conversaciones cotidianas, visibles para todas y todos"
        )

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(20.dp))

                CampoTexto(
                    valor = correo,
                    alCambiar = { correo = it; errorCorreo = null; sesion.limpiarMensaje() },
                    etiqueta = "Correo electrónico",
                    error = errorCorreo,
                    tecladoCorreo = true
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = contrasena,
                    alCambiar = { contrasena = it; errorContrasena = null; sesion.limpiarMensaje() },
                    etiqueta = "Contraseña",
                    error = errorContrasena,
                    esContrasena = true
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = recordar, onCheckedChange = { recordar = it })
                        Text(
                            text = "Recordar mi correo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Vinculo(texto = "Olvidé mi clave", alPulsar = alRecuperar)
                }

                estado.mensaje?.let {
                    Spacer(Modifier.height(12.dp))
                    MensajeEstado(texto = it.texto, esError = it.esError)
                }

                Spacer(Modifier.height(20.dp))
                BotonPrincipal(
                    texto = if (estado.cargando) "Ingresando…" else "Ingresar",
                    alPulsar = { intentarIngreso() },
                    habilitado = !estado.cargando
                )

                Spacer(Modifier.height(16.dp))
                SeparadorSuave()
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿Aún no tienes cuenta?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Vinculo(texto = "Crear una", alPulsar = alRegistrarse)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Cuenta de prueba: demo@vozvisible.cl / Demo1234",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
