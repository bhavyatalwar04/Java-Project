import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.Date;

public class Student extends Librarian {
    String name = null;
    int id = 0;
    boolean active = false;
    double Studentfine = 0.0;

    Map<Integer, String> myBooks = new HashMap<>();
    ArrayList<String> notifications = new ArrayList<>();

    Student() {
        this.name = null;
        this.id = 0;
        this.active = false;
    }

    Student(String name, int id, boolean active) {
        this.name = name;
        this.id = id;
        this.active = true;
    }

    public void borrowBook(Librarian librarian, String book) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM books WHERE issued = 0 AND title = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Book available.");
                String bookav = rs.getString("title");
                int bookidav = rs.getInt("id");
                bookav = bookav.replaceAll("'", "''");

                String updateSql = "UPDATE books SET issued = 1 WHERE id = ?";
                PreparedStatement updatestmt = conn.prepareStatement(updateSql);
                updatestmt.setInt(1, bookidav);
                updatestmt.executeUpdate();

                String insertion = "INSERT INTO issued_books(book_id, student_id, issue_date, due_date) VALUES(?, ?, ?, ?)";
                PreparedStatement inssstmt = conn.prepareStatement(insertion);

                LocalDate currentDate = LocalDate.now();
                Date issuedDate = java.sql.Date.valueOf(currentDate);
                Date dueDate = java.sql.Date.valueOf(currentDate.plusDays(5)); // due after 5 days

                inssstmt.setInt(1, bookidav);
                inssstmt.setInt(2, id);
                inssstmt.setDate(3, issuedDate);
                inssstmt.setDate(4, dueDate);

                inssstmt.executeUpdate();

                myBooks.put(bookidav, bookav);
                notifications.add("Borrowed: " + book);

                JOptionPane.showMessageDialog(null, "Borrowed: " + book);
            } else {
                JOptionPane.showMessageDialog(null, "Book not available.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error borrowing book: " + e.getMessage());
        }
    }

    public void returnBook(Librarian librarian, String book) {
        try (Connection conn = DBConnection.getConnection()) {
            if (myBooks.containsValue(book)) {
                int id = myBooks.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(book))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(-1);

                String sql = "UPDATE books SET issued = 0 WHERE title = ? AND id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, book);
                stmt.setInt(2, id);
                stmt.executeUpdate();

                String deleteSql = "UPDATE issued_books SET return_date=CURDATE(), returned =1 WHERE book_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, id);
                deleteStmt.executeUpdate();

                myBooks.remove(id);

                String getfineSql = "SELECT issue_date, due_date, return_date FROM issued_books WHERE book_id=? ORDER BY return_date DESC LIMIT 1";
                PreparedStatement fineStmt = conn.prepareStatement(getfineSql);
                fineStmt.setInt(1, id);
                ResultSet rs = fineStmt.executeQuery();

                rs.next();
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                LocalDate returnDate = rs.getDate("return_date").toLocalDate();

                long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                int fine = (overdueDays > 0) ? (int) overdueDays * 15 : 0;

                Studentfine += fine;

                String finestmt = "UPDATE issued_books SET fine = ? WHERE book_id = ?";
                PreparedStatement finesql = conn.prepareStatement(finestmt);
                finesql.setDouble(1, fine);
                finesql.setInt(2, id);
                finesql.executeUpdate();

                notifications.add("Fine: " + fine);
                JOptionPane.showMessageDialog(null, "Fine: ₹" + fine);
                notifications.add("Returned: " + book);
                JOptionPane.showMessageDialog(null, "Returned: " + book);
            } else {
                JOptionPane.showMessageDialog(null, "You have not borrowed this book.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error returning book: " + e.getMessage());
        }
    }

    public void viewStatus() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                "SELECT b.title, i.issue_date, i.due_date FROM issued_books i " +
                "JOIN books b ON i.book_id = b.id WHERE i.student_id = ? AND i.returned = 0"
            );
            pst.setInt(1, this.id);
            ResultSet rs = pst.executeQuery();

            StringBuilder status = new StringBuilder("Your Borrowing Status:\n");
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                String title = rs.getString("title");
                LocalDate issueDate = rs.getDate("issue_date").toLocalDate();
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();

                long daysLeft = ChronoUnit.DAYS.between(today, dueDate);
                String timeLeft = (daysLeft >= 0)
                        ? daysLeft + " days left"
                        : Math.abs(daysLeft) + " days overdue";

                status.append("Book: ").append(title)
                      .append(", Issue Date: ").append(issueDate)
                      .append(", Due Date: ").append(dueDate)
                      .append(" (").append(timeLeft).append(")").append("\n");
            }

            JOptionPane.showMessageDialog(null, status.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching status: " + e.getMessage());
        }
    }

    public void viewNotifications() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                "SELECT b.title, i.due_date FROM issued_books i " +
                "JOIN books b ON i.book_id = b.id " +
                "WHERE i.student_id = ? AND i.returned = 0"
            );
            pst.setInt(1, this.id);
            ResultSet rs = pst.executeQuery();

            LocalDate today = LocalDate.now();

            while (rs.next()) {
                String title = rs.getString("title");
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                long daysLeft = ChronoUnit.DAYS.between(today, dueDate);

                if (daysLeft == 1) {
                    notifications.add("Reminder: '" + title + "' is due tomorrow!");
                } else if (daysLeft == 0) {
                    notifications.add("Reminder: '" + title + "' is due today!");
                }
            }

            JOptionPane.showMessageDialog(null, "Notifications:\n" + String.join("\n", notifications));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching notifications: " + e.getMessage());
        }
    }

    public void requestBook(String title) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                "INSERT INTO book_requests (student_id, book_title, request_date) VALUES (?, ?, NOW())"
            );
            pst.setInt(1, this.id);
            pst.setString(2, title);
            pst.executeUpdate();

            notifications.add("Requested new book: " + title);
            JOptionPane.showMessageDialog(null, "Request sent to librarian.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error requesting book: " + e.getMessage());
        }
    }

    public void reissueBook(String title) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT i.book_id, i.due_date FROM issued_books i " +
                "JOIN books b ON i.book_id = b.id " +
                "WHERE i.student_id = ? AND b.title = ? AND i.returned = 0 LIMIT 1"
            );
            checkStmt.setInt(1, this.id);
            checkStmt.setString(2, title);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                LocalDate today = LocalDate.now();

                if (today.isAfter(dueDate)) {
                    JOptionPane.showMessageDialog(null, "Cannot reissue. Book is already overdue.");
                    return;
                }

                int bookId = rs.getInt("book_id");
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE issued_books SET due_date = DATE_ADD(due_date, INTERVAL 7 DAY) WHERE book_id = ?"
                );
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();

                notifications.add("Reissued: " + title);
                JOptionPane.showMessageDialog(null, "Book reissued successfully for another 7 days.");
            } else {
                JOptionPane.showMessageDialog(null, "No matching issued book found for reissue.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error reissuing book: " + e.getMessage());
        }
    }

    public void holdBook(String title) {
        notifications.add("Requested hold for one week: " + title);
        JOptionPane.showMessageDialog(null, "Hold request submitted.");
    }
}
