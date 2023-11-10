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

class CardGameClient(private val textView: TextView, private val playerName: String) :
    AsyncTask<String, String, Void>() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var socket: Socket
    private lateinit var writer: PrintWriter
    private lateinit var reader: BufferedReader

    private var isRunning = true

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

    fun closeConnection() {
        isRunning = false
        socket.close()
    }

    fun startSendingMessages() {
        Thread {
            while (isRunning) {
                // Aquí puedes implementar la lógica para enviar mensajes continuamente
                val message = "Mensaje desde el cliente 1"
                sendMessage(message)
                Thread.sleep(1000) // espera 1 segundo entre mensajes (ajusta según sea necesario)
            }
        }.start()
    }

    fun connectAndSendPlayerName() {
        try {
            val serverIP = "172.23.3.161"
            val serverPort = 12345

            socket = Socket(serverIP, serverPort)

            // Inicializa el escritor después de establecer la conexión con el servidor
            writer = PrintWriter(socket.getOutputStream(), true)

            // Inicializa el lector
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            // Envía el nombre del jugador al servidor
            sendPlayerName()

            // Inicia el hilo para enviar mensajes continuamente
            startSendingMessages()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun doInBackground(vararg messages: String?): Void? {
        try {
            while (isRunning) {
                val buffer = CharArray(4096)
                val bytesRead = reader.read(buffer)
                if (bytesRead == -1) {
                    // El cliente se desconectó, cierra la conexión y sal del bucle
                    break
                }

                // Construir el mensaje acumulando los datos leídos
                val message = String(buffer, 0, bytesRead)
                publishProgress(message)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Manejar la desconexión aquí, por ejemplo, cerrando la conexión del lado del cliente
            closeConnection()
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
