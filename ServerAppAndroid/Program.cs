using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Runtime.ConstrainedExecution;
using System.Text;
using System.Threading.Tasks;

class Program
{
    static List<TcpClient> clients = new List<TcpClient>();
    static SemaphoreSlim semaphore = new SemaphoreSlim(1, 1); // Limita a un solo hilo a la vez

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
                char[] buffer = new char[4096];
                StringBuilder messageBuilder = new StringBuilder();

                while (client.Connected)
                {
                    int bytesRead = await reader.ReadAsync(buffer, 0, buffer.Length);

                    if (bytesRead > 0)
                    {
                        // Construir el mensaje acumulando los datos leídos
                        messageBuilder.Append(buffer, 0, bytesRead);

                        // Verificar si el mensaje está completo
                        string message = messageBuilder.ToString();
                        if (message.Contains("\n"))
                        {
                            Console.WriteLine($"Mensaje de {playerName}: {message}");

                            // Envía el mensaje a todos los clientes, incluido el remitente
                            await BroadcastMessageAsync($"{playerName}: {message}\n");

                            // Limpiar el StringBuilder para el próximo mensaje
                            messageBuilder.Clear();
                        }
                    }
                    else
                    {
                        // El cliente se desconectó
                        HandleDisconnect(client, playerName);
                        return;  // Salir del método para evitar continuar procesando después de la desconexión
                    }
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

    static async Task BroadcastMessageAsync(string message)
    {
        await semaphore.WaitAsync(); // Espera a adquirir el semáforo
        try
        {
            Console.WriteLine($"Enviando mensaje: {message}");

            foreach (var client in clients)
            {
                try
                {
                    using (var writer = new StreamWriter(client.GetStream()) { AutoFlush = true })
                    {
                        await writer.WriteLineAsync(message);
                        Console.WriteLine($"Mensaje enviado a {client}");
                    }
                }
                catch (IOException)
                {
                    Console.WriteLine($"Error al enviar mensaje a un cliente {client}. Posiblemente desconectado.");
                }
            }
        }
        finally
        {
            semaphore.Release(); // Libera el semáforo, permitiendo que otro hilo lo adquiera
        }
    }
}

