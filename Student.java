
import java.util.ArrayList;
import javax.swing.*;

public class Student {

    ArrayList<String> myBooks = new ArrayList<>();
    ArrayList<String> notifications = new ArrayList<>();

    public void borrowBook(Librarian librarian, String book) {
        // Placeholder logic
        myBooks.add(book);
        notifications.add("Borrowed: " + book);
        JOptionPane.showMessageDialog(null, "Borrowed: " + book);
    }

    public void returnBook(Librarian librarian, String book) {
        if (myBooks.remove(book)) {
            notifications.add("Returned: " + book);
            JOptionPane.showMessageDialog(null, "Returned: " + book);
        } else {
            JOptionPane.showMessageDialog(null, "You haven't borrowed this book.");
        }
    }

    public void viewStatus() {
        JOptionPane.showMessageDialog(null, "Borrowed: " + String.join(", ", myBooks));
    }

    public void requestBook(String title) {
        notifications.add("Requested new book: " + title);
        JOptionPane.showMessageDialog(null, "Request sent to librarian.");
    }

    public void viewNotifications() {
        JOptionPane.showMessageDialog(null, String.join("\n", notifications));
    }

    public void reissueBook(String title) {
        notifications.add("Requested reissue for: " + title);
        JOptionPane.showMessageDialog(null, "Reissue request sent.");
    }

    public void holdBook(String title) {
        notifications.add("Requested hold for one week: " + title);
        JOptionPane.showMessageDialog(null, "Hold request submitted.");
    }

}
