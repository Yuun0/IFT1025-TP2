package client;

import server.models.Course;
import server.models.RegistrationForm;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * La classe ClientCommand est une interface de commande qui permet à un utilisateur de consulter les cours d'une
 * session et de s'inscrire à un cours.
 */
public class ClientCommand {
    /**
     * L'adresse IP du serveur d'inscription (ici le server local).
     */
    private static final String IP = "127.0.0.1";
    /**
     Le port utilisé pour la connexion au serveur d'inscription.
     */
    private static final int PORT = 1337;
    private static Client client;
    /**
     La liste des différentes sessions proposées.
     */
    private static String[] sessions = {"Automne", "Hiver", "Ete"};
    private static Scanner scanner = new Scanner(System.in);

    /**
     * La méthode main est le point d'entrée du programme. Elle permet de gérer l'ensemble du processus d'inscription,
     * en demandant à l'utilisateur si il veut consulter les cours ou s'inscrire à un cours. Elle appelle ensuite la
     * méthode adéquate.
     * @param args les arguments en ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        try {
            client = new Client(IP, PORT);
            boolean next = false;
            System.out.println("*** Bienvenue au portail d’inscription de cours de l’UDEM ***");

            while (!next) {
                client.connect();
                commandLoadCourses();

                System.out.println("1. Consulter les cours offerts pour une autre session");
                System.out.println("2. Inscription à un cours");
                System.out.print("> Choix : ");
                int choice = scanner.nextInt(); scanner.nextLine();

                if(choice == 2) {next = true;}
            }
            commandRegistration();

            scanner.close();
            client.disconnect();
        } catch (Exception e) {
            System.out.println("Un problème est survenu lors de l'exécution du client de ligne de commande.");
        }
    }

    /**
     * La méthode commandLoadCourses() permet de consulter les cours offerts pour une session donnée. Elle demande à
     * l'utilisateur de saisir son choix de session, puis affiche la liste des cours offerts pour une session donnée.
     * Si aucun cours n'est trouvé pour la session choisie, un message est affiché à l'utilisateur.
     */
    public static void commandLoadCourses() {
        System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours :");

        for(int i=0; i < sessions.length; i++) {
            System.out.println(i+1 + ". " + sessions[i]);
        }

        System.out.print("> Choix : ");
        int sessionChoice = scanner.nextInt(); scanner.nextLine();
        ArrayList<Course> courses = (ArrayList<Course>) client.loadCourses(sessions[sessionChoice-1]);

        if(courses.isEmpty()) {
            System.out.println("Aucun cours n'a été trouvé pour la session spécifiée.");
        } else {
            System.out.println("Les cours offerts pendant la session " + sessions[sessionChoice-1] + " sont :");
            for(Course course : courses) {
                System.out.println("- " + course.getCode());
            }
        }
    }

    /**
     * La méthode commandRegistration permet de gérer le processus d'inscription à un cours. Elle demande à
     * l'utilisateur d'entrer les informations nécessaires et vérifie que le matricule et le cours sont conformes. Si
     * tout est valide elle envoie un formulaire d'inscription au serveur et affiche à l'utilisateur un message de
     * confirmation.
     */
    public static void commandRegistration() {
        String matricule = "";
        String courseCode = "";
        Course course = null;

        System.out.print("> Veuillez saisir votre prénom : ");
        String firstName = scanner.nextLine();
        System.out.print("> Veuillez saisir votre nom : ");
        String lastName = scanner.nextLine();
        System.out.print("> Veuillez saisir votre email : ");
        String email = scanner.nextLine();
        while (true) {
                System.out.print("> Veuillez saisir votre matricule : ");
                matricule = scanner.nextLine();
            try {
                if(!client.verifyMatricule(matricule)) throw new InputMismatchException();
                break;
            } catch (InputMismatchException e) {
                System.out.println("Le matricule que vous avez entré n'est pas valide. " +
                        "Un matricule valide est constitué de 8 chiffres.");
            }
        }
        while(true) {
                System.out.print("> Veuillez saisir le code du cours : ");
                courseCode = scanner.nextLine();
                course = client.findCourse(courseCode);
            try {
                if(course == null) throw new InputMismatchException();
                break;
            } catch(InputMismatchException e) {
                System.out.println("Le code que vous avez entree n'existe pas.");
            }
        }

        RegistrationForm registrationForm = new RegistrationForm(firstName, lastName, email, matricule, course);
        client.registration(registrationForm);
        System.out.println("Félicitations ! Inscription réussie de " + firstName + " " + lastName + " au cours " +
                courseCode + ".");
    }
}