package GestorPorts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class RuleDialog extends JDialog {
    private boolean ruleSaved = false;
    private FirewallRule rule;
    private JTextField nomField;
    private JTextField portField;
    private JComboBox<String> protocolField;
    private JTextField appField;
    private JTextField usuariField;
    private JTextField grupField;
    private JTextField ipField;
    private JComboBox<String> accioField;
    private JTextField interficieField;
    private JComboBox<String> sentitField;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel loadingIndicator;

    public RuleDialog(Frame owner) {
        super(owner, "Nova Regla", true);

        nomField = new JTextField();
        portField = new JTextField();
        protocolField = new JComboBox<>(new String[] { "TCP", "UDP", "ICMP", "IP" });
        appField = new JTextField();
        usuariField = new JTextField();
        grupField = new JTextField();
        ipField = new JTextField();
        accioField = new JComboBox<>(new String[] { "Permetre", "Denegar" });
        interficieField = new JTextField();
        sentitField = new JComboBox<>(new String[] { "IN", "OUT" });
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");

        // Initialize the loading indicator
        loadingIndicator = new JLabel("Cargando...");
        loadingIndicator.setVisible(false);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Disable interactive elements
                setInteractiveElementsEnabled(false);

                // Show loading indicator
                showLoadingIndicator();

                // Create a SwingWorker to perform the long-running task
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    private boolean isError = false; // Add this line

                    @Override
                    protected Void doInBackground() throws Exception {
                        // Simulate loading time
                        Thread.sleep(1500);

                        // Check if the port field is empty
                        if (portField.getText() == null || portField.getText().trim().isEmpty()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(RuleDialog.this,
                                            "El campo del puerto no puede estar vacío.", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            isError = true;
                            return null;
                        }

                        // Create a new FirewallRule from the input fields
                        rule = new FirewallRule(
                                nomField.getText(),
                                Integer.parseInt(portField.getText()),
                                (String) protocolField.getSelectedItem(),
                                appField.getText(),
                                usuariField.getText(),
                                grupField.getText(),
                                ipField.getText(),
                                (String) accioField.getSelectedItem(),
                                interficieField.getText(),
                                (String) sentitField.getSelectedItem());

                        // Validate the rule and check for duplicates
                        String ruleValidationError = isRuleValid(rule);
                        if (ruleValidationError != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(RuleDialog.this,
                                            ruleValidationError, "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            isError = true; // Add this line
                            return null;
                        }

                        if (isDuplicateRule(rule)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(RuleDialog.this,
                                            " Regla duplicada.", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            isError = true; // Add this line
                            return null;
                        }

                        // Add the rule to the firewall
                        try {
                            FirewallManager manager = new FirewallManager();
                            manager.addRule(rule);
                            ruleSaved = true;
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(RuleDialog.this,
                                            "Error: " + ex.getMessage(), "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                            isError = true; // Add this line
                            return null;
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        // Hide loading indicator
                        hideLoadingIndicator();

                        // Enable interactive elements
                        setInteractiveElementsEnabled(true);

                        try {
                            get(); // This line can throw an ExecutionException if doInBackground() threw an
                                   // exception
                        } catch (InterruptedException e) {
                            // This happens if the SwingWorker's thread was interrupted
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // This happens if we throw an exception from doInBackground().
                            Throwable cause = e.getCause();
                            JOptionPane.showMessageDialog(RuleDialog.this,
                                    "Error: " + cause.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // If there was no error, close the dialog
                        if (!isError) { // Modify this line
                            RuleDialog.this.dispose();
                        }
                    }
                };

                // Start the SwingWorker
                worker.execute();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close dialog without saving
                RuleDialog.this.dispose();
            }
        });

        setLayout(new GridLayout(13, 2));
        add(new JLabel("Nom: "));
        add(nomField);
        add(new JLabel("Port: "));
        add(portField);
        add(new JLabel("Protocol: "));
        add(protocolField);
        add(new JLabel("App: "));
        add(appField);
        add(new JLabel("Usuari: "));
        add(usuariField);
        add(new JLabel("Grup: "));
        add(grupField);
        add(new JLabel("IP: "));
        add(ipField);
        add(new JLabel("Accio: "));
        add(accioField);
        add(new JLabel("Interficie: "));
        add(interficieField);
        add(new JLabel("Sentit: "));
        add(sentitField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel);

        pack();
    }

    public boolean isRuleSaved() {
        return ruleSaved;
    }

    public FirewallRule getRule() {
        return rule;
    }

    private void setInteractiveElementsEnabled(boolean enabled) {
        nomField.setEnabled(enabled);
        portField.setEnabled(enabled);
        protocolField.setEnabled(enabled);
        appField.setEnabled(enabled);
        usuariField.setEnabled(enabled);
        grupField.setEnabled(enabled);
        ipField.setEnabled(enabled);
        accioField.setEnabled(enabled);
        interficieField.setEnabled(enabled);
        sentitField.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }

    private void showLoadingIndicator() {
        loadingIndicator.setVisible(true);
    }

    private void hideLoadingIndicator() {
        loadingIndicator.setVisible(false);
    }

    private String isRuleValid(FirewallRule rule) {
        // Comprova si el nom de la regla està buit
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            return " El nom de la regla no pot estar buit.";
        }

        // Comprova si el número de port és vàlid
        if (rule.getPort() < 1 || rule.getPort() > 65535) {
            return " El número de port no és vàlid.";
        }

        // Comprova si l'adreça IP és vàlida
        if (rule.getIpAddress() != null && !rule.getIpAddress().isEmpty() && !isValidIP(rule.getIpAddress())) {
            return " L'adreça IP no és vàlida.";
        }

        return null;
    }

    private boolean isValidIP(String ip) {
        // Check if the IP address is a range
        if (ip.contains("-")) {
            String[] parts = ip.split("-");
            if (parts.length != 2) {
                return false;
            }

            return isValidSingleIP(parts[0].trim()) && isValidSingleIP(parts[1].trim());
        }

        // If it's not a range, check if it's a valid single IP address
        return isValidSingleIP(ip);
    }

    private boolean isValidSingleIP(String ip) {
        // Use a regular expression (regex) to check if the IP address is valid
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }

    private boolean isDuplicateRule(FirewallRule rule) {
        FirewallRuleDAO dao;
        try {
            dao = FirewallRuleDAO.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // O maneja esta excepción de la manera que prefieras
        }
        return dao.ruleExists(rule);
    }
}