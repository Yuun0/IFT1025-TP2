package server.multithreading;

import server.multithreading.ServerMultithreading;
import java.net.ServerSocket;

/**
 * La classe ServerLauncher lance un serveur en mode multithread.
 */
public class ServerLauncherMultithreading {
    /**
     * Port utilisé pour lancer le serveur.
     */
    public final static int PORT = 1337;

    /**
     * La méthode main est le point d'entrée du programme. Elle  crée un socket serveur pour accepter les connexions
     * entrantes des clients, puis crée un thread pour chaque client connecté. La méthode est également responsable de
     * la gestion des exceptions qui pourraient survenir pendant l'exécution du serveur.
     * @param args les arguments en ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        ServerSocket server;
        ServerMultithreading clientSock;
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server is running...");
            while (true) {
                clientSock = new ServerMultithreading(server);
                new Thread(clientSock).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
