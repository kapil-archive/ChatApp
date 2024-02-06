package com.archive.chatapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.archive.chatapp.MainActivity.Companion.CHAT_SCREEN
import com.archive.chatapp.MainActivity.Companion.DM_SCREEN
import com.archive.chatapp.MainActivity.Companion.SIGNIN_SCREEN
import com.archive.chatapp.MainActivity.Companion.SIGNUP_SCREEN
import com.archive.chatapp.ui.screens.signupscreen.SignUpScreen

@Composable
fun MainApp(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SIGNUP_SCREEN){
        composable(SIGNUP_SCREEN){
            SignUpScreen()
        }
        composable(SIGNIN_SCREEN){

        }
        composable(CHAT_SCREEN){

        }
        composable(DM_SCREEN){

        }
    }
}