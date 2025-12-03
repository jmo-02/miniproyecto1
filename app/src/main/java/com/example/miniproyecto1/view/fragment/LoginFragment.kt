package com.example.miniproyecto1.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.miniproyecto1.R
import com.example.miniproyecto1.databinding.FragmentLoginBinding
import com.example.miniproyecto1.model.auth.UserRequest
import com.example.miniproyecto1.utils.SessionManager
import com.example.miniproyecto1.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * LoginFragment - Fragment principal para el proceso de autenticación
 * 
 * Este fragment maneja toda la interfaz de usuario para el login y registro de usuarios.
 * Utiliza Data Binding para conectar la UI con el ViewModel y observa cambios en tiempo real
 * para validaciones, estados de carga y resultados de autenticación.
 * 
 * Características principales:
 * - Validación en tiempo real de campos (email y contraseña)
 * - Login de usuarios existentes con Firebase Authentication
 * - Registro de nuevos usuarios
 * - Gestión de sesión persistente usando SharedPreferences
 * - Navegación automática cuando el usuario ya está logueado
 * 
 * @AndroidEntryPoint permite la inyección de dependencias con Hilt
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    // Binding para acceder a los elementos de la vista de forma segura
    private lateinit var binding: FragmentLoginBinding
    
    // ViewModel que contiene la lógica de negocio del login
    // Se obtiene mediante delegación 'by viewModels()' gracias a Hilt
    private val loginViewModel: LoginViewModel by viewModels()
    
    // Gestor de sesión para mantener el estado de login entre sesiones de la app
    private lateinit var sessionManager: SessionManager

    /**
     * Ciclo de vida: Creación de la vista
     * 
     * Aquí se infla el layout y se inicializan los componentes básicos:
     * - Se crea el binding con el layout XML
     * - Se asigna el lifecycleOwner para que LiveData pueda observar cambios automáticamente
     * - Se inicializa el SessionManager con el contexto de la aplicación
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    /**
     * Ciclo de vida: Vista creada
     * 
     * Una vez que la vista está completamente creada, se configuran:
     * - Los listeners de eventos de UI (clics, cambios de texto)
     * - Los observers del ViewModel para reaccionar a cambios de estado
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    /**
     * Configura todos los listeners de eventos de la interfaz de usuario
     * 
     * Gestiona:
     * 1. Validación en tiempo real de los campos email y contraseña
     * 2. Acción del botón de Login
     * 3. Acción del enlace de Registro
     */
    private fun setupListeners() {
        // ═══════════════════════════════════════════════════════════════
        // 🔹 VALIDACIÓN EN TIEMPO REAL - CAMPO EMAIL
        // ═══════════════════════════════════════════════════════════════
        // Cada vez que el usuario escribe en el campo de email, se verifica
        // si tanto el email como la contraseña están completos para habilitar botones
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            loginViewModel.checkFieldsCompletion(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔹 VALIDACIÓN EN TIEMPO REAL - CAMPO CONTRASEÑA
        // ═══════════════════════════════════════════════════════════════
        // Cada vez que el usuario escribe en el campo de contraseña:
        // 1. Se valida que la contraseña cumpla con los requisitos (6-10 caracteres)
        // 2. Se verifica si ambos campos están completos para habilitar los botones
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            val password = text.toString()
            loginViewModel.validatePassword(password)
            loginViewModel.checkFieldsCompletion(
                binding.etEmail.text.toString(),
                password
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔹 BOTÓN LOGIN - Iniciar sesión con usuario existente
        // ═══════════════════════════════════════════════════════════════
        // Al presionar el botón Login, se envían las credenciales al ViewModel
        // que se encargará de autenticar al usuario con Firebase Authentication
        binding.btnLogin.setOnClickListener {
            loginViewModel.loginUser(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔹 ENLACE REGISTRARSE - Crear nuevo usuario
        // ═══════════════════════════════════════════════════════════════
        // Al presionar el enlace de registro, se crea un UserRequest con las
        // credenciales ingresadas y se envía al ViewModel para registrar el usuario
        // en Firebase Authentication
        binding.tvRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            val userRequest = UserRequest(email, pass)
            loginViewModel.registerUser(userRequest)
        }
    }

    /**
     * Configura los observadores del ViewModel
     * 
     * Los observers (LiveData) permiten reaccionar automáticamente a cambios de estado:
     * - areFieldsComplete: Habilita/deshabilita botones según campos completos
     * - isPasswordValid: Muestra/oculta error de validación de contraseña
     * - progressState: Muestra/oculta indicador de carga durante operaciones async
     * - authResult: Procesa el resultado del login/registro (éxito o error)
     */
    private fun observeViewModel() {
        // ═══════════════════════════════════════════════════════════════
        // 🔹 OBSERVER: Campos completos (email y contraseña no vacíos)
        // ═══════════════════════════════════════════════════════════════
        // Habilita o deshabilita los botones de Login y Registrarse
        // dependiendo de si ambos campos tienen contenido
        loginViewModel.areFieldsComplete.observe(viewLifecycleOwner) { ready ->
            binding.btnLogin.isEnabled = ready
            binding.tvRegister.isEnabled = ready
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔹 OBSERVER: Validación de contraseña en tiempo real
        // ═══════════════════════════════════════════════════════════════
        // Muestra un mensaje de error debajo del campo de contraseña si no cumple
        // con los requisitos (mínimo 6, máximo 10 caracteres)
        loginViewModel.isPasswordValid.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                binding.tilPassword.error = "Mínimo 6 dígitos"
            } else {
                binding.tilPassword.error = null
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔹 OBSERVER: Estado de progreso (carga)
        // ═══════════════════════════════════════════════════════════════
        // Muestra u oculta la barra de progreso mientras se ejecutan operaciones
        // asíncronas como login o registro (comunicación con Firebase)
        loginViewModel.progressState.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔹 OBSERVER: Resultado de autenticación (Login/Registro)
        // ═══════════════════════════════════════════════════════════════
        // Procesa la respuesta del servidor después de intentar login o registro:
        // - Si es exitoso: Guarda el estado de sesión y navega al Home
        // - Si falla: Muestra un mensaje de error al usuario
        loginViewModel.authResult.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                // ✅ AUTENTICACIÓN EXITOSA
                // 1. Se guarda el estado de sesión como "logueado" en SharedPreferences
                //    para mantener la sesión activa aunque se cierre la app
                sessionManager.saveLoginState(true)
                
                // 2. Se navega automáticamente al fragment principal (Home)
                //    usando Navigation Component de Android
                findNavController().navigate(R.id.action_loginFragment_to_homeInventoryFragment)
            } else {
                // ❌ ERROR EN AUTENTICACIÓN
                // Se muestra un mensaje Toast con el error específico
                // (ej: "Login incorrecto", "Email ya registrado", etc.)
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Ciclo de vida: onResume
     * 
     * Esta función se ejecuta cada vez que el fragment vuelve a estar visible.
     * Se verifica si el usuario ya tiene una sesión activa:
     * - Si está logueado: Redirige automáticamente al Home (evita ver el login)
     * - Si no está logueado: Permanece en la pantalla de login
     * 
     * Esto garantiza que los usuarios logueados no vean la pantalla de login
     * innecesariamente al abrir la aplicación.
     */
    override fun onResume() {
        super.onResume()
        if (sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_loginFragment_to_homeInventoryFragment)
        }
    }
}
