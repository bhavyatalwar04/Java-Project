import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignupForm extends JFrame {
    public SignupForm() {
        setTitle("Student Signup");
        setSize(350, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"active", "inactive"});
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JButton registerButton = new JButton("Register");

        panel.add(new JLabel("Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Create Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField);
        panel.add(new JLabel("")); // Spacer
        panel.add(registerButton);

        add(panel);

        registerButton.addActionListener(e -> {
            String idText = idField.getText();
            String name = nameField.getText();
            String status = statusBox.getSelectedItem().toString();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (idText.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            try {
                int id = Integer.parseInt(idText);
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "INSERT INTO students (id, name, active, password) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, id);
                    stmt.setString(2, name);
                    stmt.setBoolean(3, status.equals("active"));
                    stmt.setString(4, password); // Consider hashing in production
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Student registered successfully!");
                    dispose();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Student ID must be a number.");
            } catch (SQLIntegrityConstraintViolationException dup) {
                JOptionPane.showMessageDialog(this, "Student ID already exists.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        setVisible(true);
    }
}
