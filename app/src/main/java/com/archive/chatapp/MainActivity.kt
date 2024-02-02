package com.archive.chatapp

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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
/// HI NEW CHANGE, JUST FOR CHECKS
class MainActivity : ComponentActivity() {
    companion object{
        val CURRENT_USER_ID = Firebase.auth.uid
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
        AppEventsLogger.activateApp(Application())
        val callbackManager = CallbackManager.Factory.create()
        setContent {
                // A surface container using the 'background' color from the theme
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var chatUser by remember {
                        mutableStateOf<ChatUser?>(null)
                    }
                    val viewModel: SignInViewModel = viewModel()
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = if (googleAuthUiClient.getSignedInUser() != null) "profile" else "sign_in") {
                        composable("sign_in") {
//                            val viewModel: SignInViewModel = viewModel()
                            val state by viewModel.state.collectAsState()
                            Log.d("NAVCONTROLLER",navController.currentDestination?.route?:"null")
                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult =
                                                googleAuthUiClient.signInWithIntent(
                                                    intent = result.data ?: return@launch
                                                )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )
                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }
                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                },
                                onFbClick = login
                            )
                        }
                        composable("profile") {
                            googleAuthUiClient.getSignedInUser()?.let { it1 ->
                                ChatScreen(
                                    user = it1,
                                    onSignOut = { lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        logout()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.popBackStack()
                                    } },
                                    onChatClick = {it->
                                        chatUser = it
                                        navController.navigate("dm")
                                    })
                            }
                        }
                        composable("dm"){
                            chatUser?.let { it1 ->
                                DMScreen(chatUser = it1){
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val login = {
        LoginManager.getInstance().logIn(this, CallbackManager.Factory.create(), listOf("email","public_profile"))
    }


}
val logout = {
    LoginManager.getInstance().logOut()
    Log.i("FB","Logged out")
}
