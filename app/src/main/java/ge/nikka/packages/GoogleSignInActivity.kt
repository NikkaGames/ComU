package ge.nikka.packages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleSignInActivity : FragmentActivity() {
    private val oauthc = "698075347856-ths3q0k5f73qfge26na2oh1isjdiel17.apps.googleusercontent.com"  //Change your oauth and also don't forget to add your own custom sign key, otherwise Google auth will fail!
    private var mGoogleSignInClient: GoogleSignInClient? = null
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (listener == null) {
            LoginActivity.logger?.append("Google Authentication failed. Listener is null")
            finish()
            return
        }
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    listener!!.onSuccess(account.serverAuthCode)
                    LoginActivity.logger?.append("Google Authentication success")
                    finish()
                    return
                } else {
                    listener!!.onError("Account is null")
                    LoginActivity.logger?.append("Account is null")
                }
            } catch (e: ApiException) {
                LoginActivity.logger?.append("signInResult:failed code=" + e.statusCode)
                listener!!.onError("Google Authentication failed. Please, try again!")
                LoginActivity.logger?.append("Google Authentication failed. Please, try again!")
                //signIn();
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(oauthc)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        instance = this
        //signOut();
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onStart() {
        super.onStart()
        signIn()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private var instance: GoogleSignInActivity? = null
        private var listener: GoogleSignInListener? = null
        @JvmStatic
        fun signIn(activity: Activity, googleSignInListener: GoogleSignInListener?) {
            val intent = Intent(activity, GoogleSignInActivity::class.java)
            listener = googleSignInListener
            activity.startActivity(intent)
        }

        fun signOut() {
            if (instance != null) {
                instance!!.mGoogleSignInClient!!.signOut()
                    .addOnCompleteListener { task: Task<Void?>? ->
                        if (listener != null) {
                            //listener.onSignOut();
                        }
                    }
            }
        }
    }
}