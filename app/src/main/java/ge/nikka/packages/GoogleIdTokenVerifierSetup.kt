package ge.nikka.packages

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory

object GoogleIdTokenVerifierSetup {
    private val CLIENT_ID = String(
        charArrayOf(
            50.toChar(),
            56.toChar(),
            56.toChar(),
            57.toChar(),
            54.toChar(),
            50.toChar(),
            57.toChar(),
            51.toChar(),
            50.toChar(),
            52.toChar(),
            57.toChar(),
            50.toChar(),
            45.toChar(),
            98.toChar(),
            112.toChar(),
            99.toChar(),
            49.toChar(),
            104.toChar(),
            57.toChar(),
            117.toChar(),
            57.toChar(),
            54.toChar(),
            100.toChar(),
            48.toChar(),
            115.toChar(),
            102.toChar(),
            111.toChar(),
            104.toChar(),
            57.toChar(),
            108.toChar(),
            53.toChar(),
            51.toChar(),
            115.toChar(),
            117.toChar(),
            109.toChar(),
            52.toChar(),
            57.toChar(),
            55.toChar(),
            56.toChar(),
            55.toChar(),
            99.toChar(),
            106.toChar(),
            110.toChar(),
            57.toChar(),
            107.toChar(),
            46.toChar(),
            97.toChar(),
            112.toChar(),
            112.toChar(),
            115.toChar(),
            46.toChar(),
            103.toChar(),
            111.toChar(),
            111.toChar(),
            103.toChar(),
            108.toChar(),
            101.toChar(),
            117.toChar(),
            115.toChar(),
            101.toChar(),
            114.toChar(),
            99.toChar(),
            111.toChar(),
            110.toChar(),
            116.toChar(),
            46.toChar(),
            99.toChar(),
            111.toChar(),
            109.toChar()
        )
    )
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    fun createVerifier(): GoogleIdTokenVerifier {
        return GoogleIdTokenVerifier.Builder(NetHttpTransport(), JSON_FACTORY)
            .setAudience(listOf(CLIENT_ID)) // Sets your app’s client ID as the audience
            .build()
    }
}