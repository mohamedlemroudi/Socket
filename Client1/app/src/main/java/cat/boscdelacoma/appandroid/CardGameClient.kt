package cat.boscdelacoma.appandroid

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class CardGameClient(private val textView: TextView) : AsyncTask<String, String, Void>() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var socket: Socket

    override fun doInBackground(vararg messages: String?): Void? {
        try {
            val serverIP = "192.168.18.131"  // Cambia esto con la dirección IP del servidor
            val serverPort = 12345

            socket = Socket(serverIP, serverPort)

            val playerName = "UsuarioAndroid_1"
            PrintWriter(socket.getOutputStream(), true).apply { println(playerName) }

            messages.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                PrintWriter(socket.getOutputStream(), true).apply { println(it) }
            }

            BufferedReader(InputStreamReader(socket.getInputStream())).use { `in` ->
                var active = true

                while (active) {
                    `in`.readLine()?.let { respuesta ->
                        publishProgress(respuesta)
                    } ?: run {
                        // El servidor cerró la conexión
                        active = false
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            socket.close()
        }
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        values[0]?.let { respuesta ->
            handler.post {
                textView.text = respuesta
                (textView.context as? MainActivity)?.handleServerResponse(respuesta)
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCancelled() {
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}