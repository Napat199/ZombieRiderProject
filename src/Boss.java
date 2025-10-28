import javax.swing.*;
import java.awt.*;

/**
 * Boss.java (Full Fixed Version)
 * - เดินเข้าหาผู้เล่น (moveTowardsPlayer)
 * - มีสถานะ RUN / ATTACK / HURT / DEAD
 * - เล่นอนิเมชันทุกสถานะครบ
 * - โดนยิงแล้วสั่น / ตายแล้วเล่นอนิเมชันตายครบ
 * - โจมตีเมื่อเข้าใกล้ผู้เล่น เรียก bossAttackPlayer() จาก GamePanel
 */
public class Boss {

    private int x, y, width, height;
    private int health, maxHealth;
    private int speed = 7;
    private boolean alive = true;
    private boolean finishedDeathAnim = false;

    private enum State { RUN, ATTACK, HURT, DEAD }
    private State currentState = State.RUN;

    private int frameTimer = 0;
    private int frameIndex = 0;
    private int attackTimer = 0;
    private int hurtTimer = 0;
    private int deadTimer = 0;

    private Image[] runFrames;
    private Image[] attackFrames;
    private Image hurtFrame;
    private Image[] deadFrames;

    private GamePanel game;

    public Boss(int x, int y, int stage, GamePanel game) {
        this.x = x;
        this.y = y;
        this.width = 256;
        this.height = 256;
        this.maxHealth = 100 + (stage * 50);
        this.health = maxHealth;
        this.game = game;

        runFrames = new Image[]{
                new ImageIcon("assets/ZombieBoss_run2.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run3.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run4.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run5.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run6.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run7.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run8.png").getImage(),
                new ImageIcon("assets/ZombieBoss_run9.png").getImage()
        };



        hurtFrame = new ImageIcon("assets/ZombieBoss_hurt.png").getImage();

        deadFrames = new Image[]{
                new ImageIcon("assets/ZombieBoss_dead1.png").getImage(),
                new ImageIcon("assets/ZombieBoss_dead2.png").getImage(),
                new ImageIcon("assets/ZombieBoss_dead3.png").getImage(),
                new ImageIcon("assets/ZombieBoss_dead4.png").getImage(),
                new ImageIcon("assets/ZombieBoss_dead5.png").getImage(),
                new ImageIcon("assets/ZombieBoss_dead6.png").getImage()
        };
    }

    public void update(int playerX) {
        if (!alive && finishedDeathAnim) return;

        switch (currentState) {
            case RUN -> updateRun(playerX);
            case ATTACK -> updateAttack(playerX);
            case HURT -> updateHurt();
            case DEAD -> updateDead();
        }
    }

    private void updateRun(int playerX) {
        moveTowardsPlayer(playerX);

        frameTimer++;
        if (frameTimer % 8 == 0) {
            frameIndex = (frameIndex + 1) % runFrames.length;
        }

        if (Math.abs(x - playerX) < 150) {
            currentState = State.ATTACK;
            frameIndex = 0;
            attackTimer = 0;
        }
    }

    private void moveTowardsPlayer(int playerX) {
        if (x > playerX + 100) {
            x -= speed;
        } else if (x < playerX - 100) {
            x += speed;
        }
    }

    private void updateAttack(int playerX) {
        frameTimer++;
        if (frameTimer % 6 == 0) {
            frameIndex = (frameIndex + 1) % attackFrames.length;
        }
        attackTimer++;

        if (attackTimer == 20) {
            game.bossAttackPlayer(); // ทำดาเมจเมื่อถึงจังหวะตี
        }

        if (attackTimer > 45) {
            currentState = State.RUN;
            frameIndex = 0;
            attackTimer = 0;
        }
    }

    private void updateHurt() {
        hurtTimer++;
        if (hurtTimer > 15) {
            hurtTimer = 0;
            currentState = State.RUN;
        }
    }

    private void updateDead() {
        deadTimer++;
        if (deadTimer % 10 == 0 && frameIndex < deadFrames.length - 1) {
            frameIndex++;
        } else if (frameIndex >= deadFrames.length - 1) {
            finishedDeathAnim = true;
        }
    }

    public void draw(Graphics g) {
        Image currentImage = null;
        switch (currentState) {
            case RUN -> currentImage = runFrames[frameIndex];
            case HURT -> currentImage = hurtFrame;
            case DEAD -> currentImage = deadFrames[Math.min(frameIndex, deadFrames.length - 1)];
        }

        g.drawImage(currentImage, x, y, width, height, null);

        // แถบเลือด
        if (alive) {
            int barWidth = 200;
            int barHeight = 15;
            int barX = x + (width / 2) - (barWidth / 2);
            int barY = y - 20;
            double hpPercent = (double) health / maxHealth;

            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barWidth, barHeight);
            g.setColor(Color.RED);
            g.fillRect(barX, barY, (int) (barWidth * hpPercent), barHeight);
            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barWidth, barHeight);
        }
    }

    public void takeDamage(int dmg) {
        if (!alive) return;

        health -= dmg;
        currentState = State.HURT;
        hurtTimer = 0;
        frameIndex = 0;

        if (health <= 0) {
            health = 0;
            alive = false;
            currentState = State.DEAD;
            frameIndex = 0;
            deadTimer = 0;
        }
    }

    public boolean isAlive() { return alive; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isDeathDone() {
        return currentState == State.DEAD && frameIndex >= deadFrames.length - 1;
    }
}