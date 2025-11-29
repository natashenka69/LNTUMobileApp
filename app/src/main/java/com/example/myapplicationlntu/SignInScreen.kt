package com.example.myapplicationlntu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SignInScreen(onSignIn: () -> Unit, signInError: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Вхід через Google",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
            Text("Sign in with Google")
        }

        signInError?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Помилка: $it", style = MaterialTheme.typography.bodyMedium)
        }
    }
}