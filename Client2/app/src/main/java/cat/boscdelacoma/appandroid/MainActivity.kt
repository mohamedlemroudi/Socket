package cat.boscdelacoma.appandroid

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var editTextMessage: EditText
    private lateinit var btnSendMessage: Button
    private lateinit var cardGameClient: CardGameClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Permitir operaciones de red en el hilo principal (solo para depuración)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitNetwork()
                .build()
        )

        textView = findViewById(R.id.textView)
        editTextMessage = findViewById(R.id.editTextMessage)
        btnSendMessage = findViewById(R.id.btnSendMessage)

        val playerName = "UsuarioAndroid2"
        cardGameClient = CardGameClient(textView, playerName)

        // Inicia la conexión y envía el nombre del jugador al servidor
        cardGameClient.execute()

        // Inicia el hilo para enviar mensajes continuamente
        //cardGameClient.startSendingMessages()

        btnSendMessage.setOnClickListener {
            val message = editTextMessage.text.toString()
            cardGameClient.sendMessage(message)
            editTextMessage.text.clear()
        }
    }

    fun handleServerResponse(response: String) {
        textView.text = response
    }
}


