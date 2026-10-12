# VozVisible

App de accesibilidad para personas sordas o con hipoacusia. La idea es que puedan
comunicarse en el día a día pasando de voz a texto y de texto a voz con el teléfono.

Proyecto de Desarrollo de Aplicaciones Móviles.

## Qué tiene

- Login, registro y recuperar contraseña con Firebase Authentication (correo y contraseña).
  Al entrar saluda con el nombre y muestra el menú principal.
- **Hablar** (texto a voz): escribes o eliges una sugerencia, el teléfono lo dice en voz alta
  (velocidad normal o lenta) y lo muestra en letra grande para la otra persona.
- **Escribir** (voz a texto): el reconocedor de voz del teléfono escucha a quien te habla y
  lo deja escrito en pantalla; desde ahí se puede responder escribiendo.
- **Historial**: cada uso de Hablar y Escribir queda guardado en Firestore (solicitud y
  respuesta). Se puede filtrar por tipo, repetir en voz alta y borrar uno o todos.
- **Ayuda**: tutorial de cuatro pasos y preguntas frecuentes.
- Perfil del usuario (nombre, región, tipo de uso y apoyos) guardado en Cloud Firestore.
  Se puede consultar, modificar y eliminar la cuenta desde la pantalla de perfil.
- Frases rápidas: crear, ver, editar y borrar frases propias (Firestore, en tiempo real) y
  reproducirlas en voz alta con el motor de texto a voz del teléfono.
- La sesión queda guardada con `SharedPreferences`, junto con el correo recordado, así que
  al abrir la app de nuevo se entra directo.
- Pruebas: JUnit, Mockito y Robolectric corren en el computador. Las pruebas de interfaz, con
  Compose UI test y Espresso-Intents (que reemplaza al reconocedor de voz), corren en un emulador.

Está hecha en Kotlin con Jetpack Compose y Material 3. La navegación la maneja
Navigation Compose y la lógica vive en ViewModels (`SesionViewModel`, `FrasesViewModel`,
`ComunicacionViewModel`, los dos últimos heredan de `BaseViewModel`) que solo conocen
interfaces (`AuthRepository`, `PerfilRepository`, `FraseRepository`, `RegistroRepository`);
Firebase queda en una sola capa.

## Para correrlo

Abrir la carpeta en Android Studio, esperar que termine de sincronizar Gradle y darle
run. Anda de Android 7 (API 24) para arriba y necesita internet.

El archivo `app/google-services.json` (la configuración del proyecto de Firebase) no se sube
al repositorio. Para compilar hay que bajar el de tu propio proyecto de Firebase y dejarlo en
`app/`, con el paquete `cl.duoc.vozvisible`, Authentication con correo/contraseña activado y
Firestore creado con estas reglas:

```
match /usuarios/{uid} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
  match /frases/{frase} {
    allow read, write: if request.auth != null && request.auth.uid == uid;
  }
  match /historial/{registro} {
    allow read, write: if request.auth != null && request.auth.uid == uid;
  }
}
```

Cuenta de prueba: `demo@vozvisible.cl` / `Demo1234`.

## Pruebas

```
./gradlew testDebugUnitTest            # JUnit, Mockito y Robolectric
./gradlew connectedDebugAndroidTest    # pruebas de interfaz, necesita un emulador o celular
```

## APK firmado

La firma se lee de `keystore.properties` (no está en el repositorio):

```
storeFile=/ruta/a/vozvisible-release.jks
storePassword=...
keyAlias=vozvisible
keyPassword=...
```

```
./gradlew assembleRelease
```

El APK queda en `app/build/outputs/apk/release/`. La versión se define en `app/build.gradle.kts`
(`versionCode` y `versionName`).

Las capturas de las pantallas están en `docs/capturas`.
