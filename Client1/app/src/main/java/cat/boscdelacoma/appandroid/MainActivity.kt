package cat.boscdelacoma.appandroid

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var editTextMessage: EditText
    private lateinit var btnSendMessage: Button
    private lateinit var cardGameClient: CardGameClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        editTextMessage = findViewById(R.id.editTextMessage)
        btnSendMessage = findViewById(R.id.btnSendMessage)

        btnSendMessage.setOnClickListener {
            val message = editTextMessage.text.toString()

            // Verifica si la tarea ya está en curso
            if (cardGameClient == null || cardGameClient?.status != AsyncTask.Status.RUNNING) {
                // Crea una nueva instancia solo si no está en ejecución
                cardGameClient = CardGameClient(textView)
                cardGameClient?.execute(message)
            } else {
                // La tarea ya está en curso, puedes manejarlo de alguna manera (por ejemplo, mostrar un mensaje)
            }

            editTextMessage.text.clear()
        }
    }

    fun handleServerResponse(response: String) {
        // Actualiza todos los textViews necesarios
        // Puedes agregar lógica aquí para determinar a cuáles textViews actualizar
        // según tus necesidades
        textView.text = response
    }

}
