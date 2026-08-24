package cl.duoc.vozvisible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.PerfilUsuario
import cl.duoc.vozvisible.data.ResultadoRegistro
import cl.duoc.vozvisible.data.Usuario
import cl.duoc.vozvisible.data.UsuarioRepository
import cl.duoc.vozvisible.ui.contrasenaValida
import cl.duoc.vozvisible.ui.correoValido
import cl.duoc.vozvisible.ui.theme.VozVisibleTheme

private val REGIONES = listOf(
    "Región Metropolitana",
    "Valparaíso",
    "Biobío",
    "La Araucanía",
    "Antofagasta",
    "Los Lagos"
)

// en horizontal y en tablet el formulario no debe estirarse a todo el ancho
private val ANCHO_MAXIMO = 560.dp

private val APOYOS = listOf(
    "Subtítulos automáticos",
    "Alerta por vibración",
    "Alto contraste",
    "Texto ampliado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(alVolver: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf(REGIONES.first()) }
    var desplegado by remember { mutableStateOf(false) }
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
    var aviso by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    fun registrar() {
        aviso = null
        val detectados = buildMap {
            if (nombre.isBlank()) put("nombre", "Escribe tu nombre")
            if (!correoValido(correo)) put("correo", "Escribe un correo con formato válido")
            if (!contrasenaValida(contrasena)) {
                put("contraseña", "Usa al menos 8 caracteres, con letras y números")
            }
            if (confirmacion != contrasena) put("confirmacion", "Las contraseñas no coinciden")
        }
        errores = detectados
        if (detectados.isNotEmpty()) return

        if (!aceptaCondiciones) {
            aviso = "Debes aceptar las condiciones de uso para crear la cuenta" to true
            return
        }

        val resultado = UsuarioRepository.registrar(
            Usuario(
                nombre = nombre.trim(),
                correo = correo.trim(),
                contrasena = contrasena,
                region = region,
                perfil = perfil,
                preferencias = apoyosElegidos.toList()
            )
        )

        aviso = when (resultado) {
            is ResultadoRegistro.Exitoso ->
                "Cuenta creada en el registro ${resultado.posicion} de ${UsuarioRepository.CAPACIDAD}" to false

            ResultadoRegistro.CorreoDuplicado ->
                "Ese correo ya tiene una cuenta activa" to true

            ResultadoRegistro.SinCupos ->
                "El registro admite solo ${UsuarioRepository.CAPACIDAD} cuentas y ya están ocupadas" to true
        }

        if (resultado is ResultadoRegistro.Exitoso) {
            nombre = ""; correo = ""; contrasena = ""; confirmacion = ""
            apoyosElegidos.clear()
            aceptaCondiciones = false
        }
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

        Spacer(Modifier.height(4.dp))
        Text(
            text = "Quedan ${UsuarioRepository.cuposDisponibles()} de " +
                "${UsuarioRepository.CAPACIDAD} registros disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier
                .widthIn(max = ANCHO_MAXIMO)
                .fillMaxWidth()
                .padding(start = 12.dp)
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                CampoTexto(
                    valor = nombre,
                    alCambiar = { nombre = it; errores = errores - "nombre"; aviso = null },
                    etiqueta = "Nombre y apellido",
                    error = errores["nombre"]
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = correo,
                    alCambiar = { correo = it; errores = errores - "correo"; aviso = null },
                    etiqueta = "Correo electrónico",
                    error = errores["correo"],
                    tecladoCorreo = true
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = contrasena,
                    alCambiar = { contrasena = it; errores = errores - "contrasena"; aviso = null },
                    etiqueta = "Contraseña",
                    apoyo = "Mínimo 8 caracteres, con letras y números",
                    error = errores["contraseña"],
                    esContrasena = true
                )
                Spacer(Modifier.height(16.dp))

                CampoTexto(
                    valor = confirmacion,
                    alCambiar = { confirmacion = it; errores = errores - "confirmacion"; aviso = null },
                    etiqueta = "Repetir contraseña",
                    error = errores["confirmacion"],
                    esContrasena = true
                )
                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Región",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = desplegado,
                    onExpandedChange = { desplegado = !desplegado }
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecciona tu región") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = desplegado,
                        onDismissRequest = { desplegado = false }
                    ) {
                        REGIONES.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { region = opcion; desplegado = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "¿Cómo usarás VozVisible?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Column(modifier = Modifier.selectableGroup()) {
                    PerfilUsuario.entries.forEach { opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .selectable(
                                    selected = perfil == opcion,
                                    onClick = { perfilNombre = opcion.name },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = perfil == opcion, onClick = null)
                            Text(
                                text = opcion.etiqueta,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Apoyos que quieres activar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                // los dejo en dos columnas, se ven mejor que en lista
                APOYOS.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        fila.forEach { apoyo ->
                            CeldaApoyo(
                                texto = apoyo,
                                marcado = apoyo in apoyosElegidos,
                                alPulsar = {
                                    if (apoyo in apoyosElegidos) apoyosElegidos.remove(apoyo)
                                    else apoyosElegidos.add(apoyo)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .toggleable(
                            value = aceptaCondiciones,
                            onValueChange = { aceptaCondiciones = it; aviso = null },
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

                aviso?.let { (texto, esError) ->
                    Spacer(Modifier.height(16.dp))
                    MensajeEstado(texto = texto, esError = esError)
                }

                Spacer(Modifier.height(20.dp))
                BotonPrincipal(texto = "Crear mi cuenta", alPulsar = { registrar() })
            }
        }

        Spacer(Modifier.height(24.dp))
        TablaRegistrados()
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun CeldaApoyo(
    texto: String,
    marcado: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borde = if (marcado) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .toggleable(value = marcado, onValueChange = { alPulsar() }, role = Role.Checkbox),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (marcado) 2.dp else 1.dp, borde),
        colors = CardDefaults.cardColors(
            containerColor = if (marcado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = marcado, onCheckedChange = null)
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// tabla con los 5 cupos, para ir viendo como se llena
@Composable
private fun TablaRegistrados() {
    val registrados = UsuarioRepository.registrados()

    Text(
        text = "Usuarios almacenados",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    Card(
        modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                CeldaEncabezado("N", 0.12f)
                CeldaEncabezado("Correo", 0.52f)
                CeldaEncabezado("Clave", 0.36f)
            }

            repeat(UsuarioRepository.CAPACIDAD) { indice ->
                val usuario = registrados.getOrNull(indice)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CeldaTexto("${indice + 1}", 0.12f)
                    CeldaTexto(usuario?.correo ?: "Cupo disponible", 0.52f, usuario == null)
                    CeldaTexto(
                        usuario?.let { UsuarioRepository.contrasenaEnmascarada(it) } ?: "-",
                        0.36f,
                        usuario == null
                    )
                }
                if (indice < UsuarioRepository.CAPACIDAD - 1) SeparadorSuave()
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CeldaEncabezado(
    texto: String,
    peso: Float
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.weight(peso)
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CeldaTexto(
    texto: String,
    peso: Float,
    atenuado: Boolean = false
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (atenuado) 0.45f else 1f),
        modifier = Modifier.weight(peso)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistroPreview() {
    VozVisibleTheme { RegistroScreen(alVolver = {}) }
}
