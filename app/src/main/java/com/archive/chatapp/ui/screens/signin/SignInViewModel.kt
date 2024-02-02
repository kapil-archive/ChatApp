package com.archive.chatapp.ui.screens.signin

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archive.chatapp.logout
import com.archive.chatapp.presentation.sign_in.SignInResult
import com.archive.chatapp.presentation.sign_in.SignInState
import com.archive.chatapp.presentation.sign_in.UserData
import com.archive.chatapp.presentation.sign_in.saveUserToFirestore
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.ProfileTracker
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel: ViewModel() {

    private val profileTracker =
        object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                if (currentProfile != null) {
                    this@SignInViewModel.updateProfile(currentProfile)
                } else {
                    this@SignInViewModel.resetProfile()
                }
            }
        }

    private val _profileViewState = MutableStateFlow(ProfileViewState(Profile.getCurrentProfile()))

    val profileViewState= _profileViewState.asStateFlow()

    override fun onCleared() {
        profileTracker.stopTracking()
        super.onCleared()
    }

    private fun updateProfile(profile: Profile) {
        _profileViewState.update { it.copy(profile = profile) }
        AccessToken.getCurrentAccessToken()?.let { handleFacebookSignIn(it) }
    }

    private fun resetProfile() {
        _profileViewState.update { it.copy(profile = null) }
    }


    fun handleFacebookSignIn(token: AccessToken) {
        Log.d("SIGN IN FB", "handleFacebookSignIn:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        val auth = Firebase.auth
        viewModelScope.launch {
            auth.currentUser?.let { firebaseUser ->
                // The user is already signed in with another provider, link Facebook credential
                firebaseUser.linkWithCredential(credential).addOnSuccessListener {
                    val user = it.user
                    if (user != null) {
                        updateProfile(Profile(id = user.uid, name = user.displayName, pictureUri = user.photoUrl, firstName = user.displayName, middleName = null, lastName = null, linkUri = null))
                        saveUserToFirestore(
                            user = UserData(
                                userId = user.uid,
                                username = user.displayName,
                                email = user.email,
                                profilePictureUrl = user.photoUrl.toString(),
                                fcmToken = ""
                            )
                        )
                        onSignInResult(
                            result = SignInResult(
                                data = UserData(
                                    userId = user.uid,
                                    username = user.displayName,
                                    email = user.email,
                                    profilePictureUrl = user.photoUrl.toString(),
                                    fcmToken = ""
                                ),
                                errorMessage = null
                            )
                        )
                        Log.d("SIGN IN FB", "Facebook linked successfully: $user")
                    }
                }.addOnFailureListener {
                    Log.e("SIGN IN FB", "Facebook link failed: ${it.message}")

                    onSignInResult(
                        result = SignInResult(null,
                            errorMessage = it.message)
                        )

                    logout()
                }
            } ?: run {
                // The user is not signed in, sign in with Facebook credential
                auth.signInWithCredential(credential).addOnSuccessListener {
                    val user = it.user
                    if (user != null) {
                        updateProfile(Profile(id = user.uid, name = user.displayName, pictureUri = user.photoUrl, firstName = user.displayName, middleName = null, lastName = null, linkUri = null))
                        saveUserToFirestore(
                            user = UserData(
                                userId = user.uid,
                                username = user.displayName,
                                email = user.email,
                                profilePictureUrl = user.photoUrl.toString(),
                                fcmToken = ""
                            )
                        )
                        Log.d("SIGN IN FB", "Facebook sign-in successful: $user")
                    }
                }.addOnFailureListener {
                    Log.e("SIGN IN FB", "Facebook sign-in failed: ${it.message}")
                }
            }
        }
    }




    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()
    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun updateEmail(eml:String){
        _email.update { eml }
    }
    fun updatePassword(pass:String){
        _password.update { pass }
    }
    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
        Log.i("SIGN_VM","$result state = ${state.value}")
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}

@Immutable
data class ProfileViewState(
    val profile: Profile? = null
)