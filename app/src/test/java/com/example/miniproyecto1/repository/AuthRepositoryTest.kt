package com.example.miniproyecto1.repository

import com.example.miniproyecto1.model.auth.UserRequest
import com.example.miniproyecto1.model.auth.UserResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repo: AuthRepository

    @Before
    fun setup() {
        firebaseAuth = Mockito.mock(FirebaseAuth::class.java)
        repo = AuthRepository(firebaseAuth)
    }

    @Test
    fun `registerUser returns failure when firebase throws`() = runBlocking {
        val userRequest = UserRequest(email = "a@b.com", password = "123456")

        Mockito.`when`(firebaseAuth.createUserWithEmailAndPassword(Mockito.eq(userRequest.email), Mockito.eq(userRequest.password)))
            .thenThrow(RuntimeException("boom"))

        val response = repo.registerUser(userRequest)
        assertFalse(response.isSuccessful)
        assertNull(response.email)
    }

    @Test
    fun `loginUser returns failure message on exception`() = runBlocking {
        val email = "x@y.com"
        val pass = "pwd"

        Mockito.`when`(firebaseAuth.signInWithEmailAndPassword(Mockito.eq(email), Mockito.eq(pass))).thenThrow(RuntimeException("Boom"))

        val response = repo.loginUser(email, pass)

        assertFalse(response.isSuccessful)
        assertEquals("Login incorrecto", response.message)
    }
}
