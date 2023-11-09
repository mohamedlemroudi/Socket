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
            val serverIP = "172.23.3.161"
            val serverPort = 12345

            socket = Socket(serverIP, serverPort)

            val playerName = "Jaska"

            val out = PrintWriter(socket.getOutputStream(), true)
            out.println(playerName)

            // Enviar el mensaje proporcionado como parámetro
            if (messages.isNotEmpty()) {
                out.println(messages[0])
            }

            val `in` = BufferedReader(InputStreamReader(socket.getInputStream()))
            var active = true

            while (active) {
                val respuesta = `in`.readLine()
                if (respuesta != null) {
                    publishProgress(respuesta)
                } else {
                    // El servidor cerró la conexión
                    active = false
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        val respuesta = values[0]
        handler.post {
            textView.text = respuesta
            // Envía un mensaje de vuelta a la actividad principal
            if (respuesta != null) {
                (textView.context as? MainActivity)?.handleServerResponse(respuesta)
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        // Cierra la conexión después de que la tarea se completa
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCancelled() {
        // Cierra la conexión si la tarea es cancelada
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}