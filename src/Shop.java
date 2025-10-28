import java.awt.*;
import javax.swing.ImageIcon;

public class Shop {
    private int x, y;
    private int speed = 8;
    private Image image;

    public Shop(int x, int y) {
        this.x = x;
        this.y = y;
        image = new ImageIcon("assets/shop_obj.png").getImage(); // ← รูปร้านค้าของคุณ
    }

    public void update() {
        x -= speed;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, image.getWidth(null), image.getHeight(null));
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
