package cat.boscdelacoma.appandroid

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class CardGameClient(private val textView: TextView, private val playerName: String) : AsyncTask<String, String, Void>() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var socket: Socket
    private lateinit var writer: PrintWriter

    init {
        // Esta inicialización se realiza cuando se crea una instancia del cliente
        connectAndSendPlayerName()
    }

    fun sendPlayerName() {
        // Envía el nombre del jugador al servidor
        writer.println(playerName)
    }

    fun sendMessage(message: String) {
        // Asegúrate de que el socket y el escritor estén inicializados
        if (this::socket.isInitialized && this::writer.isInitialized) {
            // Envía el mensaje al servidor
            writer.println(message)
        } else {
            Log.e("CardGameClient", "Socket o escritor no inicializados.")
        }
    }

    fun connectAndSendPlayerName() {
        try {
            val serverIP = "172.23.3.161"
            val serverPort = 12345

            socket = Socket(serverIP, serverPort)

            // Inicializa el escritor después de establecer la conexión con el servidor
            writer = PrintWriter(socket.getOutputStream(), true)

            // Envía el nombre del jugador al servidor
            sendPlayerName()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun doInBackground(vararg messages: String?): Void? {
        try {
            // Este método ahora solo se encarga de mantener la conexión activa
            // y no realiza la conexión inicial o envío del nombre del jugador
            while (true) {
                // Puedes implementar aquí la lógica para leer mensajes del servidor si es necesario
                // por ejemplo, si el servidor envía algo de vuelta.
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        values[0]?.let { respuesta ->
            // Actualiza la interfaz de usuario desde el hilo principal
            handler.post {
                textView.text = respuesta
                (textView.context as? MainActivity)?.handleServerResponse(respuesta)
            }
        }
    }
}
