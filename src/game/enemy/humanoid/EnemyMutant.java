package game.enemy.humanoid;

import game.enemy.SmartEnemy;
import game.player.Player;
import game.player.attack.AttackAxeCleave;
import game.sprite.EntitySprite;
import game.state.StateMultiplayer;
import game.util.resource.AnimationLibrary;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class EnemyMutant extends SmartEnemy {
    
    protected static final int DIR_SWITCH_SPEED = 500;
    protected int dirChangeCounter = 0;
    
    protected int direction;
    
    public EnemyMutant(Player player) {
        super(player);
        hitDamage = 4;
        attack = AttackAxeCleave.create().setAttackRest(500);
        speed = 0.0625;
        health = 50;
        minimapColor = new Color(95,124,112);
    }
    
    @Override
    protected void initializeAttack() {
        attack.init();
    }
    
    @Override
    protected void resolveAttack(int delta) {
        if (attack.canAttack()) {
            direction = directionToPlayer()*2;
            attack.attack(direction,false);
        }
        attack.update(delta,x-halfWidth,y-halfHeight);
        resolveAttackCollision();
    }
    
    protected void resolveAttackCollision() {
        attack.resolveAttackHit(player,x,y);
    }
    
    @Override
    public void beSmart(int delta) {
        
    }
    
    @Override
    public void move(int delta) {
        if (stunTimer>0) {
            sprite.getAnim(spritePointer).setCurrentFrame(0);
            sprite.getAnim(spritePointer).stop();
            x+=(knockbackDX*stunTimer)/(KNOCKBACK_DISTANCE*KNOCKBACK_MULTIPLIER);
            y+=(knockbackDY*stunTimer)/(KNOCKBACK_DISTANCE*KNOCKBACK_MULTIPLIER);
            return;
        }
        
        if (dirChangeCounter<DIR_SWITCH_SPEED)
            dirChangeCounter += (int) (2*delta*Math.random());
        else
            updateSpritePointer(delta);
        
        sprite.getAnim(spritePointer).start();
        
        int dx = 0;
        int dy = 0;
        
        switch(spritePointer) {
            case 0:
                dx+=speed*delta;
                break;
            case 1:
                dy-=speed*delta;
                break;
            case 2:
                dx-=speed*delta;
                break;
            case 3:
                dy+=speed*delta;
                break;
        }
        
        x += dx;
        y += dy;
    }
    
    protected void updateSpritePointer(int delta) {
        dirChangeCounter = 0;
        if (Math.random()<0.1) {
            spritePointer = (int) (Math.random()*4);
            return;
        }
        spritePointer = directionToPlayer();
    }
    
    @Override
    protected void initializeSprite() {
        sprite = new EntitySprite(4);
        Animation[] animList = {
            AnimationLibrary.MUTANT_RIGHT.getAnim(),
            AnimationLibrary.MUTANT_UP.getAnim(),
            AnimationLibrary.MUTANT_LEFT.getAnim(),
            AnimationLibrary.MUTANT_DOWN.getAnim(),
        };
        sprite.setAnimations(animList);
        initializeMask();
    }
    
    @Override
    protected void renderAttack() {
        attack.render(x-halfWidth,y-halfHeight);
    }
    
    @Override
    protected void renderDebugInfo(Graphics g) {
        super.renderDebugInfo(g);
        attack.renderDebugInfo(x-halfWidth,y+74,g);
        if (StateMultiplayer.DEBUG_COLLISION) {
            attack.renderMask(x-halfWidth,y-halfHeight,g);
        }
    }
}
