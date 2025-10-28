import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Zombie Rider - Endless Runner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1536, 1024);
        frame.setResizable(false);

        StartMenu menu = new StartMenu(frame);
        frame.add(menu);

        frame.setVisible(true);
    }
}
