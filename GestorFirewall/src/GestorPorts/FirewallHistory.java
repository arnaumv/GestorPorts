package GestorPorts;
	
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

public class FirewallHistory extends JFrame {
    private FirewallManager manager;
    private JList<String> ruleList;
    private DefaultTableModel tableModelHistory;
    private JTable table;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JButton recoveryRule, btnBack;
    
    public FirewallHistory(FirewallManager manager) {
        this.manager = manager; // Inicializamos manager

        setTitle("Firewall History");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(900, 300);
        setLocationRelativeTo(null);

        

        // Crear un panel
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Añadir márgenes de 20px
        add(panel);


        // Crear un JLabel para el título "Historial de reglas"
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Margen inferior de 10px


        JLabel titleLabel = new JLabel("Històric de regles:");
        northPanel.add(titleLabel, BorderLayout.WEST);

        // Crear un JButton para "Reglas activas"
        btnBack = new JButton("Regles actives");
        btnBack.addActionListener(e -> {
            // Cerrar la ventana actual
            panel.setVisible(false);
        });
        northPanel.add(btnBack, BorderLayout.EAST);

        panel.add(northPanel, BorderLayout.NORTH);
        // Crear un JList para mostrar las reglas del Firewall
        ruleList = new JList<>();
        panel.add(ruleList, BorderLayout.WEST);


        // Crear un JScrollPane para mostrar las reglas del Firewall
        scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);
        
        recoveryRule = new JButton("Recuperar regla");
        recoveryRule.addActionListener(e -> {
            // Obtener el índice seleccionado en la lista
            int selectedIndex = table.getSelectedRow();
            
            System.out.println(selectedIndex);
            if (selectedIndex != -1) {

                
                // Obtener el nombre de la regla seleccionada
                String ruleName = (String) tableModelHistory.getValueAt(selectedIndex, 0);
                System.out.println(ruleName);
                manager.recoverRule(ruleName);

                // verificar  que la data de finalització de la regla estigui buida
                if (tableModelHistory.getValueAt(selectedIndex, 11).equals("")) {
                    JOptionPane.showMessageDialog(this, "La regla ja està activa", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    // Añadir la regla recuperada a la base de datos
                    FirewallRule rule = manager.getHistoryRule(ruleName);
                    manager.addHistoryRuleToActiveRules(rule);

                    // Modificar la tabla para que cambie la fecha de fin de la regla a "Regla activa"
                    tableModelHistory.setValueAt("", selectedIndex, 11);
                    
                    JOptionPane.showMessageDialog(this, "Regla recuperada amb éxit", "Éxit", JOptionPane.INFORMATION_MESSAGE);
                }


            }
        });
        panel.add(recoveryRule, BorderLayout.SOUTH);


        // Crear un JTable para mostrar las reglas del Firewall
        table = new JTable();
        scrollPane.setViewportView(table);
        
        recoveryRule.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = table.getSelectedRow();
                recoveryRule.setEnabled(selectedIndex != -1);
            }
        });
        
        // Crear un DefaultTableModel para la tabla
        tableModelHistory = new DefaultTableModel(new Object[] { "Nom", "Port", "Protocol", "App", "Usuari", "Grup",
                "IP", "Accio", "Interficie", "Sentit", "Data inici", "Data final" }, 0);

        // Añadir el DefaultTableModel a la tabla
        table.setModel(tableModelHistory);
        table.setDefaultEditor(Object.class, null);

        // Obtener el historial de reglas del FirewallRuleDAO
        List<FirewallHistoryRule> rules = manager.getHistoryRules();
        for (FirewallHistoryRule rule : rules) {
            addRuleToTable(rule);
        }

    }

    private void addRuleToTable(FirewallHistoryRule rule) {  	
        String endDate = rule.getEndDate();
        if (endDate == null) {
            endDate = "";
        }
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
                rule.getDirection(),
                rule.getCreatedDate(),
                endDate
        });


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