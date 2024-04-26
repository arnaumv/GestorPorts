import java.io.IOException;

public class FirewallManager {
    private FirewallRuleDAO dao;

    public FirewallManager() {
        this.dao = new FirewallRuleDAO();
    }

    public void addRule(FirewallRule rule) {
        dao.addRule(rule);

                
        // Build the firewall command
        String command = String.format("iptables -A INPUT -p %s --dport %d -j ACCEPT", rule.getProtocol(), rule.getPort());

        // Execute the firewall command
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // other methods to delete, update and retrieve rules
}