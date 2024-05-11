package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

public class FirewallHistory extends JFrame {
    private FirewallManager manager;
    private FirewallGUI gui;
    private JList<String> ruleList;
    private DefaultTableModel tableModelHistory;
    private JTable table;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JButton recoveryRule, btnBack;

    public FirewallHistory(FirewallManager manager, FirewallGUI gui) {
        this.manager = manager;
        this.gui = gui;

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
            String currentText = btnBack.getText();
            if (currentText.equals("Regles actives")) {
                btnBack.setText("Totes les regles");
                // Mostrar solo las reglas activas del historial
                tableModelHistory.setRowCount(0);
                List<FirewallHistoryRule> rules = manager.getHistoryRules();
                for (FirewallHistoryRule rule : rules) {
                    if (rule.getEndDate() == null) {
                        addRuleToTable(rule);
                    }
                }
            } else {
                btnBack.setText("Regles actives");
                // Mostrar solo las reglas activas del historial
                tableModelHistory.setRowCount(0);
                List<FirewallHistoryRule> rules = manager.getHistoryRules();
                for (FirewallHistoryRule rule : rules) {
                    addRuleToTable(rule);
                }
            }
        });
        northPanel.add(btnBack, BorderLayout.EAST);

        panel.add(northPanel, BorderLayout.NORTH);
        // Crear un JList para mostrar las reglas del Firewall
        ruleList = new JList<>();
        panel.add(ruleList, BorderLayout.WEST);

        // Crear un JScrollPane para mostrar las reglas del Firewall
        scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Vetana de carga para la interfaz de usuario ;
        JDialog loadingDialog = new JDialog(this, "Recuperant regla...", false);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new FlowLayout(FlowLayout.CENTER));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel panelProgressBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelProgressBar.add(progressBar);
        
        // Añadir el panel que contiene el progressBar al loadingDialog
        loadingDialog.add(panelProgressBar);
        loadingDialog.pack();
        loadingDialog.setResizable(false);

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

                // verificar que la data de finalització de la regla estigui buida
                if (tableModelHistory.getValueAt(selectedIndex, 11).equals("")) {
                    JOptionPane.showMessageDialog(this, "La regla ja està activa", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                	loadingDialog.setVisible(true);
                	SwingWorker<Void, Void> worker = new SwingWorker<>() {
                	    @Override
                	    protected Void doInBackground() throws Exception {
                	        try {
                	            Thread.sleep(1500);
                	            FirewallRule rule = manager.getHistoryRule(ruleName);
                	            manager.addHistoryRuleToActiveRules(rule);
                	            gui.addRuleToTable(rule);
                	            tableModelHistory.setValueAt("", selectedIndex, 11);
                	        } catch (InterruptedException e1) {
                	            JOptionPane.showMessageDialog(FirewallHistory.this, "Ha ocurrido un error", "Error", JOptionPane.ERROR_MESSAGE);
                	        }
                	        return null;
                	    }

                	    @Override
                	    protected void done() {
                	        loadingDialog.setVisible(false);
            	            JOptionPane.showMessageDialog(FirewallHistory.this, "Regla recuperada amb éxit", "Éxit", JOptionPane.INFORMATION_MESSAGE);

                	    }
                	};
                	worker.execute();
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

}