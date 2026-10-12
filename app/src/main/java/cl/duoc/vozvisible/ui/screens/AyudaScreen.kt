package cl.duoc.vozvisible.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

private val ANCHO_MAXIMO = 600.dp

data class PreguntaAyuda(val pregunta: String, val respuesta: String)

val PASOS_TUTORIAL = listOf(
    "Ingresa con tu correo y contraseña. Si es la primera vez, crea tu cuenta.",
    "En el menú elige Hablar si quieres decir algo, o Escribir si alguien te está hablando.",
    "Sigue lo que indica cada pantalla. Puedes mostrar el teléfono a la otra persona.",
    "Todo queda guardado en tu Historial. Al terminar vuelves al menú."
)

val PREGUNTAS_AYUDA = listOf(
    PreguntaAyuda(
        "¿Qué hace Hablar?",
        "Escribes tu mensaje (o eliges una sugerencia) y el teléfono lo dice en voz alta. " +
            "Además lo muestra en letra grande para que la otra persona lo pueda leer."
    ),
    PreguntaAyuda(
        "¿Qué hace Escribir?",
        "Tocas el micrófono y acercas el teléfono a quien te habla. Lo que diga aparece escrito " +
            "en pantalla. Después puedes responder escribiendo."
    ),
    PreguntaAyuda(
        "No se escucha la voz",
        "Revisa el volumen multimedia. Si sigue sin sonar, instala la voz en español en " +
            "Ajustes > Accesibilidad > Salida de texto a voz."
    ),
    PreguntaAyuda(
        "Escribir no reconoce lo que dicen",
        "Necesitas conexión a internet y la app de Google activa. Pide que hablen claro y " +
            "cerca del teléfono, sin mucho ruido alrededor."
    ),
    PreguntaAyuda(
        "¿Dónde quedan mis mensajes?",
        "En tu cuenta, en la nube. Los ves en Historial desde cualquier teléfono donde ingreses " +
            "y puedes borrarlos uno por uno o todos juntos."
    ),
    PreguntaAyuda(
        "¿Cómo cambio mis datos o borro mi cuenta?",
        "En Perfil puedes cambiar tu nombre, región y apoyos. Al final de esa pantalla está la " +
            "opción para eliminar la cuenta con todos sus datos."
    )
)

// área de ayuda: tutorial corto y preguntas frecuentes que se abren al tocarlas
@Composable
fun AyudaScreen(alVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = ANCHO_MAXIMO).fillMaxWidth()) {
            BarraSuperior(titulo = "Ayuda", alVolver = alVolver)

            Text("Cómo se usa", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            PASOS_TUTORIAL.forEachIndexed { indice, paso ->
                Paso(numero = indice + 1, texto = paso)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(12.dp))
            Text("Preguntas frecuentes", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            PREGUNTAS_AYUDA.forEach { pregunta ->
                TarjetaPregunta(pregunta)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            Vinculo(texto = "Volver al menú", alPulsar = alVolver)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Paso(numero: Int, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
        ) {
            Text(
                text = numero.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun TarjetaPregunta(pregunta: PreguntaAyuda) {
    var abierta by rememberSaveable(pregunta.pregunta) { mutableStateOf(false) }
    val giro by animateFloatAsState(if (abierta) 180f else 0f, label = "giro")

    Card(
        onClick = { abierta = !abierta },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                stateDescription = if (abierta) "Abierta" else "Cerrada"
            }
    ) {
        Column(modifier = Modifier.animateContentSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = pregunta.pregunta,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.rotate(giro))
            }
            if (abierta) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = pregunta.respuesta,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}
