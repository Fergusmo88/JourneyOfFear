package game.player.attack;

import game.enemy.Enemy;
import game.sprite.Hittable;
import game.sprite.ImageMask;
import java.util.List;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;

public abstract class Attack {
    
    protected List<Enemy> enemies;
    
    protected Animation anim;
    
    protected int x = 0;
    protected int y = 0;
    
    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }
    
    public abstract ImageMask getMask(int x, int y);
    public abstract void init();
    public abstract void render(int x, int y);
    public abstract boolean canAttack();
    public abstract void attack(int direction, boolean sound);
    public abstract void update(int delta, int x, int y);
    public abstract void resolveAttackHit(Hittable other, int x, int y);
    public abstract void renderMask(int x, int y, Graphics g);
    public abstract void renderDebugInfo(int camX, int camY, Graphics g);
}