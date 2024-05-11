
package GestorPorts;

import javax.swing.*;
import java.awt.*;

public class DeleteRuleDialog extends JDialog {
    private int userOption;

    public DeleteRuleDialog(Frame parent, String ruleInfo) {
        super(parent, "Confirmar eliminació", true);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        String[] ruleInfoLines = ruleInfo.split("\n");
        for (String line : ruleInfoLines) {
            infoPanel.add(new JLabel(line));
        }
        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Establecer márgenes

        JButton deleteButton = new JButton("Esborrar");
        JButton cancelButton = new JButton("Cancelar");

        deleteButton.addActionListener(e -> {
            userOption = JOptionPane.YES_OPTION;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            userOption = JOptionPane.NO_OPTION;
            setVisible(false);
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.PAGE_END);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Establecer márgenes


        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    public int getUserOption() {
        return userOption;
    }
}