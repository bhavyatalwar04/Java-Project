
import java.sql.*;
import javax.swing.*;

public class Admin {

    public void addLibrarian(String name) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO librarians (name, active) VALUES (?, TRUE)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Librarian added to database: " + name);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding librarian: " + e.getMessage());
        }
    }

    public void viewLibrarians() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name, active FROM librarians";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("Librarians:\n");
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id"))
                        .append(", Name: ").append(rs.getString("name"))
                        .append(", Active: ").append(rs.getBoolean("active") ? "Yes" : "No")
                        .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error viewing librarians: " + e.getMessage());
        }
    }

    public void deleteLibrarian(String name) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM librarians WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Librarian deleted: " + name);
            } else {
                JOptionPane.showMessageDialog(null, "Librarian not found: " + name);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting librarian: " + e.getMessage());
        }
    }

    public void activateDeactivateLibrarian(String name, boolean activate) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE librarians SET active = ? WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, activate);
            stmt.setString(2, name);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                String status = activate ? "activated" : "deactivated";
                JOptionPane.showMessageDialog(null, "Librarian " + status + ": " + name);
            } else {
                JOptionPane.showMessageDialog(null, "Librarian not found: " + name);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating librarian: " + e.getMessage());
        }
    }

    public void viewFineReports() {
        String[] options = {"Individual Fine", "Monthly Total Fine"};
        int choice = JOptionPane.showOptionDialog(
                null, "Select Fine Report Type", "Fine Reports",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        try (Connection conn = DBConnection.getConnection()) {
            if (choice == 0) { // Individual
                String sql = "SELECT student_id, SUM(fine) AS total_fine FROM issued_books WHERE returned = TRUE GROUP BY student_id";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                StringBuilder sb = new StringBuilder("Individual Fine Report:\n");
                while (rs.next()) {
                    sb.append("Student ID: ").append(rs.getInt("student_id"))
                            .append(", Fine: ₹").append(rs.getDouble("total_fine")).append("\n");
                }
                JOptionPane.showMessageDialog(null, sb.toString());
            } else if (choice == 1) {
                String sql = "SELECT MONTH(return_date) AS month, SUM(fine) AS total_fine "
                        + "FROM issued_books WHERE returned = TRUE GROUP BY MONTH(return_date)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                StringBuilder sb = new StringBuilder("Monthly Fine Report:\n");
                while (rs.next()) {
                    int month = rs.getInt("month");
                    double totalFine = rs.getDouble("total_fine");
                    sb.append("Month ").append(month).append(": ₹").append(totalFine).append("\n");
                }
                JOptionPane.showMessageDialog(null, sb.toString());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching fine reports: " + e.getMessage());
        }
    }

    public void generateSystemReport() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql1 = "SELECT COUNT(*) AS total_books FROM books";
            String sql2 = "SELECT COUNT(*) AS total_issued FROM issued_books WHERE returned = FALSE";
            String sql3 = "SELECT COUNT(*) AS total_students FROM students";

            PreparedStatement stmt1 = conn.prepareStatement(sql1);
            ResultSet rs1 = stmt1.executeQuery();
            rs1.next();
            int totalBooks = rs1.getInt("total_books");

            PreparedStatement stmt2 = conn.prepareStatement(sql2);
            ResultSet rs2 = stmt2.executeQuery();
            rs2.next();
            int totalIssued = rs2.getInt("total_issued");

            PreparedStatement stmt3 = conn.prepareStatement(sql3);
            ResultSet rs3 = stmt3.executeQuery();
            rs3.next();
            int totalStudents = rs3.getInt("total_students");

            StringBuilder sb = new StringBuilder("Overall System Report:\n");
            sb.append("Total Books: ").append(totalBooks).append("\n")
                    .append("Currently Issued Books: ").append(totalIssued).append("\n")
                    .append("Total Students Registered: ").append(totalStudents);

            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error generating system report: " + e.getMessage());
        }
    }

}
