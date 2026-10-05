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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.PerfilUsuario
import cl.duoc.vozvisible.ui.exigirLargoMinimo
import cl.duoc.vozvisible.ui.validarCampo
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel

private val ANCHO_MAXIMO = 560.dp

// consultar (se ven los datos de Firestore), modificar (guardar cambios) y eliminar la cuenta
@Composable
fun PerfilScreen(
    sesion: SesionViewModel,
    alVolver: () -> Unit,
    alEliminarCuenta: () -> Unit
) {
    val estado by sesion.estado.collectAsState()
    val usuario = estado.usuario
    LaunchedEffect(Unit) { sesion.limpiarMensaje() }

    var nombre by remember(usuario) { mutableStateOf(usuario?.nombre.orEmpty()) }
    var region by remember(usuario) { mutableStateOf(usuario?.region ?: REGIONES.first()) }
    var perfil by remember(usuario) { mutableStateOf(usuario?.perfil ?: PerfilUsuario.PERSONA_SORDA) }
    val apoyos = remember(usuario) { mutableStateListOf<String>().apply { addAll(usuario?.preferencias.orEmpty()) } }
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var confirmando by remember { mutableStateOf(false) }

    fun guardar() {
        errorNombre = validarCampo(nombre, "Escribe tu nombre completo", exigirLargoMinimo(2))
        if (errorNombre != null) return
        sesion.actualizarPerfil(nombre, region, perfil, apoyos.toList()) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = alVolver, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al inicio")
            }
            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(20.dp))

        if (usuario == null) {
            Text(
                text = "No pudimos cargar tu perfil. Vuelve atrás e inténtalo de nuevo.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            return@Column
        }

        Card(
            modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Correo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = nombre,
                    alCambiar = { nombre = it; errorNombre = null; sesion.limpiarMensaje() },
                    etiqueta = "Nombre y apellido",
                    error = errorNombre
                )
                Spacer(Modifier.height(20.dp))

                SelectorRegion(region = region, alCambiar = { region = it })
                Spacer(Modifier.height(24.dp))

                SelectorPerfil(perfil = perfil, alCambiar = { perfil = it })
                Spacer(Modifier.height(20.dp))

                SelectorApoyos(
                    elegidos = apoyos,
                    alAlternar = { if (it in apoyos) apoyos.remove(it) else apoyos.add(it) }
                )

                estado.mensaje?.let {
                    Spacer(Modifier.height(8.dp))
                    MensajeEstado(texto = it.texto, esError = it.esError)
                }

                Spacer(Modifier.height(16.dp))
                BotonPrincipal(
                    texto = if (estado.cargando) "Guardando…" else "Guardar cambios",
                    alPulsar = { guardar() },
                    habilitado = !estado.cargando
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { sesion.limpiarMensaje(); confirmando = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text("Eliminar mi cuenta") }
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    if (confirmando) {
        DialogoEliminar(
            cargando = estado.cargando,
            error = estado.mensaje?.takeIf { it.esError }?.texto,
            alConfirmar = { clave -> sesion.eliminarCuenta(clave, alEliminarCuenta) },
            alCancelar = { confirmando = false; sesion.limpiarMensaje() }
        )
    }
}

@Composable
private fun DialogoEliminar(
    cargando: Boolean,
    error: String?,
    alConfirmar: (String) -> Unit,
    alCancelar: () -> Unit
) {
    var clave by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Eliminar cuenta") },
        text = {
            Column {
                Text(
                    "Se borrarán tu perfil y todas tus frases. Esta acción no se puede deshacer. " +
                        "Para confirmar, escribe tu contraseña."
                )
                Spacer(Modifier.height(12.dp))
                CampoTexto(
                    valor = clave,
                    alCambiar = { clave = it },
                    etiqueta = "Contraseña",
                    esContrasena = true,
                    error = error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { alConfirmar(clave) },
                enabled = clave.isNotBlank() && !cargando
            ) { Text(if (cargando) "Eliminando…" else "Eliminar para siempre") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
