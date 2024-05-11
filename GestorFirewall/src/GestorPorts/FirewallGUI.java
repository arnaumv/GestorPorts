package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirewallGUI {
    private FirewallManager manager;
    private JTable table;
    private JButton modifyButton;
    private JButton deleteButton;
    private JButton newRuleButton;
    private JButton historyButton;
    private DefaultTableModel tableModel;
    private JList<String> historyList;
    private JFrame frame;

    public FirewallGUI() {
        // Obtenemos la instancia del administrador del firewall
        this.manager = FirewallManager.getInstance();

        // Creamos el modelo de la tabla con los nombres de las columnas
        this.tableModel = new DefaultTableModel(new Object[] { "Nom", "Port", "Protocol", "App", "Usuari", "Grup",
                "IP", "Accio", "Interficie", "Sentit" }, 0);

        // Creamos la tabla con el modelo definido
        this.table = new JTable(tableModel);


        // Creamos los botones y los nombramos
        this.modifyButton = new JButton("Modificar");
        this.deleteButton = new JButton("Esborrar");
        this.newRuleButton = new JButton("Nova Regla");
        this.historyButton = new JButton("Historial");

        // Creamos la lista del historial
        this.historyList = new JList<>();

        // Creamos el marco de la ventana y le damos un título
        this.frame = new JFrame("Configurar Regles del Firewall");

        // Deshabilitamos los botones de modificar y eliminar al inicio
        modifyButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // Añadimos un listener a la selección de la tabla para habilitar los botones
        // cuando se selecciona una fila
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean isRowSelected = table.getSelectedRow() != -1;
            modifyButton.setEnabled(isRowSelected);
            deleteButton.setEnabled(isRowSelected);
        });

        // Añadimos un listener al botón de nueva regla para abrir el diálogo de
        // creación de reglas
        newRuleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RuleDialog dialog = new RuleDialog(frame);
                dialog.setVisible(true);
                if (dialog.isRuleSaved()) {
                    addRuleToTable(dialog.getRule());
                }
            }
        });

        // Añadimos un listener al botón de modificar para abrir el diálogo de
        // modificación de reglas
        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    List<Object> selectedRule = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        selectedRule.add(tableModel.getValueAt(selectedRow, i));
                    }

                    // Guardamos el nombre original de la regla
                    String originalName = (String) selectedRule.get(0);
                    System.out.println("Original rule name: " + originalName);

                    RuleModifier ruleModifier = new RuleModifier(manager, selectedRule);
                    JPanel modifyPanel = ruleModifier.getModifyPanel();

                    // Establecemos el tamaño preferido del panel
                    modifyPanel.setPreferredSize(new Dimension(400, 400));
                    

                    // Declaramos el marco de la ventana aquí
                    JFrame modifyFrame = new JFrame("Modificar Regla");

                    // Creamos un nuevo panel para los botones con un FlowLayout
                    JPanel buttonPanel = new JPanel(new FlowLayout());

                    JButton saveButton = new JButton("Guardar");
                    saveButton.addActionListener(ruleModifier.getSaveButtonActionListener(modifyFrame, originalName,
                            tableModel, selectedRow));
                    buttonPanel.add(saveButton);

                    // Añadimos un botón de cancelar que cierra el marco
                    JButton cancelButton = new JButton("Cancelar");
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            modifyFrame.dispose();
                        }
                    });
                    buttonPanel.add(cancelButton);

                    // Añadimos un poco de espacio encima de los botones
                    modifyPanel.add(Box.createVerticalStrut(20));

                    // Añadimos el panel de botones al panel de modificación
                    modifyPanel.add(buttonPanel);

                    // Añadimos un borde al panel para crear espacio entre los botones y los campos
                    // de arriba
                    modifyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    modifyFrame.setContentPane(modifyPanel);
                    modifyFrame.pack();
                    modifyFrame.setLocationRelativeTo(null);
                    modifyFrame.setVisible(true);
                }
            }
        });

        // Añadimos un listener al botón de eliminar para abrir el diálogo de
        // confirmación de eliminación
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Obtenemos la información de la regla
                    StringBuilder ruleInfo = new StringBuilder();
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        ruleInfo.append(table.getColumnName(i)).append(": ").append(table.getValueAt(selectedRow, i))
                                .append("\n");
                    }

                    // Creamos un DeleteRuleDialog
                    DeleteRuleDialog dialog = new DeleteRuleDialog(null, ruleInfo.toString());
                    dialog.setVisible(true);

                    if (dialog.getUserOption() == JOptionPane.YES_OPTION) {
                        // Obtenemos el nombre de la regla
                        String ruleName = table.getValueAt(selectedRow, 0).toString(); // Cambia 0 al índice de la
                                                                                       // columna
                                                                                       // del nombre de la regla

                        // Eliminamos la regla de la base de datos y del sistema operativo
                        // Usamos la instancia de manager existente en lugar de crear una nueva
                        try {
                            manager.deleteRule(ruleName);

                            // Eliminamos la regla de la tabla
                            ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            // Mostramos un diálogo al usuario indicando que hubo un error
                            JOptionPane.showMessageDialog(null, "Error deleting rule: " + ex.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        // Añadimos un listener al botón de historial para abrir el historial
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FirewallHistory history = new FirewallHistory(manager, FirewallGUI.this);
                history.setVisible(true);
            }
        });

        // Cargamos las reglas de la base de datos
        List<FirewallRule> rules = manager.getAllRules();
        for (FirewallRule rule : rules) {
            addRuleToTable(rule);
        }
    }

    public void addRuleToTable(FirewallRule rule) {
        // Añadimos la regla a la tabla
        tableModel.addRow(new Object[] {
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
    }

    public void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1200, 500);


        JLabel rulesLabel = new JLabel("Regles: ");
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Añadir márgenes de 20px
        topPanel.add(rulesLabel, BorderLayout.WEST);
        topPanel.add(historyButton, BorderLayout.EAST);

        JPanel bottomLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomLeftPanel.add(newRuleButton);

        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.add(modifyButton);
        bottomRightPanel.add(deleteButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
        bottomPanel.add(bottomRightPanel, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20)); // Añadir márgenes de 20px


        // Add components to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table) {
            @Override
            public Insets getInsets() {
                return new Insets(20, 20, 20, 20); // Márgenes de 20 píxeles en cada lado
            }
        }, BorderLayout.CENTER);
        // Centrar la ventana en la pantalla después de empaquetar los componentes
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }


}