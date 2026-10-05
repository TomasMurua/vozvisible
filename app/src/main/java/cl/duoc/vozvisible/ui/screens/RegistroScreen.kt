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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.PerfilUsuario
import cl.duoc.vozvisible.data.Usuario
import cl.duoc.vozvisible.ui.esContrasenaValida
import cl.duoc.vozvisible.ui.esCorreoValido
import cl.duoc.vozvisible.ui.exigirLargoMinimo
import cl.duoc.vozvisible.ui.validarCampo
import cl.duoc.vozvisible.ui.viewmodel.SesionViewModel

// en horizontal y en tablet el formulario no debe estirarse a todo el ancho
private val ANCHO_MAXIMO = 560.dp

@Composable
fun RegistroScreen(
    sesion: SesionViewModel,
    alRegistrar: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by sesion.estado.collectAsState()
    LaunchedEffect(Unit) { sesion.limpiarMensaje() }

    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf(REGIONES.first()) }
    var perfilNombre by rememberSaveable { mutableStateOf(PerfilUsuario.PERSONA_SORDA.name) }
    val perfil = PerfilUsuario.valueOf(perfilNombre)
    val apoyosElegidos = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf<String>() }
    var aceptaCondiciones by rememberSaveable { mutableStateOf(false) }

    var errores by remember { mutableStateOf(mapOf<String, String>()) }
    var avisoLocal by remember { mutableStateOf<String?>(null) }

    fun registrar() {
        avisoLocal = null
        sesion.limpiarMensaje()
        val detectados = buildMap {
            validarCampo(nombre, "Escribe tu nombre completo", exigirLargoMinimo(2))
                ?.let { put("nombre", it) }
            validarCampo(correo, "Escribe un correo con formato válido") { it.esCorreoValido() }
                ?.let { put("correo", it) }
            validarCampo(contrasena, "Usa al menos 8 caracteres, con letras y números") {
                it.esContrasenaValida()
            }?.let { put("contrasena", it) }
            validarCampo(confirmacion, "Las contraseñas no coinciden") { it == contrasena }
                ?.let { put("confirmacion", it) }
        }
        errores = detectados
        if (detectados.isNotEmpty()) return

        if (!aceptaCondiciones) {
            avisoLocal = "Debes aceptar las condiciones de uso para crear la cuenta"
            return
        }

        sesion.registrar(
            usuario = Usuario(
                nombre = nombre.trim(),
                correo = correo.trim(),
                region = region,
                perfil = perfil,
                preferencias = apoyosElegidos.toList()
            ),
            clave = contrasena,
            alExito = alRegistrar
        )
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al inicio de sesión"
                )
            }
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                CampoTexto(
                    valor = nombre,
                    alCambiar = { nombre = it; errores = errores - "nombre"; avisoLocal = null },
                    etiqueta = "Nombre y apellido",
                    error = errores["nombre"]
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = correo,
                    alCambiar = { correo = it; errores = errores - "correo"; avisoLocal = null },
                    etiqueta = "Correo electrónico",
                    error = errores["correo"],
                    tecladoCorreo = true
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = contrasena,
                    alCambiar = { contrasena = it; errores = errores - "contrasena"; avisoLocal = null },
                    etiqueta = "Contraseña",
                    apoyo = "Mínimo 8 caracteres, con letras y números",
                    error = errores["contrasena"],
                    esContrasena = true
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = confirmacion,
                    alCambiar = { confirmacion = it; errores = errores - "confirmacion"; avisoLocal = null },
                    etiqueta = "Repetir contraseña",
                    error = errores["confirmacion"],
                    esContrasena = true
                )
                Spacer(Modifier.height(20.dp))

                SelectorRegion(region = region, alCambiar = { region = it })
                Spacer(Modifier.height(24.dp))

                SelectorPerfil(perfil = perfil, alCambiar = { perfilNombre = it.name })
                Spacer(Modifier.height(20.dp))

                SelectorApoyos(
                    elegidos = apoyosElegidos,
                    alAlternar = { apoyo ->
                        if (apoyo in apoyosElegidos) apoyosElegidos.remove(apoyo)
                        else apoyosElegidos.add(apoyo)
                    }
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .toggleable(
                            value = aceptaCondiciones,
                            onValueChange = { aceptaCondiciones = it; avisoLocal = null },
                            role = Role.Checkbox
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = aceptaCondiciones, onCheckedChange = null)
                    Text(
                        text = "Acepto las condiciones de uso y el tratamiento de mis datos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // el aviso local es de validación; el del view model viene del servidor
                (avisoLocal?.let { it to true } ?: estado.mensaje?.let { it.texto to it.esError })
                    ?.let { (texto, esError) ->
                        Spacer(Modifier.height(16.dp))
                        MensajeEstado(texto = texto, esError = esError)
                    }

                Spacer(Modifier.height(20.dp))
                BotonPrincipal(
                    texto = if (estado.cargando) "Creando cuenta…" else "Crear mi cuenta",
                    alPulsar = { registrar() },
                    habilitado = !estado.cargando
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
