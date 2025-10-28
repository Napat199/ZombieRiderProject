import java.awt.*;
import javax.swing.ImageIcon;
import java.util.Random;

public class Zombie {
    private int x, y;
    private int speed = 8;

    // 🔥 เก็บเฟรมแอนิเมชันของ Zombie
    private Image[] frames;
    private int currentFrame = 0;
    private int frameCount = 0;
    private int frameDelay = 6;

    // 🧟 สุ่มชนิดของซอมบี้
    private static final int TOTAL_TYPES = 4; // มีซอมบี้ทั้งหมด 4 แบบ
    private int zombieType;

    private Random random = new Random();

    public Zombie(int x, int y) {
        this.x = x;
        this.y = y;

        // 🎲 สุ่มชนิดซอมบี้ (1–4)
        zombieType = 1 + random.nextInt(TOTAL_TYPES);

        // ✅ โหลดเฟรมของซอมบี้ตัวนี้ (แต่ละแบบมี 5 เฟรม)
        int frameTotal = 5;
        frames = new Image[frameTotal];

        for (int i = 0; i < frameTotal; i++) {
            String path = "assets/zombie" + zombieType + "_" + (i + 1) + ".png";
            frames[i] = new ImageIcon(path).getImage();
        }

        // ปรับ y ให้อยู่พื้น
        if (frames[0] != null) {
            this.y -= frames[0].getHeight(null);
        }
    }

    // 🔁 อัปเดตตำแหน่ง + เฟรมภาพ
    public void update() {
        x -= speed;

        frameCount++;
        if (frameCount >= frameDelay) {
            frameCount = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    // 🧠 วาด Zombie
    public void draw(Graphics g) {
        g.drawImage(frames[currentFrame], x, y, null);
    }

    // 🧩 ใช้ตรวจโดนกระสุน / คลิก
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return frames[0].getWidth(null);
    }

    public int getHeight() {
        return frames[0].getHeight(null);
    }
}
