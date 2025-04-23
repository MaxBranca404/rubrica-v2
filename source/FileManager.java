import java.io.*;
import java.util.*;

public class FileManager {
    private static final String CONTACTS_DIR = "contacts";
    private static final String INDEX_FILE = "index.txt";

    static {
        File dir = new File(CONTACTS_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static Vector<Persona> caricaPersone() {
        Vector<Persona> persone = new Vector<>();
        File indexFile = new File(INDEX_FILE);

        try {
            if (indexFile.exists()) {
                try (Scanner scanner = new Scanner(indexFile)) {
                    while (scanner.hasNextLine()) {
                        String filename = scanner.nextLine().trim();
                        if (!filename.isEmpty()) {
                            Persona persona = loadPersonaFromFile(filename);
                            if (persona != null) {
                                persone.add(persona);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la lettura dell'indice: " + e.getMessage());
        }

        return persone;
    }

    private static Persona loadPersonaFromFile(String filename) {
        File file = new File(CONTACTS_DIR + File.separator + filename);
        if (!file.exists()) {
            return null;
        }

        try (Scanner scanner = new Scanner(file)) {
            String nome = "", cognome = "", indirizzo = "", telefono = "";
            int eta = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":", 2);

                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    switch (key.toLowerCase()) {
                        case "nome":
                            nome = value;
                            break;
                        case "cognome":
                            cognome = value;
                            break;
                        case "indirizzo":
                            indirizzo = value;
                            break;
                        case "telefono":
                            telefono = value;
                            break;
                        case "eta":
                            try {
                                eta = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                System.err.println("Formato età non valido: " + value);
                            }
                            break;
                    }
                }
            }

            return new Persona(nome, cognome, indirizzo, telefono, eta);
        } catch (FileNotFoundException e) {
            System.err.println("File non trovato: " + e.getMessage());
            return null;
        }
    }

    public static void salvaPersone(Vector<Persona> persone) {
        try {
            PrintStream indexPs = new PrintStream(new FileOutputStream(INDEX_FILE));

            Set<String> existingFiles = getAllContactFiles();
            Set<String> updatedFiles = new HashSet<>();

            for (Persona persona : persone) {
                String filename = getFilenameForPersona(persona);
                savePersonaToFile(persona, filename);
                indexPs.println(filename);
                updatedFiles.add(filename);
            }

            indexPs.close();

            cleanupUnusedFiles(existingFiles, updatedFiles);

        } catch (FileNotFoundException e) {
            System.err.println("Impossibile scrivere sull'indice: " + e.getMessage());
        }
    }

    public static void deletePersona(Persona persona) {
        String filename = findFilenameForPersona(persona);
        if (filename != null) {
            File file = new File(CONTACTS_DIR + File.separator + filename);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Impossibile eliminare il file: " + filename);
                }
            }
        }
    }

    private static String findFilenameForPersona(Persona persona) {
        File dir = new File(CONTACTS_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith(persona.getNome() + "-" + persona.getCognome()));

        if (files != null) {
            for (File file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    String nome = "", cognome = "";

                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] parts = line.split(":", 2);

                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();

                            if ("nome".equalsIgnoreCase(key)) {
                                nome = value;
                            } else if ("cognome".equalsIgnoreCase(key)) {
                                cognome = value;
                            }

                            if (!nome.isEmpty() && !cognome.isEmpty()) {
                                if (nome.equals(persona.getNome()) && cognome.equals(persona.getCognome())) {
                                    return file.getName();
                                }
                                break;
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("File non trovato: " + e.getMessage());
                }
            }
        }

        return null;
    }

    private static Set<String> getAllContactFiles() {
        Set<String> files = new HashSet<>();
        File dir = new File(CONTACTS_DIR);
        File[] contactFiles = dir.listFiles((d, name) -> name.endsWith(".txt"));

        if (contactFiles != null) {
            for (File file : contactFiles) {
                files.add(file.getName());
            }
        }

        return files;
    }

    private static void cleanupUnusedFiles(Set<String> existingFiles, Set<String> updatedFiles) {
        existingFiles.removeAll(updatedFiles);

        for (String filename : existingFiles) {
            File file = new File(CONTACTS_DIR + File.separator + filename);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Impossibile eliminare il file non utilizzato: " + filename);
                }
            }
        }
    }

    private static void savePersonaToFile(Persona persona, String filename) {
        File file = new File(CONTACTS_DIR + File.separator + filename);

        try (PrintStream ps = new PrintStream(new FileOutputStream(file))) {
            ps.println("Nome: " + persona.getNome());
            ps.println("Cognome: " + persona.getCognome());
            ps.println("Indirizzo: " + persona.getIndirizzo());
            ps.println("Telefono: " + persona.getTelefono());
            ps.println("Eta: " + persona.getEta());
        } catch (FileNotFoundException e) {
            System.err.println("Impossibile scrivere il file contatto: " + e.getMessage());
        }
    }

    private static String getFilenameForPersona(Persona persona) {
        String baseFilename = persona.getNome() + "-" + persona.getCognome() + ".txt";
        String filename = baseFilename;
        File file = new File(CONTACTS_DIR + File.separator + filename);

        int counter = 1;
        while (file.exists() && !isFileForPersona(file, persona)) {
            filename = persona.getNome() + "-" + persona.getCognome() + "-" + counter + ".txt";
            file = new File(CONTACTS_DIR + File.separator + filename);
            counter++;
        }

        return filename;
    }

    private static boolean isFileForPersona(File file, Persona persona) {
        try (Scanner scanner = new Scanner(file)) {
            String nome = "", cognome = "";

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":", 2);

                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if ("nome".equalsIgnoreCase(key)) {
                        nome = value;
                    } else if ("cognome".equalsIgnoreCase(key)) {
                        cognome = value;
                    }

                    if (!nome.isEmpty() && !cognome.isEmpty()) {
                        return nome.equals(persona.getNome()) && cognome.equals(persona.getCognome());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }

        return false;
    }

}
