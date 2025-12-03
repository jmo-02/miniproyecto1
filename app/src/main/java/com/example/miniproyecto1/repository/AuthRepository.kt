package com.example.miniproyecto1.repository

import com.example.miniproyecto1.model.auth.UserRequest
import com.example.miniproyecto1.model.auth.UserResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * AuthRepository - Repositorio para operaciones de autenticación con Firebase
 * 
 * Este repositorio actúa como intermediario entre el ViewModel y Firebase Authentication.
 * Encapsula toda la lógica de comunicación con Firebase, manejo de errores y
 * transformación de respuestas en objetos UserResponse que la app puede entender.
 * 
 * Responsabilidades:
 * - Registro de nuevos usuarios en Firebase
 * - Login de usuarios existentes
 * - Manejo de excepciones específicas de Firebase
 * - Transformación de resultados de Firebase a modelos de la app
 * 
 * Todas las funciones son suspend ya que las operaciones de Firebase son asíncronas.
 * 
 * @param firebaseAuth Instancia de FirebaseAuth inyectada por Hilt
 */
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * registerUser - Registra un nuevo usuario en Firebase Authentication
     * 
     * Flujo de ejecución:
     * 1. Intenta crear un usuario con email y contraseña en Firebase
     * 2. Firebase valida el formato del email y fortaleza de la contraseña
     * 3. Si tiene éxito: Retorna UserResponse con isSuccessful=true
     * 4. Si falla: Captura la excepción y retorna UserResponse con el error
     * 
     * Manejo de errores específicos:
     * - FirebaseAuthUserCollisionException: El email ya está registrado
     * - Otros errores: Errores de red, formato inválido, etc.
     * 
     * @param userRequest Objeto con email y password del usuario
     * @return UserResponse con el resultado de la operación
     * 
     * Nota: Esta función es 'suspend' porque await() es una operación asíncrona
     */
    suspend fun registerUser(userRequest: UserRequest): UserResponse {
        return try {
            // ═══════════════════════════════════════════════════════════════
            // ✅ INTENTO DE REGISTRO EN FIREBASE
            // ═══════════════════════════════════════════════════════════════
            // createUserWithEmailAndPassword es un método de Firebase que:
            // - Valida el formato del email
            // - Verifica que la contraseña cumpla requisitos mínimos de Firebase
            // - Crea el usuario en Firebase Authentication
            // - Retorna un resultado con datos del usuario creado
            // 
            // await() convierte la tarea asíncrona de Firebase en una corrutina
            val result = firebaseAuth.createUserWithEmailAndPassword(
                userRequest.email,
                userRequest.password
            ).await()

            // ✅ REGISTRO EXITOSO
            // Se construye un UserResponse indicando éxito y el email del usuario
            UserResponse(
                isSuccessful = true,
                message = "Registro exitoso. Bienvenido.",
                email = result.user?.email
            )
        } catch (e: Exception) {
            // ═══════════════════════════════════════════════════════════════
            // ❌ ERROR EN EL REGISTRO
            // ═══════════════════════════════════════════════════════════════
            // Se capturan excepciones específicas de Firebase para dar
            // mensajes de error claros al usuario
            val message = when (e) {
                // Email ya existe en la base de datos de Firebase
                is FirebaseAuthUserCollisionException -> "Error en el registro: Este email ya está registrado."
                // Otros errores: red, formato inválido, contraseña débil, etc.
                else -> "Error en el registro: Ocurrió un error inesperado."
            }
            UserResponse(
                isSuccessful = false,
                message = message,
                email = null
            )
        }
    }

    /**
     * loginUser - Autentica un usuario existente en Firebase
     * 
     * Flujo de ejecución:
     * 1. Intenta autenticar al usuario con email y contraseña en Firebase
     * 2. Firebase verifica que las credenciales coincidan con un usuario registrado
     * 3. Si tiene éxito: Retorna UserResponse con isSuccessful=true
     * 4. Si falla: Retorna UserResponse con "Login incorrecto"
     * 
     * Posibles causas de fallo:
     * - Email no registrado
     * - Contraseña incorrecta
     * - Usuario deshabilitado
     * - Problemas de conexión
     * 
     * @param email Email del usuario
     * @param pass Contraseña del usuario
     * @return UserResponse con el resultado de la autenticación
     * 
     * Criterios implementados:
     * - Criterio 9: Mensaje "Login incorrecto" en caso de fallo
     * - Criterio 10: Login exitoso con datos del usuario
     */
    suspend fun loginUser(email: String, pass: String): UserResponse {
        return try {
            // ═══════════════════════════════════════════════════════════════
            // ✅ INTENTO DE LOGIN EN FIREBASE
            // ═══════════════════════════════════════════════════════════════
            // signInWithEmailAndPassword es un método de Firebase que:
            // - Busca el usuario por email en la base de datos
            // - Verifica que la contraseña coincida con el hash almacenado
            // - Genera un token de sesión si las credenciales son correctas
            // - Retorna un resultado con los datos del usuario autenticado
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()

            // ✅ LOGIN EXITOSO (Criterio 10)
            // Las credenciales son correctas, se retorna el email del usuario
            UserResponse(
                isSuccessful = true,
                message = "Login exitoso. Redirigiendo...",
                email = result.user?.email
            )
        } catch (e: Exception) {
            // ═══════════════════════════════════════════════════════════════
            // ❌ LOGIN INCORRECTO (Criterio 9)
            // ═══════════════════════════════════════════════════════════════
            // Cualquier error de autenticación se maneja con el mismo mensaje
            // por seguridad (no revelar si el email existe o si la contraseña es incorrecta)
            // 
            // Posibles causas:
            // - Email no registrado en Firebase
            // - Contraseña incorrecta
            // - Usuario deshabilitado
            // - Error de red
            UserResponse(
                isSuccessful = false,
                message = "Login incorrecto", // Mensaje exacto requerido por Criterio 9
                email = null
            )
        }
    }
}