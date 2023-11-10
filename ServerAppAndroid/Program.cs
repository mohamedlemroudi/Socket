using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

class Program
{
    static List<TcpClient> clients = new List<TcpClient>();

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
        {
            string playerName = await reader.ReadLineAsync();
            Console.WriteLine($"Jugador conectado: {playerName}");

            try
            {
                while (true)
                {
                    string message = await reader.ReadLineAsync();
                    if (message == null)
                    {
                        Console.WriteLine($"Jugador {playerName} se desconectó.");
                        break;
                    }

                    Console.WriteLine($"Mensaje de {playerName}: {message}");

                    // Envía el mensaje a todos los clientes, incluido el remitente
                    BroadcastMessage($"{playerName}: {message}\n", client);
                }
            }
            catch (IOException)
            {
                Console.WriteLine($"Error al leer el mensaje del jugador {playerName}. Posiblemente desconectado.");
                HandleDisconnect(client, playerName);
            }
        }
    }


    static void HandleDisconnect(TcpClient disconnectedClient, string playerName)
    {
        clients.Remove(disconnectedClient);
        Console.WriteLine($"Jugador {playerName} se desconectó inesperadamente.");
    }

    static void BroadcastMessage(string message, TcpClient sender)
    {
        foreach (var client in clients)
        {
            if (client != sender)
            {
                try
                {
                    using (var writer = new StreamWriter(client.GetStream()) { AutoFlush = true })
                    {
                        writer.WriteLine(message);
                    }
                }
                catch (IOException)
                {
                    Console.WriteLine("Error al enviar mensaje a un cliente. Posiblemente desconectado.");
                }
            }
        }
    }
}
