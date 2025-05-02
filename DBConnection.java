
import java.sql.*;

public class DBConnection {

    private static final String DB_NAME = "library_system";
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root"; // Change if your MySQL username is different
    private static final String PASSWORD = "Kbvnoas@7"; // Change this to your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Force-load the driver
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Include it in your classpath.");
        }
        setupDatabase(); // Then setup the DB
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL + DB_NAME, USER, PASSWORD);
    }

    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); Statement stmt = conn.createStatement()) {

            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            // Create librarians table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS librarians (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    active TINYINT(1) DEFAULT 1
                )""");

            // Create books table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS books (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(200) NOT NULL,
                    issued TINYINT(1) DEFAULT 0
                )""");

            // Create students table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS students (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    active TINYINT(1) DEFAULT 1
                )""");

            // Create issued_books table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS issued_books (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    book_id INT,
                    student_id INT,
                    issue_date DATE,
                    due_date DATE,
                    return_date DATE,
                    fine DOUBLE DEFAULT 0,
                    returned TINYINT(1) DEFAULT 0
                )""");
            stmt.executeUpdate("""
    CREATE TABLE IF NOT EXISTS admins (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(100) NOT NULL UNIQUE,
        password VARCHAR(100) NOT NULL
    )""");

// Insert default admins only if table is empty
            ResultSet adminCheck = stmt.executeQuery("SELECT COUNT(*) AS total FROM admins");
            if (adminCheck.next() && adminCheck.getInt("total") == 0) {
                stmt.executeUpdate("INSERT INTO admins (username, password) VALUES"
                        + "('Arnav', 'Arna'),"
                        + "('Arunangshu', 'Arun'),"
                        + "('Bhavya', 'Bhav'),"
                        + "('Navanshu', 'Nava')");
                System.out.println("Default admin accounts created.");
            }

            System.out.println("Database and all required tables are ready.");
            try {
                stmt.executeUpdate("ALTER TABLE issued_books ADD COLUMN return_date DATE");
            } catch (SQLException e) {
                // Ignore if column already exists
                if (!e.getMessage().contains("Duplicate column")) {
                    System.err.println("Could not add 'return_date' column: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
