import java.awt.*;
import javax.swing.*;

public class BloodEffect {
    private int x, y;
    private int frame = 0;
    private int delay = 0;
    private boolean finished = false;
    private Image[] frames;

    public BloodEffect(int x, int y) {
        this.x = x;
        this.y = y;
        frames = new Image[5]; // จำนวนเฟรมเลือด
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new ImageIcon("assets/blood" + (i + 1) + ".png").getImage();
        }
    }

    public void update() {
        delay++;
        if (delay >= 4) { // หน่วงเวลาเปลี่ยนเฟรม
            delay = 0;
            frame++;
            if (frame >= frames.length) {
                finished = true;
            }
        }
    }

    public void draw(Graphics g) {
        if (!finished && frame < frames.length) {
            g.drawImage(frames[frame], x, y, 100, 100, null);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
