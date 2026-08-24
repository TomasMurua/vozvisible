# VozVisible

App de accesibilidad para personas sordas o con hipoacusia. La idea es que puedan
comunicarse en el día a día pasando de voz a texto y de texto a voz con el teléfono.

Proyecto de Desarrollo de Aplicaciones Móviles (DSY2204), Duoc UC.

## Qué tiene hasta ahora

Por ahora está hecha la parte de acceso: login, registro y recuperar contraseña, más
una pantalla de inicio donde se ven las funciones que vienen más adelante.

Los usuarios quedan guardados en un arreglo de 5 en memoria (`UsuarioRepository`), así
que se pierden al cerrar la app. Todavía no hay base de datos ni backend.

## Para correrlo

Abrir la carpeta en Android Studio, esperar que termine de sincronizar Gradle y darle
run. Anda de Android 7 (API 24) para arriba.

Si no quieres registrarte, entra con `demo@vozvisible.cl` / `Demo1234`.

Está hecha con Kotlin y Jetpack Compose con Material 3, y la navegación entre pantallas
es con Navigation Compose.

Las capturas de las pantallas están en `docs/capturas`.
