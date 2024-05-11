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
    private JComboBox<String> interficieField;
    private JComboBox<String> sentitField;
    private JButton saveButton;
    private JButton cancelButton;



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
        interficieField = new JComboBox<>(new String[] { "Totes", "LAN", "WIRELESS", "RAS" });
        sentitField = new JComboBox<>(new String[] { "IN", "OUT" });
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");

        // Vetana de carga para la interfaz de usuario ;
        JDialog loadingDialog = new JDialog(this, "Guardant regla...", false);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new FlowLayout(FlowLayout.CENTER));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingDialog.add(progressBar);
        loadingDialog.pack();
        loadingDialog.setResizable(false);
       
        

        // Acción del botón de guardar
        saveButton.addActionListener(e -> {
            // Desactivar elementos interactivos durante la carga
            setInteractiveElementsEnabled(false);

            // Mostrar indicador de carga
            loadingDialog.setVisible(true);
            

            // Crear un SwingWorker para realizar la tarea de larga duración
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                private boolean isError = false; // Variable para controlar si hay un error

                @Override
                protected Void doInBackground() throws Exception {
                    // Simular tiempo de carga
                    Thread.sleep(1500);

                    // Comprobar si el campo del puerto está vacío
                    if (isNullOrEmpty(portField.getText())) {
                        showErrorDialog("El campo del puerto no puede estar vacío.");
                        isError = true;
                        return null;
                    }

                    // Comprobar si el puerto es un número válido
                    int port;
                    try {
                        port = Integer.parseInt(portField.getText());
                        if (port < 1 || port > 65535) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        showErrorDialog("El número de puerto no es válido. Debe ser un número entre 1 y 65535.");
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
                            (String) interficieField.getSelectedItem(), // Cambiado aquí
                            (String) sentitField.getSelectedItem());

                    // Validar la regla y comprobar si hay duplicados
                    String ruleValidationError = isRuleValid(rule);
                    if (ruleValidationError != null) {
                        showErrorDialog(ruleValidationError);
                        isError = true; // Marcar que hay un error
                        return null;
                    }

                    if (isDuplicateRule(rule)) {
                        showErrorDialog("Regla duplicada.");
                        isError = true; // Marcar que hay un error
                        return null;
                    }

                    // Añadir la regla al firewall
                    try {
                        FirewallManager manager = FirewallManager.getInstance();
                        manager.addRule(rule);
                        ruleSaved = true;
                    } catch (Exception ex) {
                        // LANZA LOS ERRROES DE PARTE DEL FIREWALL MANAEGER
                        showErrorDialog("Error: " + ex.getMessage());
                        isError = true; // Marcar que hay un error
                        return null;
                    }

                    return null;
                }

                @Override
                protected void done() {
                    // Ocultar indicador de carga
                    loadingDialog.setVisible(false);
                    
                    // Habilitar elementos interactivos
                    setInteractiveElementsEnabled(true);

                    try {
                        get(); // Esta línea puede lanzar una ExecutionException si doInBackground() lanzó una
                               // excepción
                    } catch (InterruptedException | ExecutionException e) {
                        // Esto ocurre si lanzamos una excepción desde doInBackground().
                        Throwable cause = e.getCause();
                        showErrorDialog("Error: " + cause.getMessage());
                        return;
                    }

                    // Si no hubo error, cerrar el diálogo
                    if (!isError) {
                        RuleDialog.this.dispose();
                    }
                }

                private boolean isNullOrEmpty(String str) {
                    return str == null || str.trim().isEmpty();
                }

                private void showErrorDialog(String message) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(RuleDialog.this,
                            message, "Error",
                            JOptionPane.ERROR_MESSAGE));
                }
            };

            // Iniciar el SwingWorker
            worker.execute();
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
        
        setLayout(new BorderLayout()); // Establecer el BorderLayout para el diálogo

        
        JPanel fieldsPanel = new JPanel(new GridLayout(13, 2));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Establecer márgenes

        fieldsPanel.add(new JLabel("Nom: "));
        fieldsPanel.add(nomField);
        fieldsPanel.add(new JLabel("Port: "));
        fieldsPanel.add(portField);
        fieldsPanel.add(new JLabel("Protocol: "));
        fieldsPanel.add(protocolField);
        fieldsPanel.add(new JLabel("App: "));
        fieldsPanel.add(appField);
        fieldsPanel.add(new JLabel("Usuari: "));
        fieldsPanel.add(usuariField);
        fieldsPanel.add(new JLabel("Grup: "));
        fieldsPanel.add(grupField);
        fieldsPanel.add(new JLabel("IP: "));
        fieldsPanel.add(ipField);
        fieldsPanel.add(new JLabel("Accio: "));
        fieldsPanel.add(accioField);
        fieldsPanel.add(new JLabel("Interficie: "));
        fieldsPanel.add(interficieField);
        fieldsPanel.add(new JLabel("Sentit: "));
        fieldsPanel.add(sentitField);
        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // Establecer márgenes
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 400);
        setLocationRelativeTo(null);
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

    // Método para validar la regla de firewall
    private String isRuleValid(FirewallRule rule) {
        // Comprobar si el nombre de la regla está vacío
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            return " El nom de la regla no pot estar buit.";
        }

        // Comprobar si la dirección IP es válida
        if (rule.getIpAddress() != null && !rule.getIpAddress().isEmpty() && !isValidIP(rule.getIpAddress())) {
            return " L'adreça IP no és vàlida. ";
        }

        return null;
    }

    // Método para validar la dirección IP
    private boolean isValidIP(String ip) {
        // Usar una expresión regular (regex) para comprobar si la dirección IP es
        // válida
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // Comprobar si la dirección IP es un rango
        if (ip.contains("-")) {
            String[] parts = ip.split("-");
            if (parts.length != 2) {
                return false;
            }

            return parts[0].trim().matches(regex) && parts[1].trim().matches(regex);
        }

        // Si no es un rango, comprobar si es una dirección IP válida
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