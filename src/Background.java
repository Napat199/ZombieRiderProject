import java.awt.*;
import javax.swing.ImageIcon;

public class Background {
    private Image image;
    private int x1, x2;
    private int speed = 0;

    public Background(String path) {
        image = new ImageIcon(path).getImage();
        x1 = 0;
        x2 = image.getWidth(null);
    }

    public void update(int speed) {
        this.speed = speed;
        x1 -= speed;
        x2 -= speed;
        if (x1 + image.getWidth(null) <= 0) x1 = x2 + image.getWidth(null);
        if (x2 + image.getWidth(null) <= 0) x2 = x1 + image.getWidth(null);
    }

    public void draw(Graphics g) {
        g.drawImage(image, x1, 0, null);
        g.drawImage(image, x2, 0, null);
    }

}
