package com.example.myapplicationlntu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplicationlntu.data.FirestoreRepo
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    displayName: String,
    onSignOut: () -> Unit,
    firestoreRepo: FirestoreRepo
) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }

    val messages by firestoreRepo.messagesFlow.collectAsState()

    LaunchedEffect(Unit) { firestoreRepo.startListening() }
    DisposableEffect(Unit) { onDispose { firestoreRepo.stopListening() } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Spacer(Modifier.height(32.dp))

        Text("Hello, $displayName", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your message") }
        )
        Spacer(Modifier.height(8.dp))

        Row {
            Button(onClick = {
                scope.launch {
                    if (input.isNotBlank()) {
                        firestoreRepo.sendMessage(input) { _, _ -> }
                        input = ""
                    }
                }
            }) {
                Text("Send")
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch { firestoreRepo.deleteAll { _, _ -> } }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Delete all messages")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages) { msg ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(text = msg.text, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "by: $displayName", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                firestoreRepo.deleteMessage(msg.id) { _, _ -> }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }

                Divider()
            }
        }
    }
}