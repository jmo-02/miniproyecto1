package com.example.miniproyecto1

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.miniproyecto1.model.auth.UserRequest
import com.example.miniproyecto1.model.auth.UserResponse
import com.example.miniproyecto1.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import com.example.miniproyecto1.viewmodel.LoginViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel
    private val repository = mock(AuthRepository::class.java)

    @Before
    fun setup() {
        viewModel = LoginViewModel(repository)
    }

    // ---------- REGISTER TESTS ----------

    @Test
    fun `registerUser sets authResult on success`() = runTest {
        val request = UserRequest("test@mail.com", "123456")
        val expectedResponse = UserResponse(true, "OK", "test@mail.com")

        `when`(repository.registerUser(request)).thenReturn(expectedResponse)

        viewModel.registerUser(request)
        advanceUntilIdle()

        assertEquals(expectedResponse, viewModel.authResult.value)
        verify(repository).registerUser(request)
    }

    @Test
    fun `registerUser sets authResult on error`() = runTest {
        val request = UserRequest("test@mail.com", "123456")
        val expected = UserResponse(false, "Error en el registro", null)

        `when`(repository.registerUser(request)).thenReturn(expected)

        viewModel.registerUser(request)
        advanceUntilIdle()

        assertEquals(expected, viewModel.authResult.value)
    }

    // ---------- LOGIN TESTS ----------

    @Test
    fun `loginUser sets authResult on login success`() = runTest {
        val expectedResponse = UserResponse(true, "Login exitoso", "test@mail.com")

        `when`(repository.loginUser("test@mail.com", "123456"))
            .thenReturn(expectedResponse)

        viewModel.loginUser("test@mail.com", "123456")
        advanceUntilIdle()

        assertEquals(expectedResponse, viewModel.authResult.value)
    }

    @Test
    fun `loginUser sets authResult on error`() = runTest {
        val expected = UserResponse(false, "Login incorrecto", null)

        `when`(repository.loginUser("x@mail.com", "wrong"))
            .thenReturn(expected)

        viewModel.loginUser("x@mail.com", "wrong")
        advanceUntilIdle()

        assertEquals(expected, viewModel.authResult.value)
    }

    // ---------- PASSWORD VALIDATION ----------

    @Test
    fun `validatePassword returns true for valid length`() {
        viewModel.validatePassword("123456")
        assertEquals(true, viewModel.isPasswordValid.value)
    }

    @Test
    fun `validatePassword returns false for invalid length`() {
        viewModel.validatePassword("123")
        assertEquals(false, viewModel.isPasswordValid.value)
    }

    // ---------- FIELD COMPLETION CHECK ----------

    @Test
    fun `checkFieldsCompletion returns true when fields filled`() {
        viewModel.checkFieldsCompletion("email", "pass")
        assertEquals(true, viewModel.areFieldsComplete.value)
    }

    @Test
    fun `checkFieldsCompletion returns false when fields empty`() {
        viewModel.checkFieldsCompletion("", "")
        assertEquals(false, viewModel.areFieldsComplete.value)
    }
}
