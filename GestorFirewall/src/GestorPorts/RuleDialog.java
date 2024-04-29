package GestorPorts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class RuleDialog extends JDialog {
    // Variables de instancia para la interfaz de usuario y la regla de firewall
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

        // Inicialización de los campos de la interfaz de usuario
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

        // Inicialización del indicador de carga
        loadingIndicator = new JLabel("Cargando...");
        loadingIndicator.setVisible(false);

        // Acción del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Desactivar elementos interactivos durante la carga
                setInteractiveElementsEnabled(false);

                // Mostrar indicador de carga
                showLoadingIndicator();

                // Crear un SwingWorker para realizar la tarea de larga duración
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    private boolean isError = false; // Variable para controlar si hay un error

                    @Override
                    protected Void doInBackground() throws Exception {
                        // Simular tiempo de carga
                        Thread.sleep(1500);

                        // Comprobar si el campo del puerto está vacío
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

                        // Crear una nueva regla de firewall a partir de los campos de entrada
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

                        // Validar la regla y comprobar si hay duplicados
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
                            isError = true; // Marcar que hay un error
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
                            isError = true; // Marcar que hay un error
                            return null;
                        }

                        // Añadir la regla al firewall
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
                            isError = true; // Marcar que hay un error
                            return null;
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        // Ocultar indicador de carga
                        hideLoadingIndicator();

                        // Habilitar elementos interactivos
                        setInteractiveElementsEnabled(true);

                        try {
                            get(); // Esta línea puede lanzar una ExecutionException si doInBackground() lanzó una
                                   // excepción
                        } catch (InterruptedException e) {
                            // Esto ocurre si el hilo de SwingWorker fue interrumpido
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // Esto ocurre si lanzamos una excepción desde doInBackground().
                            Throwable cause = e.getCause();
                            JOptionPane.showMessageDialog(RuleDialog.this,
                                    "Error: " + cause.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Si no hubo error, cerrar el diálogo
                        if (!isError) { // Modificar esta línea
                            RuleDialog.this.dispose();
                        }
                    }
                };

                // Iniciar el SwingWorker
                worker.execute();
            }
        });

        // Acción del botón de cancelar
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cerrar diálogo sin guardar
                RuleDialog.this.dispose();
            }
        });

        // Configuración del layout y adición de los componentes a la interfaz de
        // usuario
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

    // Métodos getter para la regla y el estado de guardado de la regla
    public boolean isRuleSaved() {
        return ruleSaved;
    }

    public FirewallRule getRule() {
        return rule;
    }

    // Métodos para habilitar/deshabilitar elementos interactivos y mostrar/ocultar
    // el indicador de carga
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

    // Método para validar la regla de firewall
    private String isRuleValid(FirewallRule rule) {
        // Comprobar si el nombre de la regla está vacío
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            return " El nom de la regla no pot estar buit.";
        }

        // Comprobar si el número de puerto es válido
        if (rule.getPort() < 1 || rule.getPort() > 65535) {
            return " El número de port no és vàlid.";
        }

        // Comprobar si la dirección IP es válida
        if (rule.getIpAddress() != null && !rule.getIpAddress().isEmpty() && !isValidIP(rule.getIpAddress())) {
            return " L'adreça IP no és vàlida.";
        }

        return null;
    }

    // Métodos para validar la dirección IP
    private boolean isValidIP(String ip) {
        // Comprobar si la dirección IP es un rango
        if (ip.contains("-")) {
            String[] parts = ip.split("-");
            if (parts.length != 2) {
                return false;
            }

            return isValidSingleIP(parts[0].trim()) && isValidSingleIP(parts[1].trim());
        }

        // Si no es un rango, comprobar si es una dirección IP válida
        return isValidSingleIP(ip);
    }

    private boolean isValidSingleIP(String ip) {
        // Usar una expresión regular (regex) para comprobar si la dirección IP es
        // válida
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }

    // Método para comprobar si la regla es duplicada
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