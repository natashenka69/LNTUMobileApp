package com.example.myapplicationlntu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationlntu.data.FirestoreRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(
                    account.idToken, null
                )

                auth.signInWithCredential(credential)
                    .addOnSuccessListener { /* успіх */ }
                    .addOnFailureListener { /* помилка */ }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            // test
        fun launchGoogleSignIn() {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(this, gso)
            googleLauncher.launch(client.signInIntent)
        }

        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                val firestoreRepo = remember { FirestoreRepo() }

                val user = auth.currentUser
                var displayName by remember { mutableStateOf(user?.displayName) }

                LaunchedEffect(auth.currentUser) {
                    displayName = auth.currentUser?.displayName
                }

                NavHost(
                    navController = nav,
                    startDestination = if (displayName == null) "signin" else "messages"
                ) {

                    composable("signin") {
                        SignInScreen(
                            onSignIn = {
                                launchGoogleSignIn()
                                displayName = auth.currentUser?.displayName
                                if (displayName != null) nav.navigate("messages")
                            },
                            signInError = null
                        )
                    }

                    composable("messages") {
                        MessageScreen(
                            displayName = displayName ?: "User",
                            onSignOut = {
                                auth.signOut()
                                displayName = null
                                nav.navigate("signin") {
                                    popUpTo("messages") { inclusive = true }
                                }
                            },
                            firestoreRepo = firestoreRepo
                        )
                    }
                }
            }
        }
    }
}