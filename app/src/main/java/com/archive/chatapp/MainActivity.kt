package com.archive.chatapp

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.archive.chatapp.presentation.sign_in.ChatUser
import com.google.android.gms.auth.api.identity.Identity
import com.archive.chatapp.ui.screens.chatscreen.ChatScreen
import com.archive.chatapp.presentation.sign_in.GoogleAuthUiClient
import com.archive.chatapp.presentation.sign_in.UserData
import com.archive.chatapp.presentation.sign_in.updateUserFCMToken
import com.archive.chatapp.ui.screens.chatscreen.getChatIdFromUserId
import com.archive.chatapp.ui.screens.dmscreen.DMScreen
import com.archive.chatapp.ui.screens.signin.SignInScreen
import com.archive.chatapp.ui.screens.signin.SignInViewModel
import com.archive.chatapp.ui.theme.ChatAppTheme
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/// HI NEW CHANGE, JUST FOR CHECKS
class MainActivity : ComponentActivity() {
    companion object{
        val CURRENT_USER_ID = Firebase.auth.uid
        val currentSystemDate = Instant.ofEpochSecond(Timestamp.now().seconds).atZone(ZoneId.systemDefault()).toLocalDate()
        const val SIGNUP_SCREEN = "Sign Up"
        const val SIGNIN_SCREEN = "Sign In"
        const val CHAT_SCREEN = "Chats"
        const val DM_SCREEN = "DM"
    }
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val uid = Firebase.auth.uid
                if (uid!=null&&token!=null){
                    updateUserFCMToken(token = token, uid = uid)
                }
            }
        }

//        FirebaseAuth.getInstance().createUserWithEmailAndPassword("kapilbamnawat2003@gmail.com", "password")
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    Log.i("EMAIL","SIGNUP SUCCESS")
//                    // Sign up success
//                } else {
//                    Log.e("EMAIL","ERROR ${task.exception?.message}")
//                    FirebaseAuth.getInstance().signInWithEmailAndPassword("kapilbamnawat2003@gmail.com", "password")
//                        .addOnCompleteListener(this) { task1 ->
//                            if (task1.isSuccessful) {
//                                Log.i("EMAIL1","SIGNUP SUCCESS")
//                                // Sign up success
//                            } else {
//                                Log.e("EMAIL1","ERROR ${task1.exception?.message}")
//                                // If sign in fails, display a message to the user.
//                            }
//                        }
//                    // If sign in fails, display a message to the user.
//                }
//            }

//        phoneAuth(this)


//        AppEventsLogger.activateApp(Application())
//        setContent {
//                // A surface container using the 'background' color from the theme
//            ChatAppTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    var chatUser by remember {
//                        mutableStateOf<ChatUser?>(null)
//                    }
//                    val viewModel: SignInViewModel = viewModel()
//                    val navController = rememberNavController()
//                    NavHost(navController = navController, startDestination = if (googleAuthUiClient.getSignedInUser() != null) "profile" else "sign_in") {
//                        composable("sign_in") {
////                            val viewModel: SignInViewModel = viewModel()
//                            val state by viewModel.state.collectAsState()
//                            Log.d("NAVCONTROLLER",navController.currentDestination?.route?:"null")
//                            LaunchedEffect(key1 = Unit) {
//                                if (googleAuthUiClient.getSignedInUser() != null) {
//                                    navController.navigate("profile")
//                                }
//                            }
//                            val launcher = rememberLauncherForActivityResult(
//                                contract = ActivityResultContracts.StartIntentSenderForResult(),
//                                onResult = { result ->
//                                    if (result.resultCode == RESULT_OK) {
//                                        lifecycleScope.launch {
//                                            val signInResult =
//                                                googleAuthUiClient.signInWithIntent(
//                                                    intent = result.data ?: return@launch
//                                                )
//                                            viewModel.onSignInResult(signInResult)
//                                        }
//                                    }
//                                }
//                            )
//                            LaunchedEffect(key1 = state.isSignInSuccessful) {
//                                if (state.isSignInSuccessful) {
//                                    Toast.makeText(
//                                        applicationContext,
//                                        "Sign in successful",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                    navController.navigate("profile")
//                                    viewModel.resetState()
//                                }
//                            }
//                            var verificationId by remember {
//                                mutableStateOf("")
//                            }
//                            SignInScreen(
//                                state = state,
//                                onSignInClick = {
//                                    lifecycleScope.launch {
//                                        val signInIntentSender = googleAuthUiClient.signIn()
//                                        launcher.launch(
//                                            IntentSenderRequest.Builder(
//                                                signInIntentSender ?: return@launch
//                                            ).build()
//                                        )
//                                    }
//                                },
//                                onFbClick = login,
//                                onOtpClick = { phoneAuth(this@MainActivity,it){
//                                    verificationId = it
//                                } },
//                                onSubmitClick = {
//                                    signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId, it),this@MainActivity)
//                                }
//                            )
//                        }
//                        composable("profile") {
//                            googleAuthUiClient.getSignedInUser()?.let { it1 ->
//                                ChatScreen(
//                                    user = it1,
//                                    onSignOut = { lifecycleScope.launch {
//                                        googleAuthUiClient.signOut()
//                                        logout()
//                                        Toast.makeText(
//                                            applicationContext,
//                                            "Signed out",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//
//                                        navController.popBackStack()
//                                    } },
//                                    onChatClick = {it->
//                                        chatUser = it
//                                        navController.navigate("dm")
//                                    })
//                            }
//                        }
//                        composable("dm"){
//                            chatUser?.let { it1 ->
//                                DMScreen(chatUser = it1){
//                                    navController.popBackStack()
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private val login = {
//        LoginManager.getInstance().logIn(this, CallbackManager.Factory.create(), listOf("email","public_profile"))
//    }
//
//
//}
//val logout = {
//    LoginManager.getInstance().logOut()
//    Log.i("FB","Logged out")
}}
fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,activity: Activity) {
    val auth = Firebase.auth
    auth.signInWithCredential(credential)
        .addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("TAG", "signInWithCredential:success")
                val user = task.result?.user
                // Update UI or perform other actions as needed
            } else {
                // Sign in failed, display a message and update the UI
                Log.w("TAG", "signInWithCredential:failure", task.exception)
                // Handle the failure, for example, show an error message
            }
        }
}


fun phoneAuth(activity: Activity,testPhoneNumber:String,verificationId: (String)->Unit){
    // Declare these variables at the top of your activity or class
    lateinit var auth: FirebaseAuth
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

// ...

// Initialize Firebase auth in your onCreate or wherever appropriate
    auth = Firebase.auth

    fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

// Set up callbacks for PhoneAuth
    callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification.
            // 2 - Auto-retrieval.
            Log.d("TAG", "onVerificationCompleted:$credential")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("TAG", "onVerificationFailed", e)
            // Handle verification failure
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("TAG", "onCodeSent:$verificationId")
            // Save verification ID and resending token for later use
            storedVerificationId = verificationId
            resendToken = token

            // In a real-world scenario, you would send the verification code to the user's device.
            // For testing purposes, we manually trigger signInWithPhoneAuthCredential using test values.
            val testOTP = "999999" // Replace with your test OTP
            verificationId(verificationId)
        }
    }

// ...

    // Function to initiate phone number verification


    // Function to handle sign-in with PhoneAuthCredential


// ...

// Now you can call startPhoneNumberVerification with your test phone number
     // Replace with your test phone number
    startPhoneNumberVerification(testPhoneNumber)
}