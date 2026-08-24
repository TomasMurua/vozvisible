package cl.duoc.vozvisible.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulProfundo,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulProfundo,
    secondary = AzulMedio,
    onSecondary = Color.White,
    tertiary = AmbarAcento,
    onTertiary = GrisTexto,
    tertiaryContainer = AmbarSuave,
    onTertiaryContainer = GrisTexto,
    background = GrisSuperficie,
    onBackground = GrisTexto,
    surface = Color.White,
    onSurface = GrisTexto,
    error = RojoError,
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulClaro,
    onPrimary = AzulProfundo,
    primaryContainer = AzulProfundo,
    onPrimaryContainer = AzulClaro,
    secondary = AzulClaro,
    onSecondary = AzulProfundo,
    tertiary = AmbarAcento,
    onTertiary = GrisTexto,
    background = Color(0xFF10141A),
    onBackground = Color(0xFFE6E9EF),
    surface = Color(0xFF181D25),
    onSurface = Color(0xFFE6E9EF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun VozVisibleTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}
