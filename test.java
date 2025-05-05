
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Test extends Student {

    static Admin admin = new Admin();
    static Librarian librarian = new Librarian();
    static Student student = new Student();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showLoginScreen());
    }

    private static void showLoginScreen() {
        JFrame frame = new JFrame("Library Management System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 450);
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
        loginButton.setBounds(100, 230, 80, 30);
        frame.add(loginButton);

        JButton signupButton = new JButton("Signup");
        signupButton.setBounds(200, 230, 80, 30);
        frame.add(signupButton);

        //Button for forgot password
        JButton forgotPasswordButton = new JButton("Forgot Password");
        forgotPasswordButton.setBounds(100, 280, 180, 30);
        frame.add(forgotPasswordButton);

        forgotPasswordButton.addActionListener(e -> {
            String username = userField.getText();
            String role = (String) roleBox.getSelectedItem();

            if (!username.isEmpty()) {
                try (Connection conn = DBConnection.getConnection()) {
                    String table = "";
                    String sql = "";
                    switch (role) {
                        case "Admin":
                            table = "admins";
                            sql = "SELECT password FROM " + table + " WHERE username = ?";
                            break;
                        case "Librarian":
                            table = "librarians";
                            sql = "SELECT password FROM " + table + " WHERE name = ?";
                            break;
                        case "Student":
                            table = "students";
                            sql = "SELECT password FROM " + table + " WHERE name = ?";
                            break;
                        default:
                            JOptionPane.showMessageDialog(frame, "Invalid role.");
                            return;
                    }

                    //String sql = "SELECT password FROM " + table + " WHERE username = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String retrievedPassword = rs.getString("password");
                        JOptionPane.showMessageDialog(frame, "Password for " + role + " \"" + username + "\": " + retrievedPassword);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Username not found in " + role + " records.");
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter your username.");
            }
        });

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = String.valueOf(passField.getPassword());
            String role = (String) roleBox.getSelectedItem();

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
                    try (Connection conn = DBConnection.getConnection()) {
                        String sql = "";
                        switch (role) {
                            case "Librarian":
                                sql = "SELECT * FROM librarians WHERE name = ? AND password = ?";
                                break;
                            case "Student":
                                sql = "SELECT * FROM students WHERE name = ? AND password = ?";
                                break;
                        }

                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            frame.dispose();
                            if ("Librarian".equals(role)) {
                                showLibrarianMenu();
                            } else if ("Student".equals(role)) {
                                showStudentMenu();
                            }
                        } else {
                            JOptionPane.showMessageDialog(frame, "Invalid credentials or user not registered.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error connecting to DB: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter username and password.");
            }
        });

        signupButton.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField idField = new JTextField();
            String[] statusOptions = {"Active", "Deactive"};
            JComboBox<String> statusBox = new JComboBox<>(statusOptions);

            Object[] message = {
                "Student Name:", nameField,
                "Student ID:", idField,
                "Status:", statusBox
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Student Registration", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String name = nameField.getText();
                int id;
                try {
                    id = Integer.parseInt(idField.getText());
                    boolean isActive = "Active".equals(statusBox.getSelectedItem());
                    Student newStudent = new Student(name, id, isActive);
                    JOptionPane.showMessageDialog(null, "Student Registered Successfully.");
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid ID format.");
                }
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
        String[] options = {
            "Add Book", "View Books", "Delete Book",
            "Issue Book", "Return Book", "View Issued Books",
            "View Overdue Books", "View Student Details", "Logout"
        };

        while (true) {
            int choice = JOptionPane.showOptionDialog(null, "Librarian Dashboard", "Options",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, options[0]);

            switch (choice) {
                case 0:
                    String book = JOptionPane.showInputDialog("Enter Book Title:");
                    if (book != null && !book.trim().isEmpty()) {
                        librarian.addBook(book.trim());
                    }
                    break;
                case 1:
                    librarian.viewBooks();
                    break;
                case 2:
                    String delBook = JOptionPane.showInputDialog("Enter Book Title to Delete:");
                    if (delBook != null && !delBook.trim().isEmpty()) {
                        librarian.deleteBook(delBook.trim());
                    }
                    break;
                case 3:
                    try {
                        int bookId = Integer.parseInt(JOptionPane.showInputDialog("Enter Book ID:"));
                        int studentId = Integer.parseInt(JOptionPane.showInputDialog("Enter Student ID:"));
                        librarian.issueBook(bookId, studentId);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid input. Please enter numeric IDs.");
                    }
                    break;
                case 4:
                    try {
                        int bookId = Integer.parseInt(JOptionPane.showInputDialog("Enter Book ID:"));
                        int studentId = Integer.parseInt(JOptionPane.showInputDialog("Enter Student ID:"));
                        librarian.returnBook(bookId, studentId);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid input. Please enter numeric IDs.");
                    }
                    break;
                case 5:
                    librarian.viewIssuedBooks();
                    break;
                case 6:
                    librarian.viewOverdueBooks();
                    break;
                case 7:
                    librarian.viewStudentDetails();
                    break;
                case 8:
                    JOptionPane.showMessageDialog(null, "Logging out...");
                    showLoginScreen();
                    return;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid selection.");
            }
        }
    }

    private static void showStudentMenu() {
        JFrame frame = new JFrame("Student Panel");
        frame.setSize(400, 500);
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));
        Student student = new Student("username", 1111, true);

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
