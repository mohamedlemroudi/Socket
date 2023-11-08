package cat.boscdelacoma.appandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Socket
        // Puedes iniciar la comunicación con el servidor aquí o en otra parte de tu actividad
        iniciarComunicacionConServidor()
    }

    private fun iniciarComunicacionConServidor() {
        // Crear una instancia de CardGameClient y ejecutarla como tarea asíncrona
        val cardGameClient = CardGameClient()
        cardGameClient?.execute()
    }
}