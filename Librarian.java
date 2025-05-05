import java.sql.*;
import javax.swing.*;

public class Librarian {

    // Add a new book
    public void addBook(String book) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO books (title) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Book added to database: " + book);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding book: " + e.getMessage());
        }
    }

    // View all books in the library
    public void viewBooks() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM books";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("Books:\n");
            while (rs.next()) {
                sb.append(rs.getInt("id")).append(": ").append(rs.getString("title")).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching books: " + e.getMessage());
        }
    }

    // Delete a book by title
    public void deleteBook(String bookTitle) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM books WHERE title = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bookTitle);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Book deleted: " + bookTitle);
            } else {
                JOptionPane.showMessageDialog(null, "Book not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting book: " + e.getMessage());
        }
    }

    // Issue a book to a student
    public void issueBook(int bookId, int studentId) {
        try (Connection conn = DBConnection.getConnection()) {
            // Check if the book is available
            String checkSql = "SELECT issued FROM books WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("issued") == 1) {
                JOptionPane.showMessageDialog(null, "Book already issued.");
                return;
            }

            // Issue the book
            String sql = "INSERT INTO issued_books (book_id, student_id, issue_date, due_date) VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY))";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, studentId);
            stmt.executeUpdate();

            // Update book status as issued
            PreparedStatement update = conn.prepareStatement("UPDATE books SET issued = 1 WHERE id = ?");
            update.setInt(1, bookId);
            update.executeUpdate();

            JOptionPane.showMessageDialog(null, "Book issued successfully.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error issuing book: " + e.getMessage());
        }
    }

    // View details of all students
    public void viewStudentDetails() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM students";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id"))
                  .append(", Name: ").append(rs.getString("name"))
                  .append(", Status: ").append(rs.getBoolean("active") ? "Active" : "Inactive")
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString(), "Student Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // View all issued books
    public void viewIssuedBooks() {
        displayBooks("SELECT * FROM issued_books WHERE returned = 0");
    }

    // View overdue books
    public void viewOverdueBooks() {
        displayBooks("SELECT * FROM issued_books WHERE returned = 0 AND due_date < CURDATE()");
    }

    // Helper method to display books
    private void displayBooks(String query) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Book ID: ").append(rs.getInt("book_id"))
                        .append(", Student ID: ").append(rs.getInt("student_id"))
                        .append(", Due Date: ").append(rs.getDate("due_date"))
                        .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error displaying books: " + e.getMessage());
        }
    }

    // Return a book and calculate fine
    public void returnBook(int bookId, int studentId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE issued_books SET return_date = CURDATE(), returned = 1, fine = DATEDIFF(CURDATE(), due_date) * 2 WHERE book_id = ? AND student_id = ? AND returned = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, studentId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                PreparedStatement update = conn.prepareStatement("UPDATE books SET issued = 0 WHERE id = ?");
                update.setInt(1, bookId);
                update.executeUpdate();
                JOptionPane.showMessageDialog(null, "Book returned and fine (if any) recorded.");
            } else {
                JOptionPane.showMessageDialog(null, "No matching issue found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error returning book: " + e.getMessage());
        }
    }
}
