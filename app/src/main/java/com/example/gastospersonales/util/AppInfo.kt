package com.example.gastospersonales.util

/**
 * Información de la app para mostrar en Configuración > Acerca de.
 * Se mantiene como constante simple (no BuildConfig.VERSION_NAME) para no
 * depender de habilitar buildFeatures.buildConfig en Gradle; para un
 * proyecto de este alcance, actualizar esta línea a mano al lanzar una
 * versión es suficiente.
 */
object AppInfo {
    const val VERSION = "1.0.0 (en desarrollo)"
}