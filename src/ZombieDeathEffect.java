import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * เอฟเฟกต์กะทัดรัด: เป็น particle burst เมื่อซอมบี้ถูกฆ่า
 * แต่ละ particle จะมีตำแหน่ง ความเร็ว อายุ และจะค่อยๆ จางหาย
 */
public class ZombieDeathEffect {
    private static final int PARTICLE_COUNT = 14;
    private ArrayList<Particle> particles;
    private boolean finished = false;
    private Random rnd = new Random();

    private static class Particle {
        double x, y;
        double vx, vy;
        int life;
        Color color;
        double size;
        Particle(double x, double y, double vx, double vy, int life, Color color, double size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.life = life; this.color = color; this.size = size;
        }
    }

    public ZombieDeathEffect(int cx, int cy) {
        particles = new ArrayList<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = rnd.nextDouble() * Math.PI * 2;
            double speed = 2.5 + rnd.nextDouble() * 3.5;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed - (1.0 + rnd.nextDouble()*1.5); // ให้บางชิ้นเด้งขึ้น
            int life = 28 + rnd.nextInt(12);
            Color c = i % 3 == 0 ? new Color(255,160,0) : new Color(255,80,30);
            double size = 6 + rnd.nextDouble()*8;
            particles.add(new Particle(cx, cy, vx, vy, life, c, size));
        }
    }

    public void update() {
        finished = true;
        for (Particle p : particles) {
            if (p.life > 0) {
                finished = false;
                p.x += p.vx;
                p.y += p.vy;
                p.vy += 0.18; // gravity
                p.vx *= 0.99; // friction
                p.vy *= 0.995;
                p.life--;
            }
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (Particle p : particles) {
            if (p.life > 0) {
                float alpha = Math.max(0f, Math.min(1f, p.life / 40f));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setColor(p.color);
                int s = (int) Math.max(1, p.size * (alpha));
                g2.fillOval((int)(p.x - s/2.0), (int)(p.y - s/2.0), s, s);
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public boolean isFinished() {
        return finished;
    }
}
