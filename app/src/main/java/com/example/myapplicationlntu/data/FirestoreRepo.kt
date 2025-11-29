package com.example.myapplicationlntu.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Message(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val uid: String = ""
)

class FirestoreRepo {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userMessagesRef() =
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("messages")

    private val _messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    val messagesFlow = _messagesFlow.asStateFlow()

    private var registration: ListenerRegistration? = null

    fun startListening() {
        registration = userMessagesRef()
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _messagesFlow.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.map { doc ->
                    Message(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        uid = auth.currentUser?.uid ?: ""
                    )
                } ?: emptyList()

                _messagesFlow.value = list
            }
    }

    fun stopListening() {
        registration?.remove()
        registration = null
    }

    fun sendMessage(text: String, onComplete: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        val data = mapOf(
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "uid" to uid
        )

        userMessagesRef().add(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
    }

    fun deleteAll(onComplete: (Boolean, String?) -> Unit) {
        userMessagesRef().get()
            .addOnSuccessListener { docs ->
                val batch = db.batch()
                docs.forEach { batch.delete(it.reference) }

                batch.commit()
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
            }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
    }

    fun deleteMessage(id: String, onComplete: (Boolean, String?) -> Unit) {
        userMessagesRef().document(id)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
    }
}