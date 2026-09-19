# Entrenamientos

App Android para entrenadores de baloncesto de cantera. Sirve para llevar el día a
día de uno o varios equipos: horarios, asistencia a los entrenamientos, convocatorias,
quintetos por cuartos, resultados y estadísticas de la temporada.

## Funcionalidades

- **Equipos y jugadores**: categoría (con división 1ª/2ª), género, color y dorsales.
- **Calendario**: entrenamientos por horario semanal, festivos y partidos. El rango del
  calendario se calcula a partir de las fechas de los equipos y de los partidos.
- **Asistencia**: registro por entrenamiento y estadísticas semanales y de temporada.
- **Partidos**: convocatoria con las reglas de cada categoría, quintetos por cuartos
  con la normativa de participación (infantil y minibasket), resultado y tiros libres.
- **Notas de entrenamiento** con fotos.
- **Cuenta de entrenador** con correo y contraseña (Firebase Auth).

### Reglas por categoría

| Categoría | Cuartos | Jugadores por cuarto | Convocados |
|---|---|---|---|
| Cadete, Junior, Senior | 1 (quinteto inicial) | 5 | 5 a 12 |
| Infantil, Preinfantil | 4 | 5 | 8 a 12 (de 5 a 7 se guarda con confirmación) |
| Minibasket, PreMinibasket, Benjamin 5x5 | 6 | 5 | 8 a 15 |
| Benjamin 3x3, Pre-Benjamin 3x3 | 8 | 3 | 4 a 12 |

Están definidas en `logic/CategoryRules.kt` y `logic/LineupRules.kt`, con tests.

## Tecnología

Kotlin, Jetpack Compose (Material 3), Navigation Compose, Hilt, Firebase Authentication
y Cloud Firestore (con persistencia offline). `minSdk 26`, `compileSdk 35`.

## Estructura

```
app/src/main/java/com/example/entrenamientos/
├── data/    modelos (Team, Player, Match, Attendance, ...)
├── di/      módulos de Hilt
├── logic/   reglas de negocio puras, sin Android ni Firebase, con tests
└── ui/      ViewModels, navegación, pantallas y tema
```

La lógica que no depende de Android vive en `logic/` para poder probarla con tests
unitarios de JVM (`app/src/test`). Al añadir una regla nueva, colócala ahí y no
dentro de una pantalla.

## Puesta en marcha

1. Abre el proyecto en Android Studio.
2. Crea un proyecto en [Firebase](https://console.firebase.google.com), añade una app
   Android con el paquete `com.example.entrenamientos` y descarga `google-services.json`
   a `app/`.
3. En Firebase activa **Authentication → Correo y contraseña** y crea la base de datos
   **Firestore**.
4. Despliega las reglas de seguridad (ver más abajo).
5. Ejecuta la app.

## Reglas de seguridad de Firestore

Las reglas están en `firestore.rules`: cada usuario solo accede a `users/{su uid}` y a
las subcolecciones que usa la app; el resto está denegado. La app guarda nombres y fotos
de menores, así que **no dejes la base de datos en modo de prueba**.

Para desplegarlas con la [Firebase CLI](https://firebase.google.com/docs/cli):

```
firebase deploy --only firestore:rules --project <ID_DEL_PROYECTO>
```

También puedes pegar el contenido de `firestore.rules` en Firebase Console →
Firestore → Reglas.

## Modelo de datos en Firestore

```
users/{uid}                          perfil (name, lastName, club, email)
 ├── teams/{id}
 ├── players/{id}
 ├── schedules/{id}                  horarios semanales de entrenamiento
 ├── matches/{id}                    partido + convocatoria + resultado
 ├── attendances/{fecha}_{playerId}
 ├── training_notes/{fecha}_{equipo}_{tipo}   notas, fotos y quintetos
 └── holidays/{fecha}
```

## Tests

```
./gradlew testDebugUnitTest
```

Cubren la lógica de `logic/`: semanas de asistencia, calendario, reglas por categoría,
quintetos, validación de resultados y contraseñas.

## Notas

- Los equipos nuevos nacen con las fechas de la temporada 2026-27; se cambian en
  Ajustes y el calendario las sigue.
- `google-services.json` contiene claves de cliente de Firebase, que no son secretas.
  Lo que protege los datos son las reglas de Firestore. Conviene además restringir la
  clave de API a este paquete y a su huella SHA-1 en Google Cloud Console.
