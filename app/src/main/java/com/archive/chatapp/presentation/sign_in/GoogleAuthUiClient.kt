package com.archive.chatapp.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.archive.chatapp.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            if (user != null) {
                saveUserToFirestore(
                    user = UserData(
                        userId = user.uid,
                        username = user.displayName,
                        email = user.email,
                        profilePictureUrl = user.photoUrl.toString(),
                        fcmToken = ""
                    )
                )
            }
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        email = email,
                        profilePictureUrl = photoUrl?.toString(),
                        fcmToken = ""
                    )

                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            email = email,
            profilePictureUrl = photoUrl?.toString(),
            fcmToken = ""
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    // Function to save a registered user in Firestore

}
fun saveUserToFirestore(user: UserData) {

    Log.i("Firestore", "Saving user")
    val db = Firebase.firestore
    val usersCollection = db.collection("users")
    usersCollection.document(user.userId).set(user)
        .addOnSuccessListener { Log.i("Firestore", "User Created Success") }
        .addOnFailureListener { Log.e("Firestore", "User Creation error") }

}
fun updateUserFCMToken(token: String,uid:String){
    val db = Firebase.firestore
    val usersCollection = db.collection("users")
    usersCollection.document(uid).update(mapOf("fcmToken" to token))
        .addOnSuccessListener { Log.i("FCM", "TOKEN UPDATED SUCCESS") }
        .addOnFailureListener { Log.e("FCM", "TOKEN UPDATE error") }
}