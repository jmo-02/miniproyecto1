package com.example.miniproyecto1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproyecto1.model.auth.UserRequest
import com.example.miniproyecto1.model.auth.UserResponse
import com.example.miniproyecto1.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LoginViewModel - ViewModel para la lógica de autenticación
 * 
 * Este ViewModel maneja toda la lógica de negocio relacionada con el login y registro:
 * - Validaciones de campos en tiempo real
 * - Comunicación con el AuthRepository para operaciones de Firebase
 * - Gestión de estados de la UI (loading, errores, éxito)
 * - Mantiene la separación entre la lógica de negocio y la UI
 * 
 * Utiliza LiveData para comunicar cambios de estado a la vista (LoginFragment)
 * de forma reactiva y segura con respecto al ciclo de vida.
 * 
 * @HiltViewModel permite la inyección de dependencias automática del repositorio
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val AuthRepository: AuthRepository
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════
    // 📊 LIVEDATA OBSERVABLES - Estados que la UI puede observar
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * authResult - Resultado de operaciones de autenticación (login/registro)
     * Contiene el UserResponse con información de éxito/error y datos del usuario
     */
    private val _authResult = MutableLiveData<UserResponse>()
    val authResult: LiveData<UserResponse> = _authResult

    /**
     * progressState - Estado de carga para mostrar/ocultar ProgressBar
     * true = operación en curso, false = operación completada
     */
    private val _progressState = MutableLiveData<Boolean>()
    val progressState: LiveData<Boolean> = _progressState

    /**
     * isPasswordValid - Estado de validación de la contraseña
     * true = contraseña válida (6-10 caracteres), false = contraseña inválida
     */
    private val _isPasswordValid = MutableLiveData<Boolean>()
    val isPasswordValid: LiveData<Boolean> = _isPasswordValid

    /**
     * areFieldsComplete - Estado de completitud de los campos
     * true = ambos campos tienen contenido, false = al menos un campo vacío
     * Se usa para habilitar/deshabilitar botones de Login y Registro
     */
    private val _areFieldsComplete = MutableLiveData<Boolean>()
    val areFieldsComplete: LiveData<Boolean> = _areFieldsComplete

    // ═══════════════════════════════════════════════════════════════════════
    // 🔐 FUNCIONES DE AUTENTICACIÓN
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * registerUser - Registra un nuevo usuario en Firebase Authentication
     * 
     * Flujo de ejecución:
     * 1. Muestra la barra de progreso (UI feedback)
     * 2. Lanza una corrutina para operación asíncrona (no bloquea el hilo principal)
     * 3. Llama al repositorio que se comunica con Firebase
     * 4. Recibe la respuesta (éxito o error)
     * 5. Actualiza authResult para que la UI pueda reaccionar
     * 6. Oculta la barra de progreso
     * 
     * @param userRequest Objeto con email y password del usuario a registrar
     * 
     * Criterios implementados:
     * - Criterio 13: Registro exitoso
     * - Criterio 14: Manejo de errores en registro
     * - Criterio 15: Barra de progreso durante la operación
     */
    fun registerUser(userRequest: UserRequest) {
        // Muestra la barra de progreso (Criterio 15)
        _progressState.value = true
        viewModelScope.launch {
            // Criterio 13 y 14: Manejo del registro (éxito/error)
            val response = AuthRepository.registerUser(userRequest)
            _authResult.postValue(response)
            // Oculta la barra de progreso
            _progressState.postValue(false)
        }
    }

    /**
     * loginUser - Inicia sesión con un usuario existente
     * 
     * Flujo de ejecución:
     * 1. Muestra la barra de progreso (UI feedback)
     * 2. Lanza una corrutina para operación asíncrona
     * 3. Llama al repositorio que autentica con Firebase
     * 4. Recibe la respuesta (éxito: credenciales correctas, error: credenciales incorrectas)
     * 5. Actualiza authResult para que la UI redirija o muestre error
     * 6. Oculta la barra de progreso
     * 
     * @param email Email del usuario
     * @param pass Contraseña del usuario
     * 
     * Criterios implementados:
     * - Criterio 9: Mensaje "Login incorrecto" si las credenciales fallan
     * - Criterio 10: Login exitoso y redirección
     * - Criterio 15: Barra de progreso durante la operación
     */
    fun loginUser(email: String, pass: String) {
        // Muestra la barra de progreso (Criterio 15)
        _progressState.value = true
        viewModelScope.launch {
            // Criterio 9 y 10: Manejo del login (éxito/error)
            val response = AuthRepository.loginUser(email, pass)
            _authResult.postValue(response)
            // Oculta la barra de progreso
            _progressState.postValue(false)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ FUNCIONES DE VALIDACIÓN
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * validatePassword - Valida que la contraseña cumpla los requisitos
     * 
     * Requisitos de contraseña:
     * - Mínimo: 6 caracteres
     * - Máximo: 10 caracteres
     * 
     * Esta validación se ejecuta en tiempo real mientras el usuario escribe,
     * permitiendo feedback inmediato sobre la validez de la contraseña.
     * 
     * @param password Contraseña a validar
     * 
     * Criterio implementado:
     * - Criterio 5: Validación de tamaño de contraseña (6-10 caracteres)
     */
    fun validatePassword(password: String) {
        // Criterio 5: Tamaño mínimo de 6 números y máximo de 10 números
        val isValid = password.length in 6..10
        _isPasswordValid.value = isValid
    }

    /**
     * checkFieldsCompletion - Verifica si ambos campos tienen contenido
     * 
     * Esta función se llama cada vez que el usuario escribe en cualquiera de
     * los dos campos (email o contraseña) para determinar si los botones de
     * Login y Registro deben estar habilitados o deshabilitados.
     * 
     * Los botones solo se habilitan cuando AMBOS campos tienen al menos un carácter.
     * 
     * @param email Contenido del campo email
     * @param pass Contenido del campo contraseña
     */
    fun checkFieldsCompletion(email: String, pass: String) {
        val isComplete = email.isNotEmpty() && pass.isNotEmpty()
        _areFieldsComplete.value = isComplete
    }
}