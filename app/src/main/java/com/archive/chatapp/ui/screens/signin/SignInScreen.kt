package com.archive.chatapp.ui.screens.signin

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.archive.chatapp.R
import com.archive.chatapp.presentation.sign_in.SignInState
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.appevents.InternalAppEventsLogger
import com.facebook.internal.AnalyticsEvents
import com.facebook.login.widget.LoginButton
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@Composable
fun SignInScreen(
    state: SignInState,
    onSignInClick: () -> Unit,
    onFbClick:()->Unit
) {
    val viewModel:SignInViewModel = viewModel()
    val context = LocalContext.current
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            Modifier.fillMaxHeight(0.5f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Sign In", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,modifier = Modifier.padding(vertical = 8.dp).align(Alignment.Start))
            Text(text = "Please login to you account first.", style = MaterialTheme.typography.bodyMedium,color= MaterialTheme.colorScheme.onSurface,modifier  = Modifier.align(Alignment.Start).padding(bottom = 26.dp))

            CustomTextField(text = email, onValueChange = {viewModel.updateEmail(it)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                visualTransformation = VisualTransformation.None, placeholder = "Email")

            CustomTextField(text = password, onValueChange = {viewModel.updatePassword(it)},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(), placeholder = "Password")

            Button(onClick = {  }, shape = RoundedCornerShape(8.dp), modifier = Modifier) {
                Text(text = "Sign in")
            }
        }
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Or sign in using", style = MaterialTheme.typography.bodyMedium,color= MaterialTheme.colorScheme.onSurface,modifier  = Modifier.padding(bottom = 6.dp))
            Row(
                Modifier.padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                IconButton(onClick = onFbClick) {
                    Image(painter = painterResource(id = R.drawable.facebook_logo_2023), contentDescription = "facebook logo")
                }
                IconButton(onClick = onSignInClick) {
                    Image(painter = painterResource(id = R.drawable.google_logo), contentDescription = "google logo")
                }
            }
            Row(
                Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Don't have an account?", style = MaterialTheme.typography.bodyMedium,color= MaterialTheme.colorScheme.onSurface,modifier  = Modifier)
                Text(text = "Sign up",style = MaterialTheme.typography.bodyMedium,color= MaterialTheme.colorScheme.primary,modifier  = Modifier.clickable {  })
            }
        }

    }
}

@Composable
fun CustomTextField(text:String,placeholder:String,onValueChange:(String)->Unit,keyboardOptions: KeyboardOptions,visualTransformation:VisualTransformation){
    TextField(value = text, onValueChange = onValueChange, keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(unfocusedIndicatorColor = Color.Transparent,),
        visualTransformation = visualTransformation, placeholder = { Text(text = placeholder)},
        modifier = Modifier.padding(bottom = 16.dp))
}