package com.example.miniproyecto1.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager - Gestor de sesión persistente del usuario
 * 
 * Esta clase maneja el almacenamiento persistente del estado de sesión del usuario
 * utilizando SharedPreferences. Permite que la aplicación "recuerde" si el usuario
 * está logueado incluso después de cerrar y volver a abrir la app.
 * 
 * Funcionalidades:
 * - Guardar el estado de login del usuario
 * - Consultar si el usuario tiene una sesión activa
 * - Limpiar la sesión (logout)
 * 
 * SharedPreferences es un almacenamiento clave-valor que persiste datos localmente
 * en el dispositivo del usuario de forma segura y eficiente.
 * 
 * @param context Contexto de la aplicación para acceder a SharedPreferences
 */
class SessionManager(context: Context) {
    // ═══════════════════════════════════════════════════════════════════════
    // 💾 SHARED PREFERENCES - Almacenamiento local persistente
    // ═══════════════════════════════════════════════════════════════════════
    // Se crea un archivo de preferencias llamado "user_session" en modo privado
    // (solo esta app puede acceder a él)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        // Clave para almacenar el estado de login (true/false)
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * saveLoginState - Guarda el estado de login del usuario
     * 
     * Esta función se llama después de un login o registro exitoso para
     * marcar al usuario como "logueado". El estado se guarda en disco
     * y permanece incluso si se cierra la aplicación.
     * 
     * @param isLoggedIn true = usuario logueado, false = usuario no logueado
     * 
     * Uso típico:
     * - Después de login exitoso: saveLoginState(true)
     * - Después de logout: saveLoginState(false)
     */
    fun saveLoginState(isLoggedIn: Boolean) {
        // edit() abre el editor de SharedPreferences
        // putBoolean() guarda un valor booleano con la clave especificada
        // apply() guarda los cambios de forma asíncrona (no bloquea la UI)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    /**
     * isLoggedIn - Consulta si el usuario tiene una sesión activa
     * 
     * Esta función se llama al iniciar la app o al entrar en LoginFragment
     * para verificar si el usuario ya está logueado y debe ser redirigido
     * automáticamente al Home.
     * 
     * @return true si el usuario está logueado, false si no lo está
     * 
     * Nota: El segundo parámetro (false) es el valor por defecto que se
     * retorna si la clave no existe en SharedPreferences (primera vez que
     * se usa la app)
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * clearSession - Elimina toda la información de sesión
     * 
     * Esta función se llama cuando el usuario hace logout. Limpia todos
     * los datos almacenados en SharedPreferences, efectivamente cerrando
     * la sesión del usuario.
     * 
     * Después de llamar a esta función, isLoggedIn() retornará false.
     * 
     * Uso típico:
     * - Al presionar el botón de "Cerrar sesión"
     * - Al eliminar la cuenta del usuario
     */
    fun clearSession() {
        // clear() elimina todas las preferencias guardadas
        // apply() confirma los cambios de forma asíncrona
        prefs.edit().clear().apply()
    }
}