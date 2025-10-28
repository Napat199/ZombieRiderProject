import java.awt.Image;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class Player {
    private int x, y;

    private Image[] normalFrames;      // 4 เฟรมปกติ
    private Image[] wheelieFrames;     // 8 เฟรมยกล้อ
    private Image[] wheelieHoldFrames; // 4 เฟรมสำหรับค้าง

    private int currentFrame = 0;
    private int frameCount = 0;
    private int frameDelay = 5;

    private enum State { NORMAL, WHEELIE_PLAY, WHEELIE_HOLD }
    private State state = State.NORMAL;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;

        // โหลด normalFrames
        normalFrames = new Image[4];
        for (int i = 0; i < 4; i++) {
            normalFrames[i] = new ImageIcon("assets/Player_" + (i + 1) + ".png").getImage();
        }

        // โหลด wheelieFrames
        wheelieFrames = new Image[8];
        for (int i = 0; i < 8; i++) {
            wheelieFrames[i] = new ImageIcon("assets/Player_" + (i + 5) + ".png").getImage();
        }

        // โหลด wheelieHoldFrames
        wheelieHoldFrames = new Image[4];
        for (int i = 0; i < 4; i++) {
            wheelieHoldFrames[i] = new ImageIcon("assets/Player_" + (i + 13) + ".png").getImage();
        }
    }

    public void update() {
        frameCount++;
        if (frameCount < frameDelay) return;
        frameCount = 0;

        switch (state) {
            case NORMAL:
                currentFrame = (currentFrame + 1) % normalFrames.length;
                break;

            case WHEELIE_PLAY:
                currentFrame++;
                if (currentFrame >= wheelieFrames.length) {
                    // ถึงสุดของยกล้อ → เปลี่ยนไป WHEELIE_HOLD
                    state = State.WHEELIE_HOLD;
                    currentFrame = 0; // เริ่มวนในเฟรม Hold
                }
                break;

            case WHEELIE_HOLD:
                currentFrame = (currentFrame + 1) % wheelieHoldFrames.length; // วน 4 เฟรม
                break;
        }
    }

    public void startWheelie() {
        if (state == State.NORMAL) {
            state = State.WHEELIE_PLAY;
            currentFrame = 0;
            frameCount = 0;
        }
    }

    public void stopWheelie() {
        state = State.NORMAL;
        currentFrame = 0;
        frameCount = 0;
    }

    public void draw(Graphics g) {
        Image img;
        switch (state) {
            case NORMAL:
                img = normalFrames[currentFrame];
                break;
            case WHEELIE_PLAY:
                img = wheelieFrames[currentFrame];
                break;
            case WHEELIE_HOLD:
                img = wheelieHoldFrames[currentFrame];
                break;
            default:
                img = normalFrames[0];
        }
        g.drawImage(img, x, y, null);
    }
    public int getWidth() {
        Image currentImage;
        switch (state) {
            case WHEELIE_PLAY:
                currentImage = wheelieFrames[currentFrame];
                break;
            case WHEELIE_HOLD:
                currentImage = wheelieHoldFrames[currentFrame];
                break;
            default:
                currentImage = normalFrames[currentFrame];
                break;
        }
        return currentImage.getWidth(null);
    }

    public int getHeight() {
        Image currentImage;
        switch (state) {
            case WHEELIE_PLAY:
                currentImage = wheelieFrames[currentFrame];
                break;
            case WHEELIE_HOLD:
                currentImage = wheelieHoldFrames[currentFrame];
                break;
            default:
                currentImage = normalFrames[currentFrame];
                break;
        }
        return currentImage.getHeight(null);
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
