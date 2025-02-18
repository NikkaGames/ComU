package ge.nikka.packages

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import ge.nikka.packages.ChatActivity.Companion.forceClose
import ge.nikka.packages.Handlers.LoadChats
import ge.nikka.packages.ui.main.PlaceholderFragment
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.IndexOutOfBoundsException
import java.security.KeyStore
import java.util.LinkedList
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class SocketService : Service() {
    private var socket: SSLSocket? = null
    private var input: BufferedReader? = null
    private var output: OutputStream? = null
    private var socketFactory: SSLSocketFactory? = null
    private var socketThread: Thread? = null
    private var sendThread: Thread? = null
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        connectToSocketServer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        isconnected = false
        closeSocket()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun Init() {
        try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(
                resources.openRawResource(R.raw.commu),
                charArrayOf(
                    100.toChar(),
                    97.toChar(),
                    105.toChar(),
                    97.toChar(),
                    110.toChar(),
                    97.toChar(),
                    100.toChar(),
                    49.toChar(),
                    57.toChar(),
                    55.toChar(),
                    56.toChar()
                )
            )
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManagerFactory.trustManagers, null)
            socketFactory = sslContext.socketFactory
        } catch (exx: Exception) {
            LoginActivity.logger?.append("Error initializing SSL: $exx")
        }
    }

    private fun connectToSocketServer() {
        Init()
        socketThread = Thread {
            while (isRunning) {
                if (!isconnected || reconnect) {
                    reconnect = false
                    LoopConnection()
                }
                try {
                    Thread.sleep(20)
                } catch (ignored: InterruptedException) {
                }
            }
        }
        socketThread!!.start()
        sendThread = Thread { sendToServer() }
        sendThread!!.start()
    }

    private fun LoopConnection() {
        try {
            closeSocket()
            socket = socketFactory!!.createSocket(SERVER_IP, SERVER_PORT) as SSLSocket
            socket!!.keepAlive = true
            socket!!.tcpNoDelay = true
            input = BufferedReader(InputStreamReader(socket!!.inputStream))
            output = socket!!.outputStream
            socket!!.startHandshake()
            isconnected = true
            LoginActivity.logger?.append("Connected to server")
            if (reconnect2) {
                Handlers.Reconnect(Singleton.rpk)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (MainActivity.isFocused) {
                        LoadChats().start()
                    }
                }, 200)
                reconnect2 = false
            }
            listenToServer()
        } catch (e: IOException) {
            isconnected = false
            reconnect = true
            reconnect2 = true
            PlaceholderFragment.people.clear()
            if (ChatActivity.isFocused) {
                ChatActivity.thiz.runOnUiThread { forceClose() }
            }
            if (MainActivity.isFocused) {
                MainActivity.activity?.runOnUiThread { PlaceholderFragment.arr?.notifyDataSetChanged() }
            }
            //LoginActivity.logger.append("Connection error: " + e.toString());
            try {
                Thread.sleep(20)
            } catch (ignored: InterruptedException) {
            }
        }
    }

    private fun listenToServer() {
        while (isconnected) {
            try {
                var dsize = ""
                val buffer = ByteArray(1)
                var bytesRead: Int
                while (socket!!.inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val charRead = String(buffer, 0, 1)
                    if (charRead == "L") {
                        break
                    }
                    dsize += charRead
                }
                var messageSize = 0
                try {
                    messageSize = dsize.toInt()
                } catch (ex: NumberFormatException) {
                    LoginActivity.logger?.append(ex.toString())
                    /*isconnected = false;
                    reconnect = true;
                    reconnect2 = true;
                    break;*/Process.killProcess(Process.myPid())
                }
                val messageBytes = ByteArray(messageSize)
                val dataInputStream = DataInputStream(socket!!.inputStream)
                dataInputStream.readFully(messageBytes)
                val message = String(messageBytes)
                if (!message.isEmpty()) {
                    //LoginActivity.logger.append("Received message: " + message);
                    Handlers.pass(message)
                }
            } catch (e: IOException) {
                LoginActivity.logger?.append("Listening error: $e")
                isconnected = false
                reconnect = true
                reconnect2 = true
                break
            }
            try {
                Thread.sleep(10)
            } catch (ignored: InterruptedException) {
            }
        }
    }

    private fun sendToServer() {
        while (isRunning) {
            if (isconnected && !reqs.isEmpty()) {
                try {
                    for (i in reqs.indices) {
                        val toSend = reqs[i]
                        if (!toSend.isEmpty()) {
                            sendMessageToServer(toSend)
                            reqs.removeAt(i)
                        }
                    }
                } catch (e: IndexOutOfBoundsException) {}
            }
            try {
                Thread.sleep(20)
            } catch (ignored: InterruptedException) {
            }
        }
    }

    private fun closeSocket() {
        try {
            if (socket != null) {
                socket!!.close()
                socket = null
            }
            if (input != null) {
                input!!.close()
                input = null
            }
            if (output != null) {
                output!!.close()
                output = null
            }
            isconnected = false
            //LoginActivity.logger.append("Socket closed");
        } catch (e: IOException) {
            LoginActivity.logger?.append("Error closing socket: $e")
        }
    }

    fun sendMessageToServer(message: String) {
        Thread {
            try {
                val formattedMessage = message.length.toString() + "L" + message
                output!!.write(formattedMessage.toByteArray())
                output!!.flush()
                //LoginActivity.logger.append("Sent message: " + message);
            } catch (e: IOException) {
                reconnect = true
                reconnect2 = true
                LoginActivity.logger?.append("Send error: $e")
            }
        }.start()
    }

    companion object {
        private const val SERVER_IP = "axuimflz4.localto.net"
        private const val SERVER_PORT = 1453
        @JvmField
        var reqs: MutableList<String> = mutableListOf()
        var reconnect = false
        var reconnect2 = false
        @JvmField
        var isRunning = false
        @JvmField
        var isconnected = false
    }
}