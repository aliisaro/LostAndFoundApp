package com.example.lostandfoundapp.userInterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.lostandfoundapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // FirebaseAuth-alustus
        auth = FirebaseAuth.getInstance()

        // Hae näkymän komponentit
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Kirjautumispainikkeen tapahtuma
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Kutsu kirjautumismetodia
            loginUser(email, password)
        }
    }

    // Kirjautumismetodi
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Kirjautuminen onnistui
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Kirjautuminen onnistui.", Toast.LENGTH_SHORT).show()
                    // Siirrä käyttäjä pääsivulle
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Kirjautuminen epäonnistui
                    Toast.makeText(baseContext, "Kirjautuminen epäonnistui: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
