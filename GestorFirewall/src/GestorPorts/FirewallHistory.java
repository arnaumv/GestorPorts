package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

public class FirewallHistory extends JFrame {
    private FirewallManager manager;
    private JList<String> ruleList;
    private DefaultTableModel tableModelHistory;

    public FirewallHistory(FirewallManager manager) {
        this.manager = manager; // Inicializamos manager
        setTitle("Firewall History");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Obtener el historial de reglas del FirewallRuleDAO
        List<FirewallRule> rules = manager.getAllRules();
        for (FirewallRule rule : rules) {
            addRuleToTable(rule);
        }
    }
    
    private void addRuleToTable(FirewallRule rule) {
        tableModelHistory = new DefaultTableModel();

        // Añadimos la regla a la tabla
    	
        tableModelHistory.addRow(new Object[] {
                rule.getName(),
                rule.getPort(),
                rule.getProtocol(),
                rule.getApplication(),
                rule.getUser(),
                rule.getGroup(),
                rule.getIpAddress(),
                rule.getAction(),
                rule.getNetworkInterface(),
                rule.getDirection()
                
        });
        System.out.println(rule.getName());
    }
    
    public static void main(String[] args) {
        // Crear una instancia de FirewallManager
        FirewallManager manager = FirewallManager.getInstance();
        
        SwingUtilities.invokeLater(() -> {
            // Pasar la instancia de FirewallManager al constructor de FirewallHistory
            FirewallHistory firewallHistory = new FirewallHistory(manager);
            firewallHistory.setVisible(true);
        });
    }
}