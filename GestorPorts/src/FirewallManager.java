
import java.io.IOException;
import java.sql.SQLException;

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
        String command = String.format(
                "netsh advfirewall firewall add rule name=\"%s\" dir=in action=%s protocol=%s localport=%d",
                rule.getName(), netshAction, rule.getProtocol().toLowerCase(), rule.getPort());

        // Execute the firewall command
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error executing shell command: " + command);
            }
        } catch (IOException | InterruptedException e) {
            // Handle the exception appropriately for your application
            // For example, you could throw a new exception with a custom message
            throw new RuntimeException("Error adding firewall rule: " + rule.getName());
        }

        // Only add the rule to the database if the system firewall rule was
        // successfully created
        try {
            dao.addRule(rule);
        } catch (SQLException e) {
            // Handle the exception appropriately for your application
            // For example, you could throw a new exception with a custom message
            throw new RuntimeException("Error adding rule to database: " + rule.getName(), e);
        }
    }

    // Translate the action to an iptables action
    // String iptablesAction = rule.getAction().equals("Permetre") ? "ACCEPT" :
    // "DROP";

    // Build the firewall command
    // String command = String.format("iptables -A INPUT -p %s --dport %d -j %s",
    // rule.getProtocol(), rule.getPort(), iptablesAction);

    // Execute the firewall command
    // try {
    // ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c",
    // command);
    // Process process = processBuilder.start();
    // process.waitFor();
    // } catch (IOException | InterruptedException e) {
    // e.printStackTrace();
    // }

    // other methods to delete, update and retrieve rules
}