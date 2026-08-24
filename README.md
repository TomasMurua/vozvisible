# VozVisible

Aplicacion movil de accesibilidad para personas con discapacidad sensorial auditiva.
Proyecto de la asignatura Desarrollo de Aplicaciones Moviles (DSY2204), Duoc UC.

## Alcance de esta entrega

Primer avance del proyecto: la capa de acceso de la aplicacion.

- **Login** con validacion de correo y contrasena.
- **Registro de usuario** con seleccion de region, perfil de uso y apoyos de accesibilidad.
- **Recuperar contrasena** a partir del correo registrado.
- **Inicio** con las prestaciones previstas para las siguientes entregas.

Los perfiles se guardan en un arreglo de cinco posiciones en memoria
(`UsuarioRepository`); todavia no hay persistencia ni backend.

## Tecnologias

- Android Studio con Kotlin
- Jetpack Compose y Material Design 3
- Navigation Compose para el flujo entre vistas

## Como ejecutar

1. Abrir la carpeta del proyecto en Android Studio.
2. Esperar la sincronizacion de Gradle.
3. Ejecutar sobre un emulador o dispositivo con Android 7.0 (API 24) o superior.

Cuenta de prueba: `demo@vozvisible.cl` / `Demo1234`
