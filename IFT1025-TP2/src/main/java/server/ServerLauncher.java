package server;

/**
 * La classe ServerLauncher lance un serveur.
 */
public class ServerLauncher {
    /**
     * Port utilisé pour lancer le serveur.
     */
    public final static int PORT = 1337;

    /**
     * La méthode main est le point d'entrée du programme. Elle lance un serveur à l'aide de la classe Server, sur le
     * port spécifié par la constante PORT. Le serveur est ensuite exécuté. La méthode est également responsable de la
     * gestion des exceptions qui pourraient survenir pendant l'exécution du serveur.
     * @param args les arguments en ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        Server server;
        try {
            server = new Server(PORT);
            System.out.println("Server is running...");
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}