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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.vozvisible.data.CATEGORIAS_FRASE
import cl.duoc.vozvisible.data.Frase
import cl.duoc.vozvisible.ui.rememberHablador
import cl.duoc.vozvisible.ui.viewmodel.FrasesViewModel

private val ANCHO_MAXIMO = 560.dp

// CRUD completo de frases rápidas contra Firestore
@Composable
fun FrasesScreen(vm: FrasesViewModel, alVolver: () -> Unit) {
    val frases by vm.frases.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val hablador = rememberHablador()
    val snackbar = remember { SnackbarHostState() }

    // id vacío = frase nueva; null = diálogo cerrado
    var editando by rememberSaveable { mutableStateOf<String?>(null) }
    var textoEditado by rememberSaveable { mutableStateOf("") }
    var categoriaEditada by rememberSaveable { mutableStateOf(CATEGORIAS_FRASE.first()) }
    var porEliminar by remember { mutableStateOf<Frase?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it.texto)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    textoEditado = ""
                    categoriaEditada = CATEGORIAS_FRASE.first()
                    editando = ""
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva frase") }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp),
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
                    text = "Frases rápidas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.height(8.dp))

            val lista = frases
            when {
                lista == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                lista.isEmpty() -> Text(
                    text = "Aún no tienes frases guardadas. Toca «Nueva frase» para crear la primera.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.widthIn(max = ANCHO_MAXIMO).padding(12.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(lista, key = { it.id }) { frase ->
                        TarjetaFrase(
                            frase = frase,
                            puedeHablar = hablador.disponible,
                            alHablar = { hablador.hablar(frase.texto) },
                            alEditar = {
                                textoEditado = frase.texto
                                categoriaEditada = frase.categoria
                                editando = frase.id
                            },
                            alEliminar = { porEliminar = frase }
                        )
                    }
                }
            }
        }
    }

    editando?.let { id ->
        DialogoFrase(
            esNueva = id.isEmpty(),
            texto = textoEditado,
            categoria = categoriaEditada,
            alCambiarTexto = { textoEditado = it },
            alCambiarCategoria = { categoriaEditada = it },
            validar = vm::validar,
            alGuardar = {
                vm.guardar(id.ifEmpty { null }, textoEditado, categoriaEditada) { editando = null }
            },
            alCancelar = { editando = null }
        )
    }

    porEliminar?.let { frase ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar esta frase?") },
            text = { Text("«${frase.texto}» se borrará de tu cuenta y no se puede recuperar.") },
            confirmButton = {
                TextButton(onClick = { vm.eliminar(frase.id); porEliminar = null }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaFrase(
    frase: Frase,
    puedeHablar: Boolean,
    alHablar: () -> Unit,
    alEditar: () -> Unit,
    alEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp)) {
            Text(
                text = frase.categoria,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = frase.texto,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (puedeHablar) {
                    IconButton(onClick = alHablar, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Decir en voz alta: ${frase.texto}"
                        )
                    }
                }
                IconButton(onClick = alEditar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar la frase: ${frase.texto}")
                }
                IconButton(onClick = alEliminar, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar la frase: ${frase.texto}")
                }
            }
        }
    }
}

@Composable
private fun DialogoFrase(
    esNueva: Boolean,
    texto: String,
    categoria: String,
    alCambiarTexto: (String) -> Unit,
    alCambiarCategoria: (String) -> Unit,
    validar: (String) -> String?,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
) {
    var intento by remember { mutableStateOf(false) }
    val error = if (intento) validar(texto) else null

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(if (esNueva) "Nueva frase" else "Editar frase") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = alCambiarTexto,
                    label = { Text("Frase") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Categoría", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                // varias filas de chips para que no se salgan de la pantalla
                CATEGORIAS_FRASE.chunked(3).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { opcion ->
                            FilterChip(
                                selected = opcion == categoria,
                                onClick = { alCambiarCategoria(opcion) },
                                label = { Text(opcion) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                intento = true
                if (validar(texto) == null) alGuardar()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
