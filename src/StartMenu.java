import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StartMenu extends JPanel {
    private Image backgroundImage;
    private JFrame parentFrame; // ✅ เพิ่มตัวแปรนี้ไว้เก็บ reference ของ frame
    private double bestDistance = 0.0;
    private final String SAVE_FILE = "best_distance.dat";


    public StartMenu(JFrame frame) {
        this.parentFrame = frame; // ✅ เก็บค่า frame ที่ส่งเข้ามา
        loadBestDistance();

        // โหลดภาพพื้นหลังจากโฟลเดอร์ assets
        backgroundImage = new ImageIcon("assets/Menu_bg.png").getImage();

        // ตั้ง layout เป็น null เพื่อจัดปุ่มด้วยตำแหน่งเอง
        setLayout(null);

        // ปุ่ม Start
        JButton startButton = new JButton("START");
        startButton.setFont(new Font("Arial", Font.BOLD, 36));
        startButton.setBackground(new Color(50, 205, 50)); // เขียว
        startButton.setForeground(Color.BLACK);
        startButton.setFocusPainted(false);
        startButton.setBounds(650, 650, 250, 80); // x, y, width, height

        // ปุ่ม Exit
        JButton exitButton = new JButton("EXIT");
        exitButton.setFont(new Font("Arial", Font.BOLD, 36));
        exitButton.setBackground(new Color(220, 20, 60)); // แดง
        exitButton.setForeground(Color.BLACK);
        exitButton.setFocusPainted(false);
        exitButton.setBounds(650, 760, 250, 80);

        // เพิ่มปุ่มลงในหน้า
        add(startButton);
        add(exitButton);

        // เมื่อกด START → ไปหน้าเกม
        startButton.addActionListener(e -> {
            GamePanel gamePanel = new GamePanel(parentFrame);

            // ✅ ใช้ setContentPane() แทน add()
            parentFrame.setContentPane(gamePanel);

            // ✅ รีเฟรชทั้งหน้าจอ
            parentFrame.revalidate();
            parentFrame.repaint();

            // ✅ เริ่มเกม
            gamePanel.startGame();
            gamePanel.requestFocusInWindow();
        });

        // ปุ่ม EXIT
        exitButton.addActionListener(e -> System.exit(0));
    }
    private void loadBestDistance() {
        try {
            java.io.File file = new java.io.File(SAVE_FILE);
            if (file.exists()) {
                java.util.Scanner sc = new java.util.Scanner(file);
                if (sc.hasNextDouble()) {
                    bestDistance = sc.nextDouble();
                }
                sc.close();
            }
        } catch (Exception e) {
            System.err.println("[StartMenu] Failed to load best distance: " + e.getMessage());
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // วาดภาพพื้นหลังเต็มจอ
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.WHITE);

        String bestText = String.format("Best Distance: %.1f m", bestDistance);
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int textWidth = fm.stringWidth(bestText);
        g.drawString(bestText, (getWidth() - textWidth) / 2, 900); // ตำแหน่งกลางจอ (ปรับ y ได้ตามชอบ)

    }
}
