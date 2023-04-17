package client;

import server.models.Course;
import server.models.RegistrationForm;

import java.net.Socket;
import java.util.ArrayList;
import java.io.*;

/**
 * La classe Client définit un client qui peut se connecter a un serveur pour effectuer des actions liés à la recherche
 * d'un cours et à l'inscription à un cours.
 */
public class Client {
    /**
     * Commande d'inscription utilisée pour enregistrer un étudiant à un cours.
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";
    /**
     * Commande pour charger la liste des cours d'une session spécifique.
     */
    public final static String LOAD_COMMAND = "CHARGER";
    private final String IP;
    private final int PORT;
    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    /**
     * La méthode Client et le constructeur de sa classe. Il initialise l'adresse IP et le port qui permettent de se
     * connecter au serveur.
     * @param IP l'adresse du serveur.
     * @param PORT le port du serveur.
     */
    public Client(String IP, int PORT) {
        this.IP = IP;
        this.PORT = PORT;
    }

    /**
     * La méthode connect permet de se connecter au serveur et d'initialiser des flux d'entrée et de sortie avec lui.
     */
    public void connect() {
        try {
            this.clientSocket = new Socket(IP, PORT);
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Un problème est survenu lors de la connexion avec le serveur.");
        }
    }

    /**
     * La méthode disconnect ferme les flux d'entrée et de sortie avec le serveur. Elle stoppe alors la connexion avec
     * le serveur.
     */
    public void disconnect() {
        try {
            objectInputStream.close();
            objectOutputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Un problème est survenu lors de la déconnexion avec le serveur.");
        }
    }

    /**
     * La méthode loadCourses permet de charger les cours d'un session donnée, grâce à un appelle au serveur.
     * @param session la session dont on veut charger les cours.
     * @return une liste de cours correspondant à la session donnée.
     */
    public ArrayList<Course> loadCourses(String session) {
        try {
            String request = LOAD_COMMAND + " " + session;
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            ArrayList<Course> courses = (ArrayList<Course>) objectInputStream.readObject();
            return courses;
        } catch (Exception e) {
            System.out.println("Les cours de la session " + session + " n'ont pas pu être affichés.");
        }
        return null;
    }

    /**
     * La méthode registrationForm permet d'envoyer au serveur le formulaire d'inscription à un cours.
     * @param registrationForm le formulaire d'inscription à envoyer.
     */
    public void registration(RegistrationForm registrationForm) {
        try {
            connect();
            String request = REGISTER_COMMAND;
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.writeObject(registrationForm);
            objectOutputStream.flush();
        } catch (Exception e) {
            System.out.println("Un problème est survenu lors de l'enregistrement du formulaire.");
        }
    }

    /**
     * La méthode findCourse permet de trouver un cours à partir de son code.
     * @param courseCode le code du cours recherche.
     * @return le cours correspondant au code donne.
     */
    public Course findCourse(String courseCode) {
        String[] sessions = {"Automne", "Hiver", "Ete"};
        try {
            for (int i = 0; i < sessions.length; i++) {
                connect();
                String request = LOAD_COMMAND + " " + sessions[i];
                objectOutputStream.writeObject(request);
                objectOutputStream.flush();
                ArrayList<Course> courses = (ArrayList<Course>) objectInputStream.readObject();
                for (int j = 0; j < courses.size(); j++) {
                    if (courses.get(j).getCode().equals(courseCode)) return courses.get(j);
                }
            }
        } catch (Exception e) {
            System.out.println("Un problème est survenu lors de la recherche du cours demandé.");
        }
        return null;
    }

    /**
     * La méthode verifyMatricule verifie si la chaine de caractère passée en parametre correspond a un matricule
     * valide de 8 chiffres.
     * @param matricule le matricule à vérifier.
     * @return true si le matricule est valide, false sinon.
     */
    public boolean verifyMatricule(String matricule) {
        if(matricule.length() == 8 && isNumeric(matricule)) return true;
        else return false;
    }

    /**
     * La méthode isNumeric vérifie si la chaîne de caractère passe en paramètre représente un nombre entier.
     * @param str la chaîne de caractère à vérifier.
     * @return true si la chaîne de caractère représente un entier, false sinon.
     */
    public boolean isNumeric(String str) {
        if (str == null) {return false;}
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
