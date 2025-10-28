import javax.swing.*;
import java.awt.*;

public class GameOverExplosion {
    private Image[] frames;
    private int frameIndex = 0;
    private int frameDelay = 0;
    private boolean finished = false;
    private int width, height;

    public GameOverExplosion(int width, int height) {
        this.width = width;
        this.height = height;

        // โหลดเฟรมภาพระเบิดเต็มจอ (ขนาด 1536x1024)
        frames = new Image[]{
                new ImageIcon("assets/explosion_full1.png").getImage(),
                new ImageIcon("assets/explosion_full2.png").getImage(),
                new ImageIcon("assets/explosion_full3.png").getImage(),
                new ImageIcon("assets/explosion_full4.png").getImage(),
                new ImageIcon("assets/explosion_full5.png").getImage(),
                new ImageIcon("assets/explosion_full6.png").getImage(),
        };
    }

    public void update() {
        if (finished) return;
        frameDelay++;
        if (frameDelay > 6) { // ปรับความเร็วเฟรม
            frameDelay = 0;
            frameIndex++;
            if (frameIndex >= frames.length) {
                frameIndex = frames.length - 1;
                finished = true;
            }
        }
    }

    public void draw(Graphics g) {
        if (!finished && frames[frameIndex] != null) {
            g.drawImage(frames[frameIndex], 0, 0, width, height, null);
        }
    }

    public boolean isFinished() { return finished; }
}
