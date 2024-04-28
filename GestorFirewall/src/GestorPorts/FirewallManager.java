package GestorPorts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

public class FirewallManager {
    private FirewallRuleDAO dao;

    public FirewallManager() {
        try {
            this.dao = FirewallRuleDAO.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately for your application
        }
    }

    public void addRule(FirewallRule rule) throws IllegalArgumentException {
        // Check if a rule with the same name already exists
        if (dao.getRule(rule.getName()) != null) {
            throw new IllegalArgumentException("Una regla con el mismo nombre ya existe.");
        }

        // Translate the action to a netsh action
        String netshAction = rule.getAction().equals("Permetre") ? "allow" : "block";

        // Build the firewall command
        StringBuilder command = new StringBuilder(String.format(
                "netsh advfirewall firewall add rule name=\"%s\" dir=%s action=%s protocol=%s localport=%d remoteip=%s",
                rule.getName(), rule.getDirection().toLowerCase(), netshAction, rule.getProtocol().toLowerCase(),
                rule.getPort(), rule.getIpAddress()));
        System.out.println("Executing command: " + command.toString());

        // If user, group, application or interface are specified, add them to the
        // command
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

        Process process = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command.toString());
            process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
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
            e.printStackTrace();
            System.out.println(e.getMessage()); // Agrega esta línea

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
            // Close the reader in the finally block to ensure it gets closed whether an
            // exception occurs or not
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Only add the rule to the database if the system firewall rule was
        // successfully created
        try {
            dao.addRule(rule);
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            // Handle the exception appropriately for your application
            // For example, you could throw a new exception with a custom message
            throw new RuntimeException("Error adding rule to database: " + rule.getName(), e);
        }
    }

    public List<FirewallRule> getAllRules() {
        return dao.getAllRules();
    }
    // public void addRule(FirewallRule rule) throws IllegalArgumentException {
    // // Check if a rule with the same name already exists
    // if (dao.getRule(rule.getName()) != null) {
    // throw new IllegalArgumentException("Una regla con el mismo nombre ya
    // existe.");
    // }

    // // Translate the action to an iptables action
    // String iptablesAction = rule.getAction().equals("Permetre") ? "ACCEPT" :
    // "DROP";

    // // Build the firewall command
    // StringBuilder command = new StringBuilder(String.format(
    // "iptables -A INPUT -p %s --dport %d -j %s",
    // rule.getProtocol(), rule.getPort(), iptablesAction));

    // // If user, group or interface are specified, add them to the command
    // if (rule.getUser() != null) {
    // command.append(" -m owner --uid-owner ").append(rule.getUser());
    // }
    // if (rule.getGroup() != null) {
    // command.append(" -m owner --gid-owner ").append(rule.getGroup());
    // }
    // if (rule.getNetworkInterface() != null) {
    // command.append(" -i ").append(rule.getNetworkInterface());
    // }

    // // Execute the firewall command
    // try {
    // ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c",
    // command.toString());
    // Process process = processBuilder.start();
    // process.waitFor();
    // } catch (IOException | InterruptedException e) {
    // e.printStackTrace();
    // }

}