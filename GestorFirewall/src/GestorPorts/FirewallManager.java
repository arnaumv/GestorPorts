package GestorPorts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

public class FirewallManager {
    // DAO (Data Access Object) para interactuar con la base de datos de reglas de
    // firewall
    private FirewallRuleDAO dao;

    public FirewallManager() {
        try {
            // Intenta obtener una instancia del DAO
            this.dao = FirewallRuleDAO.getInstance();
        } catch (SQLException e) {
            // Si hay un error al obtener la instancia, imprime el error
            e.printStackTrace();
        }
    }

    // Método para obtener una regla de firewall por su nombre
    public FirewallRule getRule(String ruleName) {
        return dao.getRule(ruleName);
    }

    // Método para agregar una regla de firewall
    public void addRule(FirewallRule rule) throws IllegalArgumentException {
        // Verifica si ya existe una regla con el mismo nombre
        if (dao.getRule(rule.getName()) != null) {
            // Si existe, lanza una excepción
            throw new IllegalArgumentException("Una regla con el mismo nombre ya existe.");
        }

        // Traduce la acción a una acción de netsh
        String netshAction = rule.getAction().equals("Permetre") ? "allow" : "block";

        // Construye el comando de firewall
        StringBuilder command = new StringBuilder(String.format(
                "netsh advfirewall firewall add rule name=\"%s\" dir=%s action=%s protocol=%s localport=%d",
                rule.getName(), rule.getDirection().toLowerCase(), netshAction, rule.getProtocol().toLowerCase(),
                rule.getPort()));

        // Agrega la IP remota al comando solo si se especifica
        if (rule.getIpAddress() != null && !rule.getIpAddress().isEmpty()) {
            command.append(" remoteip=").append(rule.getIpAddress());
        }

        System.out.println("Executing command: " + command.toString());

        // Si se especifican usuario, grupo, aplicación o interfaz, los agrega al
        // comando
        if (rule.getApplication() != null) {
            command.append(" program=").append(rule.getApplication());
        }
        if (rule.getUser() != null) {
            command.append(" user=").append(rule.getUser());
        }
        if (rule.getGroup() != null) {
            command.append(" group=").append(rule.getGroup());
        }
        if (rule.getNetworkInterface() != null) {
            command.append(" interface=").append(rule.getNetworkInterface());
        }

        // Ejecuta el comando en el sistema operativo
        Process process = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command.toString());
            process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Si el comando falla, lanza una excepción
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                StringBuilder errorMessage = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    errorMessage.append(line);
                }
                throw new IOException(
                        "Error executing shell command: " + command + ". Error: " + errorMessage.toString());
            }
        } catch (IOException | InterruptedException e) {
            // Si hay un error al ejecutar el comando, imprime el error
            e.printStackTrace();
            System.out.println(e.getMessage());

            if (process != null) {
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                StringBuilder errorMessage = new StringBuilder();
                try {
                    while ((line = reader.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    System.out.println("Error details: " + errorMessage.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error adding firewall rule: " + rule.getName(), e);
        } finally {
            // Cierra el lector en el bloque finally para asegurarse de que se cierre,
            // ocurra una excepción o no
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Solo agrega la regla a la base de datos si la regla del firewall del sistema
        // se creó con éxito
        try {
            dao.addRule(rule);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error adding rule to database: " + rule.getName(), e);
        }

    }

    public void updateRule(String originalName, FirewallRule rule) throws IllegalArgumentException {
        // Verifica si existe una regla con el nombre original
        FirewallRule existingRule = dao.getRule(originalName);
        if (existingRule == null) {
            // Si no existe, lanza una excepción
            throw new IllegalArgumentException("No existe una regla con este nombre.");
        }

        // Verifica si la regla es un duplicado
        for (FirewallRule existing : dao.getAllRules()) {
            if (!existing.getName().equals(originalName) && existing.equals(rule)) {
                // Si es un duplicado, lanza una excepción
                throw new IllegalArgumentException("La regla ya existe.");
            }
        }

        // Traduce la acción a una acción de netsh
        String netshAction = rule.getAction().equals("Permetre") ? "allow" : "block";

        // Construye el comando de firewall para actualizar la regla
        StringBuilder command = new StringBuilder(String.format(
                "netsh advfirewall firewall set rule name=\"%s\" new dir=%s action=%s protocol=%s localport=%d",
                originalName, rule.getDirection().toLowerCase(), netshAction, rule.getProtocol().toLowerCase(),
                rule.getPort()));

        // Solo agrega la parte 'remoteip' si la dirección IP no está vacía
        if (rule.getIpAddress() != null && !rule.getIpAddress().isEmpty()) {
            command.append(String.format(" remoteip=%s", rule.getIpAddress()));
        }

        System.out.println("Executing command: " + command.toString());

        // Si se especifican usuario, grupo, aplicación o interfaz, los agrega al
        // comando
        if (rule.getApplication() != null) {
            command.append(" program=").append(rule.getApplication());
        }
        if (rule.getUser() != null) {
            command.append(" user=").append(rule.getUser());
        }
        if (rule.getGroup() != null) {
            command.append(" group=").append(rule.getGroup());
        }
        if (rule.getNetworkInterface() != null) {
            command.append(" interface=").append(rule.getNetworkInterface());
        }

        // Ejecuta el comando para actualizar la regla
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command.toString());
            process = processBuilder.start();
            process.waitFor(); // Espera a que se complete el comando
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error executing command: " + command, e);
        }

        // Construye el comando de firewall para renombrar la regla
        command = new StringBuilder(String.format(
                "netsh advfirewall firewall set rule name=\"%s\" new name=\"%s\"",
                originalName, rule.getName()));

        // Ejecuta el comando para renombrar la regla
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command.toString());
            process = processBuilder.start();
            process.waitFor(); // Espera a que se complete el comando
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error executing command: " + command, e);
        }

        // Agrega el código para actualizar la regla en la base de datos
        try {
            dao.updateRule(originalName, rule);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error updating rule in database: " + rule.getName(), e);
        }
    }

    public void deleteRule(String ruleName) throws SQLException {
        Process process = null;
        BufferedReader reader = null;
        String line = null;

        try {
            // Delete rule from database
            FirewallRuleDAO dao = FirewallRuleDAO.getInstance(); // Get instance from singleton or dependency injection
            dao.deleteRule(ruleName);

            // Build command to delete firewall rule
            String[] command = { "cmd.exe", "/c", "netsh advfirewall firewall delete rule name=\"" + ruleName + "\"" };

            // Execute command
            process = Runtime.getRuntime().exec(command, null, null);

            // Read command output
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for command to finish
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            // If there's an error executing the command, print the error
            e.printStackTrace();
            System.out.println(e.getMessage());

            if (process != null) {
                try {
                    reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder errorMessage = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    System.out.println("Error details: " + errorMessage.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error deleting firewall rule: " + ruleName, e);
        } finally {
            // Close the reader in the finally block to make sure it gets closed,
            // whether an exception occurs or not
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<FirewallRule> getAllRules() {
        // Devuelve todas las reglas de la base de datos
        return dao.getAllRules();
    }

}