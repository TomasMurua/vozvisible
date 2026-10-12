# VozVisible

App de accesibilidad para personas sordas o con hipoacusia. La idea es que puedan
comunicarse en el día a día pasando de voz a texto y de texto a voz con el teléfono.

Proyecto de Desarrollo de Aplicaciones Móviles.

## Qué tiene

- Login, registro y recuperar contraseña con Firebase Authentication (correo y contraseña).
  Al entrar saluda con el nombre y muestra el menú principal.
- **Hablar.** Escribes o eliges una sugerencia y el teléfono lo dice en voz alta, a velocidad
  normal o lenta. El mensaje queda en letra grande para que la otra persona lo lea.
- **Escribir.** El reconocedor de voz del teléfono escucha a quien te habla y deja el texto en
  pantalla. Desde ahí se puede responder escribiendo.
- **Historial.** Cada uso de Hablar y Escribir se guarda en Firestore con la solicitud y la
  respuesta. Se puede filtrar por tipo, repetir en voz alta y borrar uno o todos.
- **Ayuda.** Un tutorial de cuatro pasos y preguntas frecuentes.
- Perfil del usuario (nombre, región, tipo de uso y apoyos) guardado en Cloud Firestore.
  Desde la pantalla de perfil se consulta, se modifica y se elimina la cuenta.
- Frases rápidas que se pueden crear, ver, editar y borrar (Firestore, en tiempo real) y
  reproducir en voz alta con el motor de texto a voz del teléfono.
- La sesión y el correo recordado se guardan con `SharedPreferences`, así que al abrir la app
  de nuevo se entra directo.
- Pruebas con JUnit, Mockito y Robolectric que corren en el computador, y pruebas de interfaz
  con Compose UI test y Espresso-Intents que corren en un emulador. Espresso-Intents responde
  en lugar del reconocedor de voz, así la prueba no necesita micrófono.

Está hecha en Kotlin con Jetpack Compose y Material 3, y la navegación la maneja Navigation
Compose. La lógica vive en `SesionViewModel`, `FrasesViewModel` y `ComunicacionViewModel`;
los dos últimos heredan de `BaseViewModel`. Los view models solo conocen interfaces
(`AuthRepository`, `PerfilRepository`, `FraseRepository`, `RegistroRepository`), así que
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
