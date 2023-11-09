using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

class Program
{
    static ConcurrentBag<TcpClient> clients = new ConcurrentBag<TcpClient>();

    static async Task Main()
    {
        TcpListener server = new TcpListener(IPAddress.Any, 12345);
        server.Start();

        Console.WriteLine("Servidor iniciado, esperando conexiones de clientes...");

        while (true)
        {
            TcpClient client = await server.AcceptTcpClientAsync();
            clients.Add(client);

            _ = Task.Run(() => HandleClientAsync(client));
        }
    }

    static async Task HandleClientAsync(TcpClient client)
    {
        using (var stream = client.GetStream())
        using (var reader = new StreamReader(stream))
        using (var writer = new StreamWriter(stream) { AutoFlush = true })
        {
            string playerName = await reader.ReadLineAsync();
            Console.WriteLine($"Jugador conectado: {playerName}");

            try
            {
                while (true)
                {
                    string respuesta = await ReadMessageAsync(reader);
                    if (respuesta == null)
                    {
                        Console.WriteLine($"Jugador {playerName} se desconectó.");
                        break;
                    }

                    Console.WriteLine($"Respuesta de {playerName}: {respuesta}");

                    // Envía la respuesta a todos los clientes
                    BroadcastMessage($"{playerName}: {respuesta}\n");
                }
            }
            catch (IOException)
            {
                Console.WriteLine($"Error al leer el mensaje del jugador {playerName}. Posiblemente desconectado.");
                // Manejar la desconexión sin cerrar la conexión por completo
                HandleDisconnect(client, playerName);
            }
            finally
            {
                client.Close(); // Cerrar la conexión cuando termina la tarea
            }
        }
    }

    static void HandleDisconnect(TcpClient disconnectedClient, string playerName)
    {
        clients.TryTake(out disconnectedClient);
        Console.WriteLine($"Jugador {playerName} se desconectó inesperadamente.");
    }

    static void BroadcastMessage(string message)
    {
        foreach (var otherClient in clients)
        {
            try
            {
                using (var otherWriter = new StreamWriter(otherClient.GetStream()) { AutoFlush = true })
                {
                    otherWriter.WriteLine(message);
                }
            }
            catch (IOException)
            {
                // Handle exceptions related to disconnected clients
                Console.WriteLine("Error al enviar mensaje a un cliente. Posiblemente desconectado.");
            }
        }
    }

    static async Task<string> ReadMessageAsync(StreamReader reader)
    {
        StringBuilder message = new StringBuilder();
        char[] buffer = new char[1];

        while (await reader.ReadAsync(buffer, 0, 1) > 0)
        {
            char currentChar = buffer[0];
            if (currentChar == '\n') // Carácter especial que indica el final del mensaje
            {
                break;
            }

            message.Append(currentChar);
        }

        if (message.Length == 0)
        {
            // El cliente se desconectó
            return null;
        }

        return message.ToString();
    }
}
