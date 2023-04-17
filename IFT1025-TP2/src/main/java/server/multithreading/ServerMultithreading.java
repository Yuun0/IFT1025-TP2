package server.multithreading;

import javafx.util.Pair;
import server.EventHandler;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * La classe Server implémente l'interface Runnable. Elle définit les comportements d'un serveur. Elle permet
 * d'accepter les connexions des clients, de gérer les commandes envoyées par le client, de transmettre les données au
 * client et gérer les exceptions lors des opérations de lecture ou d'écriture.
 */
public class ServerMultithreading implements Runnable{
    /**
     * Commande d'inscription utilisée pour enregistrer un étudiant à un cours.
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";
    /**
     * Commande pour charger la liste des cours d'une session spécifique.
     */
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    /**
     * La méthode ServeurMultithreading est le constructeur de sa classe. Elle initialise le Serveur grâce au
     * ServerSocket passe en paramètres. Elle initialise ensuite la liste des événements et ajoute l'événement de
     * gestion des événements.
     * @param server ServerSocket utilisé pour écouter les connexions entrantes.
     * @throws IOException si une erreur survient lors de la création du du flux de sortie ou d'entrée.
     */
    public ServerMultithreading(ServerSocket server) {
        this.server = server;
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
     * La méthode addEventHandler ajoute un gestionnaire d'événement passé en paramètre à la liste des gestionnaires.
     * @param h le gestionnaire d'evenement a ajouter.
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    /**
     * La méthode alertHandlers alerte tous les gestionnaires d'événements avec la commande et l'argument spécifiés.
     * @param cmd la commande à transmettre.
     * @param arg l'argument de la commande.
     */
    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
     * La méthode run écoute en boucle les connexions entrantes et traite chaque demande reçue.
     */
    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * La méthode listen écoute les commandes envoyées par le client et les traite. Elle analyse la commande et appelle
     * gestionnaire d'événements appropriés.
     * @throws IOException si une erreur survient lors de la lecture des données envoyées par le client.
     * @throws ClassNotFoundException si la classe d'un objet sérialisé reçu n'est pas trouvée.
     */
    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    /**
     * La méthode processCommandeLine traite une ligne de commande et retourne ses différentes parties.
     * @param line la ligne de commande à traiter.
     * @return une paire de chaînes de caractères, représentant la commande et l'argument associé.
     */
    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    /**
     * La méthode disconnect ferme les flux d'entrée et de sortie du serveur. Elle stoppe alors sa connexion avec le
     * client.
     * @throws IOException si une erreur se produit lors de la fermeture des flux d'entrée et de sortie.
     */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    /**
     * La méthode handleEvent traite les évènements en fonction de la commande reçue. Elle appelle ainsi la méthode
     * adequate.
     * @param cmd la commande à recue.
     * @param arg l'argument de la commande.
     */
    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet
     dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg) {
        try {
            FileInputStream fileInputStream = new FileInputStream("./src/main/java/server/data/cours.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            ArrayList<Course> courses = new ArrayList<>();
            String line = bufferedReader.readLine();

            while(line != null) {
                String[] courseData = line.split("\t");
                if(courseData[2].equals(arg)) {
                    Course course = new Course(courseData[1], courseData[0], courseData[2]);
                    courses.add(course);
                }
                line = bufferedReader.readLine();
            }

            bufferedReader.close();
            fileInputStream.close();

            objectOutputStream.writeObject(courses);
            objectOutputStream.flush();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un
     fichier texte et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier
     ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            RegistrationForm registrationForm = (RegistrationForm) objectInputStream.readObject();
            FileOutputStream fileOutputStream = new FileOutputStream(
                    "./src/main/java/server/data/inscription.txt", true);
            BufferedWriter bufferedWritter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

            bufferedWritter.write(registrationForm.getCourse().getSession() + "\t");
            bufferedWritter.write(registrationForm.getCourse().getName() + "\t");
            bufferedWritter.write(registrationForm.getMatricule() + "\t");
            bufferedWritter.write(registrationForm.getPrenom() + "\t");
            bufferedWritter.write(registrationForm.getNom() + "\t");
            bufferedWritter.write(registrationForm.getEmail() + "\n");

            bufferedWritter.close();
            fileOutputStream.close();

        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
