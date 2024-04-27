package GestorPorts;

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

    public void addRule(FirewallRule rule) {
        dao.addRule(rule);

        // // Translate the action to an iptables action
        // String iptablesAction = rule.getAction().equals("Permetre") ? "ACCEPT" :
        // "DROP";

        // // Build the firewall command
        // String command = String.format("iptables -A INPUT -p %s --dport %d -j %s",
        // rule.getProtocol(), rule.getPort(),
        // iptablesAction);

        // // Execute the firewall command
        // try {
        // ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c",
        // command);
        // Process process = processBuilder.start();
        // process.waitFor();
        // } catch (IOException | InterruptedException e) {
        // e.printStackTrace();
        // }
    }

    // ejecutr comadnos en sistema winodws
    // public void addRule(FirewallRule rule) {
    // dao.addRule(rule);

    // // Translate the action to a netsh action
    // String netshAction = rule.getAction().equals("Permetre") ? "allow" : "block";

    // // Build the firewall command
    // String command = String.format(
    // "netsh advfirewall firewall add rule name=\"%s\" dir=in action=%s protocol=%s
    // localport=%d",
    // rule.getName(), netshAction, rule.getProtocol().toLowerCase(),
    // rule.getPort());

    // // Execute the firewall command
    // try {
    // ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
    // Process process = processBuilder.start();
    // process.waitFor();
    // } catch (IOException | InterruptedException e) {
    // e.printStackTrace();
    // }
    // }

    // other methods to delete, update and retrieve rules
}