package com.archive.chatapp.ui.screens.signin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.archive.chatapp.MainActivity
import com.archive.chatapp.R
import com.archive.chatapp.presentation.sign_in.SignInState
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

@Composable
fun SignInScreen(
    state: SignInState,
    onSignInClick: () -> Unit,
    onFbClick:()->Unit,
    onOtpClick:(String)->Unit,
    onSubmitClick:(String)->Unit
) {
    val viewModel:SignInViewModel = viewModel()
    val context = LocalContext.current
    var emailSelected by remember {
        mutableStateOf(false)
    }
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val otp by viewModel.otp.collectAsState()
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
        var getOtpClicked by remember {
            mutableStateOf(false)
        }
        Column(
            Modifier.fillMaxHeight(0.7f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Sign In", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.Start))
            Text(text = "Please login to you account first.", style = MaterialTheme.typography.bodyMedium,color= MaterialTheme.colorScheme.onSurface,modifier  = Modifier
                .align(Alignment.Start)
                .padding(bottom = 26.dp))
            if (emailSelected){
                CustomTextField(text = email, onValueChange = {viewModel.updateEmail(it)},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation.Companion.None, placeholder = "Email")

                CustomTextField(text = password, onValueChange = {viewModel.updatePassword(it)},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(), placeholder = "Password")
            }else{
                if (!getOtpClicked)CustomTextField(text = phone, onValueChange = {viewModel.updatePhone(it)},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = "Phone") else CustomTextField(text = otp, onValueChange = {viewModel.updateOTP(it)},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = "OTP")

            }
            Button(onClick = {
                             if (getOtpClicked) onSubmitClick(otp) else onOtpClick("+91"+phone);getOtpClicked = true
            }, shape = RoundedCornerShape(8.dp), modifier = Modifier) {
                Text(text = if (getOtpClicked)"Submit" else "Get Otp")
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(150.dp))
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
                androidx.compose.material3.IconButton(onClick = { emailSelected = if (emailSelected) false else true }) {
                    androidx.compose.material3.Icon(imageVector = if (emailSelected) Icons.Default.Phone else Icons.Default.Email,
                        contentDescription = if (emailSelected) "Phone" else "Email")
                }
            }
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
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
fun TextFieldForm(text1:String, text2:String, onValueChange1: (String) -> Unit,
                  onValueChange2: (String) -> Unit, keyboardType1: KeyboardType, keyboardType2: KeyboardType,
                  visualTransformation1: VisualTransformation= VisualTransformation.None,
                  visualTransformation2: VisualTransformation = VisualTransformation.None,
                  placeholder1: String,placeholder2: String
){


}

@Composable
fun CustomTextField(text:String,placeholder:String,onValueChange:(String)->Unit, keyboardOptions: KeyboardOptions,
                    visualTransformation:VisualTransformation = VisualTransformation.None){
    TextField(value = text, onValueChange = onValueChange, keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(unfocusedIndicatorColor = Color.Transparent,),
        visualTransformation = visualTransformation, placeholder = { Text(text = placeholder)},
        modifier = Modifier.padding(bottom = 16.dp))
}