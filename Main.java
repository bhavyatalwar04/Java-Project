
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

public class Main implements ActionListener {

    JFrame frame;
    JButton loginButton;
    JButton signupButton;

    public static void main(String[] args) {
        Main mainApp = new Main();
        mainApp.createUI();
    }

    public void createUI() {
        frame = new JFrame();
        frame.setTitle("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(600, 600);
        frame.setLayout(null);
        frame.getContentPane().setBackground(new Color(254, 250, 100));

        ImageIcon image = new ImageIcon("Logo.png");
        Border border = BorderFactory.createLineBorder(Color.black, 3);
        JLabel imageLabel = new JLabel(image);
        imageLabel.setBounds(150, 150, 300, 300);
        imageLabel.setBorder(border);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        frame.add(imageLabel);

        JLabel textLabel = new JLabel("Welcome to our Library Management System");
        textLabel.setBounds(50, 30, 500, 50);
        textLabel.setFont(new Font("Arial", Font.BOLD, 20));
        textLabel.setForeground(Color.BLACK);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        frame.add(textLabel);

        loginButton = new JButton("Login");
        loginButton.setBounds(250, 470, 100, 40);
        loginButton.addActionListener(this);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(loginButton);

        // New Signup Button
        signupButton = new JButton("Signup");
        signupButton.setBounds(250, 520, 100, 40);
        signupButton.setFocusPainted(false);
        signupButton.setFont(new Font("Arial", Font.PLAIN, 16));
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SignupForm(); // Open the Signup form
            }
        });
        frame.add(signupButton);

        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            frame.dispose(); // Close the Welcome Screen
            Test.main(null); // Open the Login Screen
        }
    }
}
