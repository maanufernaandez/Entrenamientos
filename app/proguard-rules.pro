# Reglas de R8/ProGuard para la build de release.
#
# NOTA: ahora mismo la minificación está desactivada (isMinifyEnabled = false en
# app/build.gradle.kts), así que estas reglas todavía no se aplican. Se dejan
# preparadas para cuando se active; antes de publicar hay que probar la build de
# release en un dispositivo (login, crear equipo, asistencia, foto, estadísticas).

# Firestore convierte estos modelos a documentos y viceversa por reflexión
# (set(objeto) / toObject(Clase::class.java)). Si R8 renombrara sus campos o
# quitara sus constructores, los datos dejarían de guardarse o de leerse.
-keep class com.example.entrenamientos.data.** { *; }

# Información que Firestore y Kotlin necesitan en tiempo de ejecución.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# Trazas de error legibles en los informes de fallos.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile