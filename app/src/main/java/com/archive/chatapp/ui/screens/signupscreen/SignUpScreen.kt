package com.archive.chatapp.ui.screens.signupscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@Composable
fun SignUpScreen(){
    Column {
        TextField(value = "", onValueChange = {}, label = { Text(text = "Email")})
        TextField(value = "", onValueChange = {})
        
    }
}