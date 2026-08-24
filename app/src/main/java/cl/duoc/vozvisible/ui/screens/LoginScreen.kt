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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.UsuarioRepository
import cl.duoc.vozvisible.ui.correoValido
import cl.duoc.vozvisible.ui.theme.VozVisibleTheme

@Composable
fun LoginScreen(
    alIngresar: (String) -> Unit,
    alRegistrarse: () -> Unit,
    alRecuperar: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var recordar by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }
    var errorAcceso by remember { mutableStateOf<String?>(null) }

    fun intentarIngreso() {
        errorCorreo = if (!correoValido(correo)) "Escribe un correo con formato válido" else null
        errorContrasena = if (contrasena.isBlank()) "Ingresa tu contraseña" else null
        if (errorCorreo != null || errorContrasena != null) return

        val usuario = UsuarioRepository.autenticar(correo, contrasena)
        if (usuario == null) {
            errorAcceso = "No encontramos una cuenta con ese correo y esa contraseña"
        } else {
            errorAcceso = null
            alIngresar(usuario.correo)
        }
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
                    alCambiar = { correo = it; errorCorreo = null; errorAcceso = null },
                    etiqueta = "Correo electrónico",
                    error = errorCorreo,
                    tecladoCorreo = true
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = contrasena,
                    alCambiar = { contrasena = it; errorContrasena = null; errorAcceso = null },
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

                if (errorAcceso != null) {
                    Spacer(Modifier.height(12.dp))
                    MensajeEstado(texto = errorAcceso!!, esError = true)
                }

                Spacer(Modifier.height(20.dp))
                BotonPrincipal(texto = "Ingresar", alPulsar = { intentarIngreso() })

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginPreview() {
    VozVisibleTheme {
        LoginScreen(alIngresar = {}, alRegistrarse = {}, alRecuperar = {})
    }
}
