package com.example.miniproyecto1.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.miniproyecto1.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigateTo = intent.getStringExtra("navigateTo")
        if (navigateTo == "login") {
            val navController = findNavController(R.id.navigationContainer)
            // Reemplaza R.id.loginFragment por el id real de tu destino de login en nav_graph
            navController.navigate(R.id.loginFragment)
        }
    }
}