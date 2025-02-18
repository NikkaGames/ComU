package ge.nikka.packages

interface GoogleSignInListener {
    fun onError(str: String?)
    fun onSuccess(str: String?)
}