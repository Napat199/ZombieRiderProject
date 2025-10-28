import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private Timer timer;
    private final int TARGET_DELAY = 30;
    private JFrame parentFrame;

    private Player player;
    private Background background1, background2, background3, background4;
    private Background currentBackground;

    private ArrayList<Zombie> zombies;
    private ArrayList<AmmoBox> ammoBoxes;
    private ArrayList<Shop> shops;
    private ArrayList<ZombieDeathEffect> deathEffects;
    private ArrayList<BloodEffect> bloodEffects;
    private ArrayList<Spark> sparks;
    private ArrayList<FlyingZombie> flyingZombies;

    private Random random = new Random();

    private boolean rightPressed = false;
    private int baseSpeed = 2;
    private int boostSpeed = 5;
    private int gameSpeed = 2;

    private double distance = 0.0;
    private int stage = 1;
    private final int maxStage = 4;
    private boolean isTransitioning = false;
    private boolean sceneChanged = false;

    private Image[] transitionFrames;
    private int transitionIndex = 0;
    private int transitionFrameDelay = 0;

    private int mouseX = 0, mouseY = 0;
    private Image crosshair;
    private int score = 0;
    private int ammo = 10;

    private int lives = 3;
    private Image heartFull, heartEmpty;

    private double stamina = 100;
    private double maxStamina = 100;
    private double staminaDrainRate = 1.4;
    private double staminaRecoverRate = 0.6;
    private boolean staminaCooldown = false;
    private boolean isImmortal = false;

    private boolean inShop = false;
    private Image shopUI;
    private Rectangle buyAmmoBtn = new Rectangle();
    private Rectangle upgradeBoostBtn = new Rectangle();
    private Rectangle upgradeStaminaBtn = new Rectangle();
    private Rectangle buyHealBtn = new Rectangle();
    private Rectangle exitShopBtn = new Rectangle();

    private int nextShopDistance = 1500;
    private boolean shopActive = false;
    private boolean shopCooldown = false;
    private int shopCooldownTicks = 0;

    private Boss boss = null;
    private boolean bossActive = false;
    private double nextBossDistance = 3000;

    private Cursor blankCursor;
    private Cursor defaultCursor;

    private Image[] explosionFrames;
    private int explosionIndex = 0;
    private int explosionFrameDelay = 0;
    private boolean explosionActive = false;
    private int explosionHoldTicksAfterEnd = 10;
    private int explosionEndHoldCounter = 0;
    private boolean isGameOver = false;
    private boolean isReturningToMenu = false;

    private final int ZOMBIE_BASE_SPAWN_CHANCE = 3;
    private final int AMMOBOX_SPAWN_CHANCE = 2;
    private final int BOSS_DISTANCE_GAP = 3000;

    private double bestDistance = 0.0;
    private final String SAVE_FILE = "best_distance.dat";


    public GamePanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setFocusable(true);
        setPreferredSize(new Dimension(1536, 1024));
        setBackground(Color.BLACK);

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        initGameObjects();
        flyingZombies = new ArrayList<>();
        loadAssets();
        loadBestDistance();


        timer = new Timer(TARGET_DELAY, this);
    }

    private void initGameObjects() {
        player = new Player(50, 750);
        background1 = new Background("assets/bg_stage1.png");
        background2 = new Background("assets/bg_stage2.png");
        background3 = new Background("assets/bg_stage3.png");
        background4 = new Background("assets/bg_stage4.png");
        currentBackground = background1;

        zombies = new ArrayList<>();
        ammoBoxes = new ArrayList<>();
        shops = new ArrayList<>();
        deathEffects = new ArrayList<>();
        bloodEffects = new ArrayList<>();
        sparks = new ArrayList<>();
    }

    private void loadAssets() {
        crosshair = safeLoad("assets/crosshair.png");
        shopUI = safeLoad("assets/shop_ui.png");
        heartFull = safeLoad("assets/heart_full.png");
        heartEmpty = safeLoad("assets/heart_empty.png");

        transitionFrames = new Image[5];
        for (int i = 0; i < transitionFrames.length; i++) {
            transitionFrames[i] = safeLoad("assets/transition_" + (i+1) + ".png");
        }

        explosionFrames = new Image[6];
        for (int i = 0; i < explosionFrames.length; i++) {
            explosionFrames[i] = safeLoad("assets/explosion" + (i+1) + ".png");
        }

        Image blank = safeLoad("assets/blank_cursor.png");
        if (blank != null) {
            blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blank, new Point(0,0), "blank");
            setCursor(blankCursor);
        } else {
            blankCursor = Cursor.getDefaultCursor();
        }
        defaultCursor = Cursor.getDefaultCursor();
    }

    private Image safeLoad(String path) {
        try {
            return new ImageIcon(path).getImage();
        } catch (Exception ex) {
            System.err.println("[GamePanel] load failed: " + path + " => " + ex.getMessage());
            return null;
        }
    }

    public void startGame() {
        timer.start();
    }

    public void stopGame() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isReturningToMenu) {
            return;
        }

        if (explosionActive) {
            updateExplosion();
            repaint();
            return;
        }

        if (isGameOver) {
            repaint();
            return;
        }

        if (shopCooldown) {
            shopCooldownTicks++;
            if (shopCooldownTicks > 120) {
                shopCooldown = false;
                shopCooldownTicks = 0;
            }
        }

        if (inShop) {
            repaint();
            return;
        }

        handleStaminaAndWheelie();

        distance += gameSpeed * 0.5;
        player.update();
        if (currentBackground != null) currentBackground.update(gameSpeed);

        if (!bossActive && distance >= nextBossDistance) {
            spawnBoss();
        }

        handleStageTransition();
        spawnEntities();

        updateZombies();
        updateAmmoBoxes();
        updateShops();
        updateEffects();
        updateSparks();
        updateBloodEffects();
        updateBoss();
        updateFlyingZombies();

        checkCollisions();
        repaint();
    }

    private void handleStaminaAndWheelie() {
        if (rightPressed && stamina > 0 && !staminaCooldown) {
            gameSpeed = boostSpeed;
            stamina -= staminaDrainRate;
            isImmortal = true;
            if (random.nextInt(4) == 0) {
                sparks.add(new Spark(player.getX()+60, player.getY()+80));
            }
            if (stamina <= 0) {
                stamina = 0;
                staminaCooldown = true;
                rightPressed = false;
                isImmortal = false;
                player.stopWheelie();
            }
        } else {
            gameSpeed = baseSpeed;
            isImmortal = false;
            if (stamina < maxStamina) {
                stamina += staminaRecoverRate;
                if (stamina > maxStamina) {
                    stamina = maxStamina;
                    staminaCooldown = false;
                }
            }
        }
    }

    private void spawnEntities() {
        int zombieSpawnChance = ZOMBIE_BASE_SPAWN_CHANCE + (stage - 1) * 2;
        if (!shopActive && !isTransitioning && random.nextInt(100) < zombieSpawnChance) {
            zombies.add(new Zombie(1600, 900));
        }

        if (!shopActive && !isTransitioning && random.nextInt(1000) < AMMOBOX_SPAWN_CHANCE) {
            ammoBoxes.add(new AmmoBox(1600, 900));
        }

        if (!shopActive && !isTransitioning && distance >= nextShopDistance) {
            shops.add(new Shop(1600, 530));
            shopActive = true;
            nextShopDistance += getShopDistanceGap();
        }
        if (!shopActive && !isTransitioning && random.nextInt(400) < 1 + stage) {
            int spawnY = 300 + random.nextInt(300); // สุ่มตำแหน่ง y ระหว่างกลางจอ
            flyingZombies.add(new FlyingZombie(1600, spawnY));
        }
    }

    private void spawnBoss() {
        boss = new Boss(1600, 650, stage, this);
        bossActive = true;
        nextBossDistance += BOSS_DISTANCE_GAP;
        System.out.println("[GamePanel] Boss spawned at distance: " + (int)distance + " (stage " + stage + ")");
    }
    private int getShopDistanceGap() {
        if (stage <= 1) return 2000; // Stage 1
        return 4000 + (stage - 2) * 1000; // Stage 2 = 4000, Stage 3 = 5000, Stage 4 = 6000 ...
    }

    private void updateZombies() {
        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            if (z == null) continue;
            z.update();
            if (z.getX() < -150) {
                zombies.remove(i--);
            }
        }
    }

    private void updateAmmoBoxes() {
        for (int i = 0; i < ammoBoxes.size(); i++) {
            AmmoBox b = ammoBoxes.get(i);
            if (b == null) continue;
            b.update();
            if (b.getX() < -150) {
                ammoBoxes.remove(i--);
            }
        }
    }

    private void updateShops() {
        for (int i = 0; i < shops.size(); i++) {
            Shop s = shops.get(i);
            if (s == null) continue;
            s.update();
            if (s.getX() < -500) {
                shops.remove(i--);
                shopActive = false;
            }
        }
    }

    private void updateEffects() {
        for (int i = 0; i < deathEffects.size(); i++) {
            ZombieDeathEffect fx = deathEffects.get(i);
            fx.update();
            if (fx.isFinished()) {
                deathEffects.remove(i--);
            }
        }
    }

    private void updateBloodEffects() {
        for (int i = 0; i < bloodEffects.size(); i++) {
            BloodEffect bf = bloodEffects.get(i);
            bf.update();
            if (bf.isFinished()) {
                bloodEffects.remove(i--);
            }
        }
    }

    private void updateSparks() {
        for (int i = 0; i < sparks.size(); i++) {
            Spark s = sparks.get(i);
            s.update();
            if (s.isFinished()) {
                sparks.remove(i--);
            }
        }
    }

    private void updateBoss() {
        if (boss != null) {
            boss.update(player.getX());
            if (!boss.isAlive()) {
                try {
                    boolean deathDone = false;
                    try {
                        java.lang.reflect.Method m = boss.getClass().getMethod("isDeathDone");
                        Object ret = m.invoke(boss);
                        if (ret instanceof Boolean) deathDone = (Boolean) ret;
                    } catch (NoSuchMethodException ignored) {
                        deathDone = true;
                    }
                    if (deathDone) {
                        deathEffects.add(new ZombieDeathEffect(boss.getX(), boss.getY()));
                        score += 50;
                        boss = null;
                        bossActive = false;
                    }
                } catch (Exception ex) {
                    deathEffects.add(new ZombieDeathEffect(boss.getX(), boss.getY()));
                    score += 50;
                    boss = null;
                    bossActive = false;
                }
            }
        }
    }

    private void handleStageTransition() {
        if (!isTransitioning && stage < maxStage && distance >= 2500 * stage) {
            isTransitioning = true;
            transitionIndex = 0;
            transitionFrameDelay = 0;
            sceneChanged = false;
        }

        if (isTransitioning) {
            transitionFrameDelay++;
            if (transitionFrameDelay >= 8) {
                transitionFrameDelay = 0;
                transitionIndex++;
                if (!sceneChanged && transitionIndex >= transitionFrames.length/2) {
                    stage++;
                    switch (stage) {
                        case 2 -> currentBackground = background2;
                        case 3 -> currentBackground = background3;
                        case 4 -> currentBackground = background4;
                        default -> {}
                    }
                    sceneChanged = true;
                }
                if (transitionIndex >= transitionFrames.length) {
                    isTransitioning = false;
                    transitionIndex = 0;
                }
            }
        }
    }

    private void checkCollisions() {
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        if (boss != null && boss.isAlive()) {
            Rectangle br = boss.getBounds();
            if (playerRect.intersects(br) && !isImmortal) {
                bossAttackPlayer();
            }
        }

        if (isImmortal) {
            for (int i = 0; i < zombies.size(); i++) {
                Zombie z = zombies.get(i);
                Rectangle zr = new Rectangle(z.getX(), z.getY(), z.getWidth(), z.getHeight());
                if (playerRect.intersects(zr)) {
                    bloodEffects.add(new BloodEffect(z.getX(), z.getY()));
                    deathEffects.add(new ZombieDeathEffect(z.getX()+z.getWidth()/2, z.getY()+z.getHeight()/2));
                    zombies.remove(i--);
                    score++;
                }
            }
        } else {
            for (int i = 0; i < zombies.size(); i++) {
                Zombie z = zombies.get(i);
                Rectangle zr = new Rectangle(z.getX(), z.getY(), z.getWidth(), z.getHeight());
                if (playerRect.intersects(zr)) {
                    zombies.remove(i--);
                    lives--;
                    bloodEffects.add(new BloodEffect(z.getX(), z.getY()));
                    if (lives <= 0) {
                        triggerExplosionFullscreen();
                    }if (distance > bestDistance) {
                        bestDistance = distance;
                        saveBestDistance();
                        System.out.println("[GamePanel] 🏁 New Best Distance: " + bestDistance + " m");
                    }
                    break;
                }
            }
        }

        for (int i = 0; i < ammoBoxes.size(); i++) {
            AmmoBox b = ammoBoxes.get(i);
            Rectangle br = new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            if (playerRect.intersects(br)) {
                ammo += 1 + random.nextInt(10);
                ammoBoxes.remove(i--);
            }
        }

        for (Shop s : shops) {
            if (!shopCooldown && playerRect.intersects(s.getBounds())) {
                inShop = true;
                rightPressed = false;
                isImmortal = false;
                setCursor(defaultCursor);
                break;
            }
        }
        for (int i = 0; i < flyingZombies.size(); i++) {
            FlyingZombie fz = flyingZombies.get(i);
            Rectangle fr = new Rectangle((int)fz.getX(), (int)fz.getY(), fz.getWidth(), fz.getHeight());
            if (playerRect.intersects(fr)) {
                flyingZombies.remove(i--);
                lives--;
                bloodEffects.add(new BloodEffect((int)fz.getX(), (int)fz.getY()));
                if (lives <= 0) triggerExplosionFullscreen();
                break;
            }
        }

    }

    public void bossAttackPlayer() {
        if (!isImmortal) {
            lives--;
            bloodEffects.add(new BloodEffect(player.getX(), player.getY()));
            if (lives <= 0) {
                triggerExplosionFullscreen();
            }
        }
    }

    private void triggerExplosionFullscreen() {
        if (explosionActive || isReturningToMenu) return;
        explosionActive = true;
        isGameOver = true;
        explosionIndex = 0;
        explosionFrameDelay = 0;
        explosionEndHoldCounter = 0;
        System.out.println("[GamePanel] 💥 Explosion triggered (Game Over).");
    }

    private void updateExplosion() {
        explosionFrameDelay++;
        if (explosionFrameDelay >= 5) {
            explosionFrameDelay = 0;
            explosionIndex++;

            if (explosionIndex >= explosionFrames.length) {
                explosionIndex = explosionFrames.length - 1;
                explosionEndHoldCounter++;

                if (explosionEndHoldCounter >= explosionHoldTicksAfterEnd) {
                    returnToStartMenu();
                }
            }
        }
    }

    private void returnToStartMenu() {
        if (isReturningToMenu) return;

        isReturningToMenu = true;
        timer.stop();

        System.out.println("[GamePanel] 🔙 Returning to StartMenu...");

        SwingUtilities.invokeLater(() -> {
            try {
                if (parentFrame != null) {
                    StartMenu startMenu = new StartMenu(parentFrame);
                    parentFrame.setContentPane(startMenu);
                    parentFrame.revalidate();
                    parentFrame.repaint();
                    startMenu.requestFocusInWindow();

                    System.out.println("[GamePanel] ✅ Successfully returned to StartMenu!");
                } else {
                    System.err.println("[GamePanel] ❌ parentFrame is null!");
                }
            } catch (Exception ex) {
                System.err.println("[GamePanel] ❌ Failed to switch to StartMenu: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
    // ✅ โหลดระยะทางสูงสุดจากไฟล์
    private void loadBestDistance() {
        try {
            java.io.File file = new java.io.File(SAVE_FILE);
            if (file.exists()) {
                java.util.Scanner sc = new java.util.Scanner(file);
                if (sc.hasNextDouble()) {
                    bestDistance = sc.nextDouble();
                }
                sc.close();
            }
        } catch (Exception e) {
            System.err.println("[GamePanel] Failed to load best distance: " + e.getMessage());
        }
    }

    // ✅ บันทึกระยะทางสูงสุดลงไฟล์
    private void saveBestDistance() {
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(SAVE_FILE);
            pw.println(bestDistance);
            pw.close();
        } catch (Exception e) {
            System.err.println("[GamePanel] Failed to save best distance: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentBackground != null) currentBackground.draw(g);

        for (Shop s : shops) if (s != null) s.draw(g);
        for (AmmoBox b : ammoBoxes) if (b != null) b.draw(g);
        for (Zombie z : zombies) if (z != null) z.draw(g);

        if (player != null) player.draw(g);
        if (boss != null) boss.draw(g);

        for (BloodEffect bf : bloodEffects) if (bf != null) bf.draw(g);
        for (ZombieDeathEffect fx : deathEffects) if (fx != null) fx.draw(g);
        for (Spark sp : sparks) if (sp != null) sp.draw(g);

        for (FlyingZombie fz : flyingZombies) if (fz != null) fz.draw(g);


        drawHearts(g);
        drawHUD(g);
        drawStaminaBar(g);
        drawStageText(g);

        if (!inShop && crosshair != null) {
            int cx = mouseX - crosshair.getWidth(null)/2;
            int cy = mouseY - crosshair.getHeight(null)/2;
            g.drawImage(crosshair, cx, cy, null);
        }

        if (isTransitioning && transitionFrames != null && transitionFrames.length > 0) {
            int idx = Math.max(0, Math.min(transitionIndex, transitionFrames.length-1));
            Image tf = transitionFrames[idx];
            if (tf != null) g.drawImage(tf, 0, 0, getWidth(), getHeight(), null);
        }

        if (inShop) drawShopUI(g);

        if (explosionActive) {
            int idx = Math.max(0, Math.min(explosionIndex, explosionFrames.length-1));
            Image ef = explosionFrames[idx];
            if (ef != null) {
                g.drawImage(ef, 0, 0, getWidth(), getHeight(), null);
            } else {
                g.setColor(new Color(255, 220, 160, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            if (explosionEndHoldCounter > explosionHoldTicksAfterEnd - 20) {
                g.setColor(new Color(255, 255, 255, 200));
                g.setFont(new Font("Arial", Font.BOLD, 32));
                String msg = "Returning to menu...";
                FontMetrics fm = g.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() - 100);
            }
        }

        if (isGameOver && !explosionActive) {
            g.setColor(Color.RED);
            g.setFont(new Font("Impact", Font.BOLD, 80));
            g.drawString("GAME OVER", getWidth()/2 - 260, getHeight()/2 - 20);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.setColor(Color.WHITE);
            g.drawString("Press R to Restart", getWidth()/2 - 170, getHeight()/2 + 40);
        }
    }

    private void drawHearts(Graphics g) {
        int x = 50, y = 170;
        for (int i = 0; i < 3; i++) {
            Image img = (i < lives) ? heartFull : heartEmpty;
            if (img != null) g.drawImage(img, x + i*48, y, 40, 40, null);
            else {
                g.setColor(i < lives ? Color.RED : Color.GRAY);
                g.fillRect(x + i*48, y, 36, 36);
            }
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Score: " + score, 50, 84);
        g.drawString(String.format("Distance: %.1f m", distance), 50, 124);
        g.drawString("Ammo: " + ammo, 50, 164);
    }

    private void drawStaminaBar(Graphics g) {
        g.setColor(new Color(50,50,50));
        g.fillRoundRect(50,214,220,22,10,10);

        g.setColor(staminaCooldown ? Color.RED : Color.CYAN);
        int sw = (int)(200 * (stamina / maxStamina));
        g.fillRoundRect(60,218,sw,14,10,10);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("Stamina", 270, 229);
    }

    private void drawStageText(Graphics g) {
        g.setFont(new Font("Impact", Font.BOLD, 48));
        String stageText = "STAGE " + stage;
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(stageText);
        int x = (getWidth() - width) / 2;
        g.setColor(new Color(0,0,0,100));
        g.drawString(stageText, x+3, 63);
        g.setColor(new Color(255,215,0));
        g.drawString(stageText, x, 60);
    }

    private void drawShopUI(Graphics g) {
        g.setColor(new Color(0,0,0,180));
        g.fillRect(0,0,getWidth(),getHeight());
        if (shopUI != null) g.drawImage(shopUI,0,0,getWidth(),getHeight(),null);

        int panelW = 820, panelH = 520;
        int panelX = (getWidth() - panelW) / 2;
        int panelY = (getHeight() - panelH) / 2;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(18,18,36,230));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 24,24);

        g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 24,24);

        g2.setFont(new Font("Impact", Font.BOLD, 52));
        g2.setColor(new Color(255,200,40));
        g2.drawString("GARAGE SHOP", panelX + 200, panelY + 80);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setColor(Color.WHITE);
        g2.drawString("Your Points: " + score, panelX + 560, panelY + 50);

        int btnY = panelY + 150;
        int gap = 90;

        drawButton(g2, buyAmmoBtn, "Buy Ammo (+10) - 10 pts", panelX + 100, btnY, 600, 70, new Color(70,150,255));
        drawButton(g2, upgradeBoostBtn, "Upgrade Boost (+2) - 15 pts", panelX + 100, btnY + gap, 600, 70, new Color(100,255,100));
        drawButton(g2, upgradeStaminaBtn, "Increase Max Stamina (+20) - 20 pts", panelX + 100, btnY + 2*gap, 600, 70, new Color(255,200,80));
        drawButton(g2, buyHealBtn, "Buy Heal (+1 Heart) - 20 pts", panelX + 100, btnY + 3*gap, 600, 70, new Color(255,100,150));
        drawButton(g2, exitShopBtn, "EXIT SHOP", panelX + 300, panelY + 4*gap + 130, 200, 60, new Color(255,80,80));
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text, int x, int y, int w, int h, Color color) {
        rect.setBounds(x,y,w,h);

        g2.setColor(new Color(0,0,0,100));
        g2.fillRoundRect(x+4,y+4,w,h,18,18);

        g2.setColor(color);
        g2.fillRoundRect(x,y,w,h,18,18);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x,y,w,h,18,18);

        g2.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int tx = x + (w - tw) / 2;
        int ty = y + ((h - fm.getHeight())/2) + fm.getAscent();

        g2.setColor(new Color(0,0,0,140));
        g2.drawString(text, tx+2, ty+2);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }
    private void updateFlyingZombies() {
        for (int i = 0; i < flyingZombies.size(); i++) {
            FlyingZombie fz = flyingZombies.get(i);
            if (fz == null) continue;
            fz.update(player.getX(), player.getY());
            if (fz.getX() < -150) {
                flyingZombies.remove(i--);
            }
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        // ตรวจ FlyingZombie ที่โดนยิง
        for (int i = 0; i < flyingZombies.size(); i++) {
            FlyingZombie fz = flyingZombies.get(i);

            // ✅ ปรับ hitbox ให้พอดีกลางภาพ
            int fx = fz.getX() - fz.getWidth() / 2;
            int fy = fz.getY() - fz.getHeight() / 2;
            Rectangle fr = new Rectangle(fx, fy, fz.getWidth(), fz.getHeight());

            // ถ้าเมาส์ยิงโดน
            if (fr.contains(mx, my)) {
                bloodEffects.add(new BloodEffect(fz.getX(), fz.getY()));
                deathEffects.add(new ZombieDeathEffect(fz.getX(), fz.getY()));
                flyingZombies.remove(i--); // ลบตัวที่โดนออก
                score++;
                break; // หยุด loop ถ้าโดนแล้ว
            }
        }

        if (inShop) {
            if (buyAmmoBtn.contains(mx,my) && score >= 10) {
                ammo += 10; score -= 10;
            } else if (upgradeBoostBtn.contains(mx,my) && score >= 15) {
                boostSpeed += 2; score -= 15;
            } else if (upgradeStaminaBtn.contains(mx,my) && score >= 20) {
                maxStamina += 20; stamina = maxStamina; score -= 20; staminaCooldown = false;
            } else if (buyHealBtn.contains(mx,my) && score >= 20) {
                if (lives < 3) { lives++; score -= 20; }
            } else if (exitShopBtn.contains(mx,my)) {
                inShop = false;
                shopCooldown = true;
                setCursor(blankCursor);
                shopActive = false;
            }
            repaint();
            return;
        }

        if (isGameOver || explosionActive) return;
        if (ammo <= 0) return;
        ammo--;

        for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            Rectangle zr = new Rectangle(z.getX(), z.getY(), z.getWidth(), z.getHeight());
            if (zr.contains(mx,my)) {
                bloodEffects.add(new BloodEffect(z.getX(), z.getY()));
                deathEffects.add(new ZombieDeathEffect(z.getX()+z.getWidth()/2, z.getY()+z.getHeight()/2));
                if (random.nextInt(100) < 20) ammoBoxes.add(new AmmoBox(z.getX(), 900));
                zombies.remove(i--);
                score++;
                break;
            }
        }

        if (boss != null && boss.isAlive()) {
            Rectangle br = boss.getBounds();
            if (br.contains(mx,my)) {
                boss.takeDamage(25);
                bloodEffects.add(new BloodEffect(boss.getX(), boss.getY()));
            }
        }
        for (int i = 0; i < flyingZombies.size(); i++) {
            FlyingZombie fz = flyingZombies.get(i);
            Rectangle fr = new Rectangle(fz.getX(), fz.getY(), fz.getWidth(), fz.getHeight());
            if (fr.contains(mx, my)) {
                bloodEffects.add(new BloodEffect(fz.getX(), fz.getY()));
                deathEffects.add(new ZombieDeathEffect(fz.getX() + fz.getWidth() / 2, fz.getY() + fz.getHeight() / 2));
                flyingZombies.remove(i--);
                score++;
                break;
            }
        }

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && !inShop && stamina > 0 && !staminaCooldown) {
            rightPressed = true;
            player.startWheelie();
        }

        if (isGameOver && e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = false;
            player.stopWheelie();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        isGameOver = false;
        explosionActive = false;
        explosionIndex = 0;
        explosionFrameDelay = 0;
        explosionEndHoldCounter = 0;
        isReturningToMenu = false;

        distance = 0;
        stage = 1;
        score = 0;
        ammo = 10;
        lives = 3;

        stamina = maxStamina = 100;
        staminaCooldown = false;
        isImmortal = false;

        zombies.clear();
        ammoBoxes.clear();
        shops.clear();
        deathEffects.clear();
        bloodEffects.clear();
        sparks.clear();

        nextShopDistance = 1500;
        nextBossDistance = 2600;
        bossActive = false;
        boss = null;

        inShop = false;
        shopActive = false;
        shopCooldown = false;

        isTransitioning = false;
        sceneChanged = false;
        transitionIndex = 0;
        transitionFrameDelay = 0;

        currentBackground = background1;
        setCursor(blankCursor);
        timer.start();
    }

    public void setBaseSpeed(int s) { this.baseSpeed = s; }
    public void setBoostSpeed(int s) { this.boostSpeed = s; }
    public void setStage(int st) { this.stage = st; }
    public int getStage() { return stage; }
    public double getDistance() { return distance; }
}