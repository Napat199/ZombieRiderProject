import java.awt.*;
import javax.swing.ImageIcon;

public class AmmoBox {
    private int x, y;
    private int speedX = 5;
    private double speedY = -15;    // 🔥 เริ่มต้นด้วยแรงกระโดดขึ้น
    private double gravity = 1.2;   // แรงโน้มถ่วง (ยิ่งมากตกเร็ว)
    private boolean onGround = false;
    private Image image;

    private static final int GROUND_Y = 900;  // พื้นดิน

    public AmmoBox(int x, int y) {
        this.x = x;
        this.y = y;
        image = new ImageIcon("assets/ammo_box.png").getImage();
        if (image != null) this.y -= image.getHeight(null);
    }

    public void update() {
        // เคลื่อนซ้าย
        x -= speedX;

        // ฟิสิกส์แนวดิ่ง
        if (!onGround) {
            speedY += gravity;
            y += speedY;

            // ถ้าแตะพื้น → เด้งแรง ๆ ขึ้น
            if (y >= GROUND_Y - getHeight()) {
                y = GROUND_Y - getHeight();
                speedY = -speedY * 0.65; // 🔥 เด้งกลับแรงขึ้น
                if (Math.abs(speedY) < 2) { // ถ้าแรงเหลือน้อยมาก
                    onGround = true;
                    speedY = 0;
                }
            }
        }
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return image.getWidth(null); }
    public int getHeight() { return image.getHeight(null); }
}
