
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Test {

    static Admin admin = new Admin();
    static Librarian librarian = new Librarian();
    static Student student = new Student();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showLoginScreen());
    }

    private static void showLoginScreen() {
        JFrame frame = new JFrame("Library Management System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(null);
        frame.getContentPane().setBackground(new Color(230, 230, 250));

        JLabel titleLabel = new JLabel("Smart Library Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBounds(50, 30, 300, 30);
        frame.add(titleLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(50, 80, 100, 30);
        frame.add(userLabel);

        JTextField userField = new JTextField();
        userField.setBounds(150, 80, 180, 30);
        frame.add(userField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(50, 130, 100, 30);
        frame.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(150, 130, 180, 30);
        frame.add(passField);

        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setBounds(50, 180, 100, 30);
        frame.add(roleLabel);

        String[] roles = {"Admin", "Librarian", "Student"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setBounds(150, 180, 180, 30);
        frame.add(roleBox);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(150, 230, 100, 30);
        frame.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = String.valueOf(passField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            // You can add real authentication here later
            if (!username.isEmpty() && !password.isEmpty()) {
                if ("Admin".equals(role)) {
                    try (Connection conn = DBConnection.getConnection()) {
                        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            frame.dispose();
                            showAdminMenu();
                        } else {
                            JOptionPane.showMessageDialog(frame, "Access Denied: You are not authorized as Admin.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error connecting to DB: " + ex.getMessage());
                    }
                } else {
                    frame.dispose();
                    switch (role) {
                        case "Librarian" ->
                            showLibrarianMenu();
                        case "Student" ->
                            showStudentMenu();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter username and password.");
            }

        });

        frame.setVisible(true);
    }

    private static void showAdminMenu() {
        JFrame frame = new JFrame("Admin Panel");
        frame.setSize(400, 400);
        JPanel panel = new JPanel(new GridLayout(7, 1));

        JButton addLibrarian = new JButton("Add Librarian");
        JButton viewLibrarians = new JButton("View Librarians");
        JButton deleteLibrarian = new JButton("Delete Librarian");
        JButton manageUsers = new JButton("Activate/Deactivate Librarian");
        JButton viewFines = new JButton("View Fine Reports");
        JButton systemReport = new JButton("System Reports");
        JButton logout = new JButton("Logout");

        addLibrarian.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Enter librarian name:");
            if (name != null && !name.isEmpty()) {
                admin.addLibrarian(name);
            }
        });

        viewLibrarians.addActionListener(e -> admin.viewLibrarians());

        deleteLibrarian.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Enter librarian name to delete:");
            if (name != null && !name.isEmpty()) {
                admin.deleteLibrarian(name);
            }
        });

        manageUsers.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Enter librarian name to activate/deactivate:");
            if (name != null && !name.isEmpty()) {
                int option = JOptionPane.showConfirmDialog(null, "Activate this librarian?", "Manage User", JOptionPane.YES_NO_OPTION);
                boolean activate = (option == JOptionPane.YES_OPTION);
                admin.activateDeactivateLibrarian(name, activate);
            }
        });

        viewFines.addActionListener(e -> admin.viewFineReports());

        systemReport.addActionListener(e -> admin.generateSystemReport());

        logout.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        panel.add(addLibrarian);
        panel.add(viewLibrarians);
        panel.add(deleteLibrarian);
        panel.add(manageUsers);
        panel.add(viewFines);
        panel.add(systemReport);
        panel.add(logout);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void showLibrarianMenu() {
        JFrame frame = new JFrame("Librarian Panel");
        frame.setSize(400, 500);
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));

        JButton addBook = new JButton("Add Book");
        JButton viewBooks = new JButton("View Books");
        JButton deleteBook = new JButton("Delete Book");
        JButton issueBook = new JButton("Issue Book");
        JButton returnBook = new JButton("Return Book");
        JButton viewIssuedBooks = new JButton("View Issued Books");
        JButton overdueBooks = new JButton("View Overdue Books");
        JButton logout = new JButton("Logout");

        addBook.addActionListener(e -> {
            String book = JOptionPane.showInputDialog("Enter book name:");
            if (book != null && !book.isEmpty()) {
                librarian.addBook(book);
            }
        });
        viewBooks.addActionListener(e -> librarian.viewBooks());
        deleteBook.addActionListener(e -> {
            String title = JOptionPane.showInputDialog("Enter book title to delete:");
            if (title != null && !title.isEmpty()) {
                librarian.deleteBook(title);
            }
        });
        issueBook.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(JOptionPane.showInputDialog("Enter Book ID:"));
                int studentId = Integer.parseInt(JOptionPane.showInputDialog("Enter Student ID:"));
                librarian.issueBook(bookId, studentId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input.");
            }
        });
        returnBook.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(JOptionPane.showInputDialog("Enter Book ID:"));
                int studentId = Integer.parseInt(JOptionPane.showInputDialog("Enter Student ID:"));
                librarian.returnBook(bookId, studentId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input.");
            }
        });
        viewIssuedBooks.addActionListener(e -> librarian.viewIssuedBooks());
        overdueBooks.addActionListener(e -> librarian.viewOverdueBooks());
        logout.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        panel.add(addBook);
        panel.add(viewBooks);
        panel.add(deleteBook);
        panel.add(issueBook);
        panel.add(returnBook);
        panel.add(viewIssuedBooks);
        panel.add(overdueBooks);
        panel.add(logout);

        frame.add(panel);
        frame.setVisible(true);

    }

    private static void showStudentMenu() {
        JFrame frame = new JFrame("Student Panel");
        frame.setSize(400, 500);
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));

        JButton borrowBook = new JButton("Borrow Book");
        JButton returnBook = new JButton("Return Book");
        JButton viewStatus = new JButton("View Status");
        JButton viewNotifications = new JButton("View Notifications");
        JButton requestBook = new JButton("Request New Book");
        JButton reissueBook = new JButton("Reissue Book");
        JButton holdBook = new JButton("Hold Book");
        JButton logout = new JButton("Logout");

        borrowBook.addActionListener(e -> {
            String book = JOptionPane.showInputDialog("Enter book to borrow:");
            if (book != null && !book.isEmpty()) {
                student.borrowBook(librarian, book);
            }
        });
        returnBook.addActionListener(e -> {
            String book = JOptionPane.showInputDialog("Enter book to return:");
            if (book != null && !book.isEmpty()) {
                student.returnBook(librarian, book);
            }
        });
        viewStatus.addActionListener(e -> student.viewStatus());
        viewNotifications.addActionListener(e -> student.viewNotifications());
        requestBook.addActionListener(e -> {
            String title = JOptionPane.showInputDialog("Enter title of the book to request:");
            if (title != null && !title.isEmpty()) {
                student.requestBook(title);
            }
        });
        reissueBook.addActionListener(e -> {
            String title = JOptionPane.showInputDialog("Enter title of the book to reissue:");
            if (title != null && !title.isEmpty()) {
                student.reissueBook(title);
            }
        });
        holdBook.addActionListener(e -> {
            String title = JOptionPane.showInputDialog("Enter title of the book to hold:");
            if (title != null && !title.isEmpty()) {
                student.holdBook(title);
            }
        });
        logout.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });

        panel.add(borrowBook);
        panel.add(returnBook);
        panel.add(viewStatus);
        panel.add(viewNotifications);
        panel.add(requestBook);
        panel.add(reissueBook);
        panel.add(holdBook);
        panel.add(logout);

        frame.add(panel);
        frame.setVisible(true);
    }
}
