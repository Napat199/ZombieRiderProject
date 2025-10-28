import java.awt.*;
import java.util.Random;

public class Spark {
    private int x, y;
    private int life = 20;
    private double vx, vy;
    private Color color;
    private Random rand = new Random();

    public Spark(int x, int y) {
        this.x = x + rand.nextInt(10) - 5;
        this.y = y + rand.nextInt(10) - 5;
        vx = rand.nextDouble() * 2 - 1;
        vy = -rand.nextDouble() * 2;
        color = new Color(255, 200 + rand.nextInt(55), 0);
    }

    public void update() {
        x += vx;
        y += vy;
        vy += 0.1;
        life--;
    }

    public void draw(Graphics g) {
        if (life > 0) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.fillOval(x, y, 6, 6);
        }
    }

    public boolean isFinished() {
        return life <= 0;
    }
}
