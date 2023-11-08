package cat.boscdelacoma.appandroid

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class CardGameClient : AsyncTask<String, Void, Void>() {
    override fun doInBackground(vararg params: String?): Void? {
        try {
            val serverIP = "192.168.18.131" // Reemplaza con la dirección IP del servidor
            val serverPort = 12345

            val socket = Socket(serverIP, serverPort)

            val playerName = "mohamed" // Recibe el nombre del jugador como parámetro
            val cardToGuess = "A" // Recibe la carta a adivinar como parámetro

            // Enviar el nombre del jugador al servidor
            val out = PrintWriter(socket.getOutputStream(), true)
            out.println(playerName)

            // Enviar la carta a adivinar al servidor
            out.println(cardToGuess)

            // Comienza a escuchar al servidor
            val `in` = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (true) {
                val respuesta = `in`.readLine()
                if (respuesta != null) {
                    // Procesa la respuesta del servidor, como mostrarla en la interfaz de usuario
                    println(respuesta)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
