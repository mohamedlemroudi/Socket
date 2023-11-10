package cat.boscdelacoma.appandroid

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        editTextMessage = findViewById(R.id.editTextMessage)
        btnSendMessage = findViewById(R.id.btnSendMessage)


        btnSendMessage.setOnClickListener {
            val message = editTextMessage.text.toString()

            // Crea una nueva instancia de CardGameClient y ejecútala
            val newCardGameClient = CardGameClient(textView)
            newCardGameClient.execute(message)

            editTextMessage.text.clear()
        }

    }

    fun handleServerResponse(response: String) {
        textView.text = response
    }
}