import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class FlyingZombie {
    private double x, y;
    private double speed;
    private double dx, dy;
    private double angle;

    private Image[] frames;
    private int frameIndex = 0;
    private int frameDelay = 0;

    private Random rand = new Random();

    public FlyingZombie(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = 5 + rand.nextDouble() * 3;

        // โหลดภาพ animation 5 เฟรม
        frames = new Image[5];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new ImageIcon("assets/zombie_fly" + (i + 1) + ".png").getImage();
        }
    }

    public void update(double targetX, double targetY) {
        double diffX = targetX - x;
        double diffY = targetY - y;
        double dist = Math.sqrt(diffX * diffX + diffY * diffY);

        if (dist > 0) {
            dx = (diffX / dist) * speed;
            dy = (diffY / dist) * speed;
        }

        // บินตรงเข้าหาเป้าหมาย (ไม่สั่น)
        x += dx;
        y += dy;

        angle = Math.atan2(diffY, diffX);

        // Animation
        frameDelay++;
        if (frameDelay > 6) {
            frameDelay = 0;
            frameIndex = (frameIndex + 1) % frames.length;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Image frame = frames[frameIndex];
        if (frame == null) return;

        int w = frame.getWidth(null);
        int h = frame.getHeight(null);

        g2.translate(x, y);
        g2.rotate(angle);
        g2.drawImage(frame, -w / 2, -h / 2, w, h, null);
        g2.rotate(-angle);
        g2.translate(-x, -y);
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getWidth() { return frames[0] != null ? frames[0].getWidth(null) : 60; }
    public int getHeight() { return frames[0] != null ? frames[0].getHeight(null) : 40; }
}
