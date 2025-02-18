package ge.nikka.packages

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import ge.nikka.packages.ChatActivity
import ge.nikka.packages.ChatActivity.Companion.addNewMessage
import ge.nikka.packages.ChatActivity.Companion.containsOnlyEmojis
import ge.nikka.packages.LoginActivity.Companion.addReq
import ge.nikka.packages.Singleton.Companion.getInstance
import ge.nikka.packages.ui.main.PlaceholderFragment
import ge.nikka.packages.ui.main.PlaceholderFragment.Companion.getRoundedCroppedBitmap
import jp.wasabeef.blurry.Blurry
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.security.SecureRandom
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.abs

object Handlers {
    private val received: MutableMap<String, String> = ConcurrentHashMap()
    private val incoming: MutableMap<String, String> = ConcurrentHashMap()
    private val notifincoming: MutableMap<String, String> = ConcurrentHashMap()
    private val requests: MutableMap<String, String> = ConcurrentHashMap()
    val icons: MutableMap<String, Bitmap> = HashMap()
    private var lres = "empty"
    var currentActivity: Activity? = null
    private val exect: Executor = Executors.newSingleThreadExecutor()
    var icservice = Executors.newSingleThreadExecutor()
    var loadedClass: Class<*>? = null
    fun contains(`in`: String, target: String?): Boolean {
        return `in`.contains(target!!)
    }

    fun equals(first: String, second: String): Boolean {
        return first == second
    }

    @Throws(Exception::class)
    fun AESEncrypt(strSrc: String?, key: String?, iv: String?): String {
        return try {
            val aesMethod = loadedClass!!.getDeclaredMethod(
                "AESEncrypt",
                String::class.java,
                String::class.java,
                String::class.java
            )
            val ret = aesMethod.invoke(null, strSrc, key, iv)
            ret as String
        } catch (ex: Exception) {
            LoginActivity.logger!!.append(ex.toString())
            ""
        }
    }

    @Throws(Exception::class)
    fun AESDecrypt(strSrc: String?, key: String?, iv: String?): String {
        return try {
            val aesMethod = loadedClass!!.getDeclaredMethod(
                "AESDecrypt",
                String::class.java,
                String::class.java,
                String::class.java
            )
            val ret = aesMethod.invoke(null, strSrc, key, iv)
            ret as String
        } catch (ex: Exception) {
            LoginActivity.logger!!.append(ex.toString())
            ""
        }
    }

    fun popOut(): Animation {
        val fadeIn: Animation = AlphaAnimation(0f, 1f)
        fadeIn.duration = 200
        return fadeIn
    }

    fun popIn(): Animation {
        val fadeIn: Animation = AlphaAnimation(1f, 0f)
        fadeIn.duration = 200
        return fadeIn
    }

    fun replaceString(subject: String, search: String?, replace: String?): String {
        return subject.replace(search!!, replace!!)
    }

    fun RPB(str: String): String {
        return replaceString(replaceString(str, "\"", ""), "\"", "")
    }

    fun xorEncryptDecrypt(input: String?): String {
        return try {
            val xorMethod = loadedClass!!.getDeclaredMethod("xorEncryptDecrypt", String::class.java)
            val ret = xorMethod.invoke(null, input)
            ret as String
        } catch (ex: Exception) {
            LoginActivity.logger!!.append(ex.toString())
            ""
        }
    }

    fun showKeyboard(view: View) {
        view.requestFocus()
        view.postDelayed({
            val imm =
                currentActivity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }, 150)
    }

    fun stringToHex(text: String?): String {
        return try {
            val hexMethod = loadedClass!!.getDeclaredMethod("stringToHex", String::class.java)
            val ret = hexMethod.invoke(null, text)
            ret as String
        } catch (ex: Exception) {
            LoginActivity.logger!!.append(ex.toString())
            ""
        }
    }

    fun hexToString(hex: String?): String {
        return try {
            val hexMethod = loadedClass!!.getDeclaredMethod("hexToString", String::class.java)
            val ret = hexMethod.invoke(null, hex)
            ret as String
        } catch (ex: Exception) {
            LoginActivity.logger!!.append(ex.toString())
            ""
        }
    }

    fun iterMsg(msg: String?, mde: Int): String? {
        if (mde == 1) {
            return stringToHex(msg)
        } else if (mde == 2) {
            return hexToString(msg)
        }
        return null
    }

    fun pass(msg: String?) {
        val tmp = xorEncryptDecrypt(hexToString(msg))
        if (contains(tmp, "name")) {
            try {
                val jobj = JSONObject(tmp)
                val tag = jobj.getString("name")
                if (equals(tag, "inbox")) {
                    incoming[tag] = tmp
                    notifincoming[tag] = tmp
                } else if (equals(tag, "rtk")) {
                    if (jobj.has("value")) {
                        getInstance().myUid = jobj.getString("value")
                        //LoginActivity.logger.append("Value: " + Singleton.getInstance().getMyUid());
                    }
                    Singleton.rpk = jobj.getString("data")
                    //LoginActivity.logger.append("Token: " + Singleton.rpk);
                } else {
                    received[tag] = tmp
                }
            } catch (ex: JSONException) {
                LoginActivity.logger!!.append(ex.toString())
            }
        }
    }

    fun addRequest(req: String?) {
        val output = xorEncryptDecrypt(req)
        addReq(stringToHex(output))
    }

    var messageCheck = Thread {
        while (true) {
            val inc = ccall()
            if (inc !== "nothing") {
                try {
                    val jobj = JSONObject(inc)
                    val dobj = jobj.getJSONObject("data")
                    if (!ChatActivity.isFocused || !getInstance().uid.contains(dobj.getString("i"))) {
                        if (!ChatActivity.isFocused && getInstance().uid.contains(dobj.getString("i"))) {
                            ChatActivity.thiz.runOnUiThread {
                                try {
                                    addNewMessage(
                                        jobj.getString("value"),
                                        jobj.getString("mid"),
                                        jobj.getString("reply"),
                                        false,
                                        true,
                                        false
                                    )
                                } catch (ex: JSONException) {
                                }
                            }
                        }
                    } else {
                        ChatActivity.thiz.runOnUiThread {
                            try {
                                val vibrator =
                                    ChatActivity.thiz.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                if (vibrator != null && vibrator.hasVibrator()) {
                                    vibrator.vibrate(
                                        VibrationEffect.createWaveform(
                                            longArrayOf(
                                                0,
                                                10
                                            ), -1
                                        )
                                    )
                                }
                                addNewMessage(
                                    jobj.getString("value"),
                                    jobj.getString("mid"),
                                    jobj.getString("reply"),
                                    false,
                                    true,
                                    false
                                )
                            } catch (ex: JSONException) {
                            }
                        }
                    }
                } catch (ex: JSONException) {
                    LoginActivity.logger!!.append(ex.toString())
                }
            }
            try {
                Thread.sleep(10)
            } catch (ex: InterruptedException) {
                LoginActivity.logger!!.append(ex.toString())
            }
        }
    }

    fun savePic(bitmap: Bitmap, uid: String) {
        val filePath = File(currentActivity!!.cacheDir.absolutePath + "/" + uid + ".png")
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        } catch (e: IOException) {
            LoginActivity.logger!!.append(e.toString())
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    LoginActivity.logger!!.append(e.toString())
                }
            }
        }
    }

    fun checkFileAndDoAction(bitmap: Bitmap, uid: String) {
        val file = File(currentActivity!!.cacheDir.absolutePath + "/" + uid + ".png")
        if (!file.exists()) {
            savePic(bitmap, uid)
        }
    }

    fun startIconThread() {
        if (!icservice.isShutdown) icservice.shutdown()
        icservice = Executors.newSingleThreadExecutor()
        icservice.execute(ic_thread)
    }

    var ic_thread = Thread {
        while (true) {
            try {
                for ((key, value) in icons) {
                    checkFileAndDoAction(value, key)
                }
            } catch (ex: ConcurrentModificationException) {
                LoginActivity.logger!!.append(ex.toString())
            }
            try {
                Thread.sleep(50)
            } catch (ex: InterruptedException) {
                LoginActivity.logger!!.append(ex.toString())
            }
        }
    }

    fun loginT(tkn: String?) {
        val data = JSONObject()
        try {
            data.put("name", "authgoogle")
            data.put("data", tkn)
        } catch (ex: JSONException) {
            LoginActivity.logger!!.append(ex.toString())
        }
        requests["loginr"] = data.toString()
        var `val`: String?
        do {
            `val` = received["rlogin"]
        } while (`val` == null)
        received.remove("rlogin")
        lres = `val`
    }

    fun pcall(): String? {
        return if (incoming.isEmpty()) {
            "nothing"
        } else {
            val result = incoming["inbox"]
            incoming.remove("inbox")
            result
        }
    }

    fun ccall(): String? {
        return if (notifincoming.isEmpty()) {
            "nothing"
        } else {
            val result = notifincoming["inbox"]
            notifincoming.remove("inbox")
            result
        }
    }

    fun checkLoginResult(): String {
        val result = lres
        lres = "empty"
        return result
    }

    fun checkRequests() {
        var `val` = requests["loginr"]
        if (`val` != null) {
            addRequest(`val`)
            requests.remove("loginr")
        } else if (requests["loginrs"].also { `val` = it } != null) {
            addRequest(`val`)
            requests.remove("loginrs")
        }
    }

    val chats: String
        get() {
            val data = JSONObject()
            try {
                data.put("name", "chats")
                data.put("data", "0")
                data.put("value", "0")
            } catch (ex: JSONException) {
            }
            addRequest(data.toString())
            var `val`: String?
            do {
                `val` = received["rchats"]
            } while (`val` == null)
            //LoginActivity.logger.append(val);
            received.remove("rchats")
            return `val`
        }

    fun getMessages(usid: String?, page: Int, size: Int): String {
        val data = JSONObject()
        try {
            data.put("name", "messages")
            data.put("data", usid)
            data.put("value", "" + page)
            data.put("psize", "" + size)
        } catch (ex: JSONException) {
        }
        addRequest(data.toString())
        var `val`: String?
        do {
            `val` = received["rmessages"]
        } while (`val` == null)
        received.remove("rmessages")
        // LoginActivity.logger.append(val);
        return `val`
    }

    fun getIcon(link: String?): CompletableFuture<Bitmap> {
        return CompletableFuture.supplyAsync({
            try {
                val url = URL(link)
                return@supplyAsync BitmapFactory.decodeStream(url.openStream())
            } catch (e: IOException) {
                LoginActivity.logger!!.append(e.toString())
                return@supplyAsync null
            }
        }, exect)
    }

    fun cancellationToken(): String {
        val LENGTH = 24
        val CHARACTERS = "abcdef0123456789"
        val RANDOM = SecureRandom()
        val sb = StringBuilder()
        for (i in 0 until LENGTH) {
            if (i > 0 && i % 6 == 0) {
                sb.append('-')
            }
            val index = RANDOM.nextInt(CHARACTERS.length)
            sb.append(CHARACTERS[index])
        }
        return sb.toString()
    }

    fun sendMessage(usid: String?, mesg: String?, reply: String?, tkn: String?) {
        val data = JSONObject()
        try {
            data.put("name", "sendmsg")
            data.put("data", usid)
            data.put("value", mesg)
            data.put("reply", reply)
            data.put("mid", tkn)
        } catch (ex: JSONException) {
        }
        addRequest(data.toString())
        //ChatActivity.loadedMessages.add(tkn);
    }

    fun Reconnect(rtk: String?) {
        val data = JSONObject()
        try {
            data.put("name", "rtk")
            data.put("data", rtk)
        } catch (ex: JSONException) {
            LoginActivity.logger!!.append(ex.toString())
        }
        addRequest(data.toString())
        //LoginActivity.logger.append(data.toString());
    }

    fun getMessage(usid: String?, mid: String?) {
        val tkn = cancellationToken()
        val data = JSONObject()
        try {
            data.put("name", "getmsg")
            data.put("data", usid)
            data.put("value", mid)
            //data.put("mid", tkn);
        } catch (ex: JSONException) {
        }
        addRequest(data.toString())
    }

    fun newChat(usern: String?): String {
        val data = JSONObject()
        try {
            data.put("name", "newchat")
            data.put("data", usern)
        } catch (ex: JSONException) {
        }
        addRequest(data.toString())
        var `val`: String?
        do {
            `val` = received["newchat"]
        } while (`val` == null)
        received.remove("newchat")
        return `val`
    }

    @JvmStatic
    val myPfp: String
        get() {
            val data = JSONObject()
            try {
                data.put("name", "mypfp")
            } catch (ex: JSONException) {
            }
            addRequest(data.toString())
            var `val`: String?
            do {
                `val` = received["mypfp"]
            } while (`val` == null)
            received.remove("mypfp")
            return `val`
        }

    fun getStatus(uid: String?): String {
        val data = JSONObject()
        try {
            data.put("name", "getstat")
            data.put("data", uid)
        } catch (ex: JSONException) {
        }
        addRequest(data.toString())
        var `val`: String?
        do {
            `val` = received["getstat"]
        } while (`val` == null)
        received.remove("getstat")
        return `val`
    }

    fun setStatus(stat: String?) {
        val data = JSONObject()
        try {
            data.put("name", "setstat")
            data.put("value", stat)
        } catch (ex: JSONException) {
        }
        addRequest(data.toString())
    }

    fun sortItemsByTimestamp(items: ArrayList<ItemClass>): ArrayList<ItemClass> {
        Collections.sort(items) { a: ItemClass, b: ItemClass ->
            java.lang.Long.compare(
                b.time,
                a.time
            )
        }
        return items
    }

    var icc: Bitmap? = null
    var itm: ItemClass? = null
    var extii = 0

    internal class CipherClass(n: Int) {
        private val map: Map<Char, Char>

        init {
            map = makeMap(n)
        }

        private fun makeMap(n: Int): Map<Char, Char> {
            val map: MutableMap<Char, Char> = HashMap()
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            val m = alphabet.length
            for (i in 0 until m) {
                map[alphabet[i]] = alphabet[(i + n + m) % m]
            }
            return map
        }

        fun encrypt(s: String): String {
            val encrypted = StringBuilder()
            for (c in s.toCharArray()) {
                encrypted.append(map[c] ?: c)
            }
            return encrypted.toString()
        }
    }

    class LoadChatPage : Thread() {
        var thiz: Thread? = null

        init {
            thiz = this
        }

        override fun run() {
            super.run()
            var msgs = ""
            while (msgs.length <= 0) {
                msgs = getMessages(getInstance().uid, 0, 24)
            }
            try {
                val mobj = JSONObject(msgs)
                val size = mobj.getString("value").toInt() + 1
                val start = mobj.getString("start").toInt()
                for (i in start until size) {
                    val arr = mobj.getJSONObject("data").getJSONArray(i.toString())
                    val mid = StringBuilder()
                    val `val` = StringBuilder()
                    val replyto = StringBuilder()
                    var time: Long = 0
                    for (j in 0 until arr.length()) {
                        val typ = arr.getJSONObject(j)
                        if (typ.getString("Name") == "mid") {
                            mid.append(typ.getString("Value"))
                        } else if (typ.getString("Name") == "val") {
                            `val`.append(typ.getString("Value"))
                        } else if (typ.getString("Name") == "replyto") {
                            replyto.append(typ.getString("Value"))
                        } else if (typ.getString("Name") == "time") {
                            time = typ.getLong("Value")
                        }
                    }
                    //ChatActivity.loadedMessages.add(mid.toString());
                    val mes = `val`.toString()
                    if (mes.substring(0, 24) == getInstance().myUid) {
                        val rinfo = replyto.toString()
                        if (rinfo.length >= 27) {
                            currentActivity!!.runOnUiThread {
                                addNewMessage(
                                    iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), rinfo, true, false, false
                                )
                            }
                        } else {
                            currentActivity!!.runOnUiThread {
                                addNewMessage(
                                    iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), "none", true, false, false
                                )
                            }
                        }
                    } else {
                        val rinfo = replyto.toString()
                        if (rinfo.length >= 27) {
                            currentActivity!!.runOnUiThread {
                                addNewMessage(
                                    iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), rinfo, false, false, false
                                )
                            }
                        } else {
                            currentActivity!!.runOnUiThread {
                                addNewMessage(
                                    iterMsg(
                                        mes.substring(25),
                                        2
                                    ), mid.toString(), "none", false, false, false
                                )
                            }
                        }
                    }
                }
                currentActivity!!.runOnUiThread {
                    val horizontalLayout2 =
                        ChatActivity.thiz.findViewById<LinearLayout>(R.id.progressLayout)
                    val mainScroll = ChatActivity.thiz.findViewById<ScrollView>(R.id.chatScroll)
                    horizontalLayout2.layoutParams = LinearLayout.LayoutParams(
                        0,
                        0
                    )
                    horizontalLayout2.visibility = View.INVISIBLE
                    mainScroll.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                    mainScroll.visibility = View.VISIBLE
                    mainScroll.animation = popOut()
                    mainScroll.postDelayed({ mainScroll.clearAnimation() }, 200)
                    if (!ChatActivity.isFocused) return@runOnUiThread
                    mainScroll.postDelayed({
                        mainScroll.fullScroll(View.FOCUS_DOWN)
                        ChatActivity.editTextMessage.requestFocus()
                    }, 400)
                }
            } catch (ex: JSONException) {
                if (ex.toString().contains("No value for 0")) {
                    currentActivity!!.runOnUiThread {
                        val horizontalLayout2 =
                            ChatActivity.thiz.findViewById<LinearLayout>(R.id.progressLayout)
                        val mainScroll = ChatActivity.thiz.findViewById<ScrollView>(R.id.chatScroll)
                        horizontalLayout2.layoutParams = LinearLayout.LayoutParams(
                            0,
                            0
                        )
                        horizontalLayout2.visibility = View.INVISIBLE
                        mainScroll.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                        mainScroll.visibility = View.VISIBLE
                        val nvi = TextView(ChatActivity.thiz)
                        nvi.gravity = Gravity.CENTER
                        nvi.text = "No messages to show\n\nSend a message to initiate chat…"
                        nvi.textSize = 19f
                        nvi.setPadding(0, 0, 0, 0)
                        nvi.animation = popOut()
                        nvi.id = abs("nomsgtitle".hashCode().toDouble()).toInt()
                        (mainScroll.findViewById<View>(R.id.messageContainer) as LinearLayout).gravity =
                            Gravity.CENTER
                        (mainScroll.findViewById<View>(R.id.messageContainer) as LinearLayout).addView(
                            nvi
                        )
                    }
                }
                LoginActivity.logger!!.append(ex.toString())
            }
        }
    }

    class LoadChats : Thread() {
        var thiz: Thread? = null

        init {
            thiz = this
        }

        override fun run() {
            super.run()
            val uuser =
                BitmapFactory.decodeResource(MainActivity.activity!!.resources, R.drawable.profile)
            val cuser = getRoundedCroppedBitmap(Bitmap.createScaledBitmap(uuser, 200, 200, false))
            if (!SocketService.isconnected) {
                return
            }
            var chatstr = ""
            while (chatstr.length <= 0) {
                chatstr = chats
            }
            try {
                if (!MainActivity.isFocused) {
                    return
                }
                currentActivity!!.runOnUiThread {
                    val llayout =
                        PlaceholderFragment.fragment!!.requireView().findViewById<LinearLayout>(R.id.chatsPage)
                    if (!MainActivity.isFocused) return@runOnUiThread
                    var horizontalLayout =
                        llayout.findViewById<LinearLayout>(R.id.horizontal_layout)
                    if (!MainActivity.isFocused) return@runOnUiThread
                    if (llayout.indexOfChild(PlaceholderFragment.apps) != -1) {
                        if (!MainActivity.isFocused) return@runOnUiThread
                        if (PlaceholderFragment.apps!!.parent != null) {
                            if (!MainActivity.isFocused) return@runOnUiThread
                            (PlaceholderFragment.apps!!.parent as ViewGroup).removeView(
                                PlaceholderFragment.apps
                            )
                        }
                        if (!MainActivity.isFocused) return@runOnUiThread
                        llayout.removeView(PlaceholderFragment.apps)
                    }
                    if (!MainActivity.isFocused) return@runOnUiThread
                    if (llayout.indexOfChild(horizontalLayout) == -1) {
                        if (horizontalLayout != null) {
                            if (!MainActivity.isFocused) return@runOnUiThread
                            llayout.addView(horizontalLayout)
                            if (!MainActivity.isFocused) return@runOnUiThread
                            horizontalLayout.animation = popOut()
                        } else {
                            if (!MainActivity.isFocused) return@runOnUiThread
                            horizontalLayout = MainActivity.activity!!.layoutInflater.inflate(
                                R.layout.chat_loading,
                                null
                            ).findViewById(R.id.horizontal_layout)
                            if (!MainActivity.isFocused) return@runOnUiThread
                            horizontalLayout.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            if (!MainActivity.isFocused) return@runOnUiThread
                            horizontalLayout.gravity = Gravity.CENTER
                            if (!MainActivity.isFocused) return@runOnUiThread
                            llayout.addView(horizontalLayout)
                            if (!MainActivity.isFocused) return@runOnUiThread
                            horizontalLayout.animation = popOut()
                        }
                    }
                }
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException exp) {}*/
                val chatobj = JSONObject(chatstr)
                val size = chatobj.getString("value").toInt()
                val xtd = chatobj.getJSONObject("data")
                if (!xtd.has("0") && size == 0) {
                    if (!MainActivity.isFocused) return
                    currentActivity!!.runOnUiThread {
                        if (!MainActivity.isFocused) return@runOnUiThread
                        val llayout =
                            PlaceholderFragment.fragment!!.requireView().findViewById<LinearLayout>(R.id.chatsPage)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        llayout.removeAllViews()
                        if (!MainActivity.isFocused) return@runOnUiThread
                        val nvi = TextView(currentActivity)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        nvi.gravity = Gravity.CENTER
                        if (!MainActivity.isFocused) return@runOnUiThread
                        nvi.text = "No Chats to show\n\nStart chatting with users…"
                        if (!MainActivity.isFocused) return@runOnUiThread
                        nvi.textSize = 19f
                        if (!MainActivity.isFocused) return@runOnUiThread
                        nvi.setPadding(0, 0, 0, 300)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        nvi.animation = popOut()
                        llayout.addView(nvi)
                    }
                    return
                }
                extii = 0
                PlaceholderFragment.people.clear()
                for (i in 0 until size) {
                    //final ItemClass itm = null;
                    itm = null
                    extii = i
                    val inf = chatobj.getJSONObject("data").getJSONObject(i.toString())
                    val fmt = inf.getString("fm")
                    val ftime = inf.getLong("fmtm")
                    //LoginActivity.logger.append("time: " + ftime);
                    if (fmt.substring(0, 24) == getInstance().myUid) {
                        val hint = "You: " + iterMsg(fmt.substring(25, fmt.length), 2)!!
                            .replace("\n", " ")
                        val oemj = containsOnlyEmojis(hint.replace("You: ", ""))
                        if (oemj) {
                            if (hint.length >= 28) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 28) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        } else {
                            if (hint.length >= 40) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 40) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        }
                    } else {
                        val hint = iterMsg(fmt.substring(25, fmt.length), 2)!!
                            .replace("\n", " ")
                        val oemj = containsOnlyEmojis(hint)
                        if (oemj) {
                            if (hint.length >= 25) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 25) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        } else {
                            if (hint.length >= 40) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 40) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        }
                    }
                    itm!!.time = ftime
                    val file =
                        File(currentActivity!!.cacheDir.absolutePath + "/" + inf.getString("i") + ".png")
                    if (file.exists()) {
                        try {
                            itm!!.icon = getRoundedCroppedBitmap(
                                Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeFile(file.absolutePath), 200, 200, false
                                )
                            )
                            icons[inf.getString("i")] = BitmapFactory.decodeFile(file.absolutePath)
                        } catch (ex: NullPointerException) {
                            LoginActivity.logger!!.append(ex.toString())
                        }
                    } else {
                        getIcon(inf.getString("p")).thenAccept { bitmap: Bitmap? ->
                            if (bitmap != null) {
                                try {
                                    icons[inf.getString("i")] = bitmap
                                } catch (ex: JSONException) {
                                    LoginActivity.logger!!.append(ex.toString())
                                }
                                itm!!.icon = getRoundedCroppedBitmap(
                                    Bitmap.createScaledBitmap(
                                        bitmap,
                                        200,
                                        200,
                                        false
                                    )
                                )
                            } else {
                                itm!!.icon = cuser
                            }
                        }
                        while (itm!!.icon == null) {
                        }
                    }
                    if (!MainActivity.isFocused) {
                        return
                    }
                    //itm.icon = cuser;
                    try {
                        PlaceholderFragment.people.add(itm!!)
                    } catch (exp: NullPointerException) {
                        continue
                    }
                }
                if (extii > 1) {
                    val ktmp = sortItemsByTimestamp(PlaceholderFragment.people)
                    PlaceholderFragment.people = ktmp
                }
                if (!MainActivity.isFocused) return
                currentActivity!!.runOnUiThread {
                    try {
                        if (!MainActivity.isFocused) return@runOnUiThread
                        val llayout =
                            PlaceholderFragment.fragment!!.requireView().findViewById<LinearLayout>(R.id.chatsPage)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        val horizontalLayout =
                            llayout.findViewById<LinearLayout>(R.id.horizontal_layout)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        //PlaceholderFragment.arr.clear();
                        //PlaceholderFragment.arr.setValue(PlaceholderFragment.people)
                        PlaceholderFragment.arr!!.notifyDataSetChanged()
                        if (!MainActivity.isFocused) return@runOnUiThread
                        llayout.removeView(horizontalLayout)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        if (PlaceholderFragment.apps!!.parent != null) {
                            if (!MainActivity.isFocused) return@runOnUiThread
                            (PlaceholderFragment.apps!!.parent as ViewGroup).removeView(
                                PlaceholderFragment.apps
                            )
                        }
                        if (!MainActivity.isFocused) return@runOnUiThread
                        llayout.addView(PlaceholderFragment.apps)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        PlaceholderFragment.apps!!.animation = popOut()
                        if (!MainActivity.isFocused) return@runOnUiThread
                        PlaceholderFragment.apps!!.postDelayed({
                            if (!MainActivity.isFocused) return@postDelayed
                            PlaceholderFragment.apps!!.clearAnimation()
                        }, 200)
                    } catch (ext: NullPointerException) {
                        LoginActivity.logger!!.append(ext.toString())
                    }
                }
            } catch (ex: JSONException) {
                Toast.makeText(currentActivity, ex.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    class RefreshChats : Thread() {
        var thiz: Thread? = null
        var exti = 0

        init {
            thiz = this
        }

        override fun run() {
            super.run()
            val uuser =
                BitmapFactory.decodeResource(MainActivity.activity!!.resources, R.drawable.profile)
            val cuser = getRoundedCroppedBitmap(Bitmap.createScaledBitmap(uuser, 200, 200, false))
            var chatstr = ""
            while (chatstr.length <= 0) {
                chatstr = chats
            }
            try {
                if (!MainActivity.isFocused) return
                currentActivity!!.runOnUiThread {
                    if (!MainActivity.isFocused) return@runOnUiThread
                    val llayout =
                        PlaceholderFragment.fragment!!.requireView().findViewById<LinearLayout>(R.id.chatsPage)
                    if (!MainActivity.isFocused) return@runOnUiThread
                    for (i in llayout.childCount - 1 downTo 0) {
                        if (!MainActivity.isFocused) return@runOnUiThread
                        val child = llayout.getChildAt(i)
                        if (!MainActivity.isFocused) return@runOnUiThread
                        if (child is TextView) {
                            if (!MainActivity.isFocused) return@runOnUiThread
                            llayout.removeViewAt(i)
                        }
                    }
                    if (!MainActivity.isFocused) return@runOnUiThread
                    if (llayout.indexOfChild(PlaceholderFragment.apps) == -1) {
                        if (!MainActivity.isFocused) return@runOnUiThread
                        llayout.addView(PlaceholderFragment.apps)
                    }
                }
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException exp) {}*/
                val chatobj = JSONObject(chatstr)
                val size = chatobj.getString("value").toInt()
                if (currentThread().isInterrupted) {
                    return
                }
                exti = 0
                for (i in 0 until size) {
                    //final ItemClass itm = null;
                    itm = null
                    exti = i
                    val inf = chatobj.getJSONObject("data").getJSONObject(i.toString())
                    val fmt = inf.getString("fm")
                    val ftime = inf.getLong("fmtm")
                    if (fmt.substring(0, 24) == getInstance().myUid) {
                        val hint = "You: " + iterMsg(fmt.substring(25, fmt.length), 2)!!
                            .replace("\n", " ")
                        val oemj = containsOnlyEmojis(hint.replace("You: ", ""))
                        if (oemj) {
                            if (hint.length >= 28) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 28) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        } else {
                            if (hint.length >= 40) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 40) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        }
                    } else {
                        val hint = iterMsg(fmt.substring(25, fmt.length), 2)!!
                            .replace("\n", " ")
                        val oemj = containsOnlyEmojis(hint)
                        if (oemj) {
                            if (hint.length >= 25) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 25) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        } else {
                            if (hint.length >= 40) itm = ItemClass(
                                inf.getString("n"),
                                hint.substring(0, 40) + "…",
                                inf.getString("i")
                            ) else itm = ItemClass(inf.getString("n"), hint, inf.getString("i"))
                        }
                    }
                    itm!!.time = ftime
                    val file =
                        File(currentActivity!!.cacheDir.absolutePath + "/" + inf.getString("i") + ".png")
                    if (file.exists()) {
                        itm!!.icon = getRoundedCroppedBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeFile(file.absolutePath), 200, 200, false
                            )
                        )
                        icons[inf.getString("i")] = BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        getIcon(inf.getString("p")).thenAccept { bitmap: Bitmap? ->
                            if (bitmap != null) {
                                try {
                                    icons[inf.getString("i")] = bitmap
                                } catch (ex: JSONException) {
                                    LoginActivity.logger!!.append(ex.toString())
                                }
                                itm!!.icon = getRoundedCroppedBitmap(
                                    Bitmap.createScaledBitmap(
                                        bitmap,
                                        200,
                                        200,
                                        false
                                    )
                                )
                            } else {
                                itm!!.icon = cuser
                            }
                        }
                        while (itm!!.icon == null) {
                        }
                    }
                    if (!MainActivity.isFocused) {
                        return
                    }
                    //itm.icon = cuser;
                    if (i == 0) PlaceholderFragment.people.clear()
                    PlaceholderFragment.people.add(itm!!)
                }
                if (extii > 1) {
                    val ktmp = sortItemsByTimestamp(PlaceholderFragment.people)
                    PlaceholderFragment.people = ktmp
                }
                if (!MainActivity.isFocused) return
                currentActivity!!.runOnUiThread {
                    try {
                        if (!MainActivity.isFocused) return@runOnUiThread
                        if (exti == size - 1) PlaceholderFragment.arr!!.notifyDataSetChanged()
                        /*PlaceholderFragment.apps.setAnimation(popOut());
                            PlaceholderFragment.apps.postDelayed(() -> {
                                PlaceholderFragment.apps.clearAnimation();
                            }, 200);*/
                    } catch (ext: NullPointerException) {
                        LoginActivity.logger!!.append(ext.toString())
                    }
                }
            } catch (ex: JSONException) {
                LoginActivity.logger!!.append(ex.toString())
            }
        }
    }

    class NewChat(private val usern: String) : Thread() {
        private var dialog: AlertDialog? = null
        override fun run() {
            super.run()
            try {
                currentActivity!!.runOnUiThread {
                    val dview = currentActivity!!.layoutInflater.inflate(R.layout.prog_dialog, null)
                    val ld = dview.findViewById<TextView>(R.id.text_loading)
                    val cpd = dview.findViewById<CircularProgressIndicator>(R.id.progress_circular)
                    cpd.setIndicatorColor(MainActivity.ccolor!!.toArgb())
                    ld.text = "Searching for target user…"
                    Blurry.with(currentActivity)
                        .radius(10)
                        .sampling(2)
                        .async()
                        .onto(currentActivity!!.window.decorView as ViewGroup)
                    val bld = MaterialAlertDialogBuilder(currentActivity!!)
                    bld.setTitle("Please wait!..").setView(dview).setCancelable(false)
                        .setOnDismissListener { Blurry.delete(currentActivity!!.window.decorView as ViewGroup) }
                        .create()
                    dialog = bld.show()
                }
                var `val` = ""
                while (`val`.length <= 0) {
                    `val` = newChat(usern)
                }
                //LoginActivity.logger.append(val);
                val jobj = JSONObject(`val`)
                val status = jobj.getString("status")
                if (status == "no") {
                    currentActivity!!.runOnUiThread {
                        dialog!!.dismiss()
                        Blurry.with(currentActivity)
                            .radius(10)
                            .sampling(2)
                            .async()
                            .onto(currentActivity!!.window.decorView as ViewGroup)
                        val bld = MaterialAlertDialogBuilder(currentActivity!!)
                        bld.setTitle("Error").setMessage("Target user not found…")
                            .setCancelable(true)
                            .setOnDismissListener { Blurry.delete(currentActivity!!.window.decorView as ViewGroup) }
                            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                        val dialg = bld.create()
                        dialg.show()
                        val positiveButton = dialg.getButton(DialogInterface.BUTTON_POSITIVE)
                        val negativeButton = dialg.getButton(DialogInterface.BUTTON_NEGATIVE)
                        val neutralButton = dialg.getButton(DialogInterface.BUTTON_NEUTRAL)
                        positiveButton.setTextColor(MainActivity.ccolor!!.toArgb())
                        negativeButton.setTextColor(MainActivity.ccolor!!.toArgb())
                        neutralButton.setTextColor(MainActivity.ccolor!!.toArgb())
                    }
                } else if (status == "already") {
                    icc = null
                    val dobj = jobj.getJSONObject("data")
                    val uuser = BitmapFactory.decodeResource(
                        MainActivity.activity!!.resources,
                        R.drawable.profile
                    )
                    val cuser =
                        getRoundedCroppedBitmap(Bitmap.createScaledBitmap(uuser, 200, 200, false))
                    val notificationIntent2 = Intent(currentActivity, ChatActivity::class.java)
                    notificationIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    notificationIntent2.putExtra("uid_value", dobj.getString("i"))
                    notificationIntent2.putExtra("name_value", dobj.getString("n"))
                    val file =
                        File(currentActivity!!.cacheDir.absolutePath + "/" + dobj.getString("i") + ".png")
                    if (file.exists()) {
                        icc = getRoundedCroppedBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeFile(file.absolutePath), 200, 200, false
                            )
                        )
                        if (!icons.containsKey(dobj.getString("i"))) icons[dobj.getString("i")] =
                            BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        getIcon(dobj.getString("p")).thenAccept { bitmap: Bitmap? ->
                            if (bitmap != null) {
                                try {
                                    icons[dobj.getString("i")] = bitmap
                                } catch (ex: JSONException) {
                                    LoginActivity.logger!!.append(ex.toString())
                                }
                                icc = getRoundedCroppedBitmap(
                                    Bitmap.createScaledBitmap(
                                        bitmap,
                                        200,
                                        200,
                                        false
                                    )
                                )
                            } else {
                                icc = cuser
                            }
                        }
                        while (icc == null) {
                        }
                    }
                    getInstance().icon = icc
                    currentActivity!!.runOnUiThread {
                        dialog!!.dismiss()
                        currentActivity!!.startActivity(notificationIntent2)
                    }
                } else if (status == "ok") {
                    icc = null
                    val dobj = jobj.getJSONObject("data")
                    val uuser = BitmapFactory.decodeResource(
                        MainActivity.activity!!.resources,
                        R.drawable.profile
                    )
                    val cuser =
                        getRoundedCroppedBitmap(Bitmap.createScaledBitmap(uuser, 200, 200, false))
                    val notificationIntent2 = Intent(currentActivity, ChatActivity::class.java)
                    notificationIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    notificationIntent2.putExtra("uid_value", dobj.getString("i"))
                    notificationIntent2.putExtra("name_value", dobj.getString("n"))
                    val file =
                        File(currentActivity!!.cacheDir.absolutePath + "/" + dobj.getString("i") + ".png")
                    if (file.exists()) {
                        icc = getRoundedCroppedBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeFile(file.absolutePath), 200, 200, false
                            )
                        )
                        if (!icons.containsKey(dobj.getString("i"))) icons[dobj.getString("i")] =
                            BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        getIcon(dobj.getString("p")).thenAccept { bitmap: Bitmap? ->
                            if (bitmap != null) {
                                try {
                                    icons[dobj.getString("i")] = bitmap
                                } catch (ex: JSONException) {
                                    LoginActivity.logger!!.append(ex.toString())
                                }
                                icc = getRoundedCroppedBitmap(
                                    Bitmap.createScaledBitmap(
                                        bitmap,
                                        200,
                                        200,
                                        false
                                    )
                                )
                            } else {
                                icc = cuser
                            }
                        }
                        while (icc == null) {
                        }
                    }
                    getInstance().icon = icc
                    currentActivity!!.runOnUiThread {
                        dialog!!.dismiss()
                        currentActivity!!.startActivity(notificationIntent2)
                    }
                }
            } catch (ex: JSONException) {
                LoginActivity.logger!!.append(ex.toString())
            }
        }
    }
}