
using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

class CardGameServer
{
    static Dictionary<string, TcpClient> clients = new Dictionary<string, TcpClient>();

    static async Task Main(string[] args)
    {
        TcpListener server = null;

        try
        {
            int port = 12345; // Puerto para la comunicación

            server = new TcpListener(IPAddress.Any, port);
            server.Start();

            Console.WriteLine($"Servidor iniciado en el puerto {port}");

            while (true)
            {
                Console.WriteLine("Esperando un cliente...");
                TcpClient client = await server.AcceptTcpClientAsync();
                Console.WriteLine("Cliente conectado!");

                // Recibir el nombre del cliente
                NetworkStream nameStream = client.GetStream();
                byte[] nameBuffer = new byte[1024];
                int nameBytesRead = await nameStream.ReadAsync(nameBuffer, 0, nameBuffer.Length);
                string playerName = Encoding.ASCII.GetString(nameBuffer, 0, nameBytesRead);

                // Agregar al jugador a la lista
                clients.Add(playerName, client);

                // Iniciar un nuevo hilo para manejar la lógica del juego para este cliente
                Task.Run(() => HandleClientGame(client, playerName));
            }
        }
        catch (Exception e)
        {
            Console.WriteLine($"Error: {e}");
        }
        finally
        {
            server?.Stop();
        }
    }

    static async void HandleClientGame(TcpClient client, string playerName)
    {
        NetworkStream stream = client.GetStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = await stream.ReadAsync(buffer, 0, buffer.Length)) > 0)
        {
            string cartaAdivinada = Encoding.ASCII.GetString(buffer, 0, bytesRead);
            Console.WriteLine($"Jugador {playerName} adivinó: {cartaAdivinada}");

            // Aquí puedes implementar la lógica del juego, verificar respuestas, etc.

            // Enviar la respuesta a un jugador específico (reemplaza "recipientPlayerName" con el nombre del jugador destinatario)
            if (clients.ContainsKey("mohamed"))
            {
                TcpClient recipientClient = clients["mohamed"];
                NetworkStream recipientStream = recipientClient.GetStream();
                byte[] responseBuffer = Encoding.ASCII.GetBytes($"El jugador {playerName} dijo: {cartaAdivinada}");
                await recipientStream.WriteAsync(responseBuffer, 0, responseBuffer.Length);
            }
        }

        // Eliminar al jugador de la lista cuando se desconecta
        clients.Remove(playerName);
        client.Close();
    }
}
