package com.unlucky.parallax;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.unlucky.Unlucky;
import com.unlucky.ui.LiteUI;

/**
 * Dynamic parallax background for battle scenes or other screens
 *
 * @author Ming Li
 */
public class BackgroundUI extends LiteUI {

    public TextureRegion image;
    protected final Vector2 scale;
    
    private float ax;
    private float ay;
    private int numDrawX;
    private int numDrawY;

    // bg movement
    private float dx;
    private float dy;

    public BackgroundUI(Batch batch, Camera camera, Vector2 scale, ShapeRenderer shapeRenderer) {
        this(batch, null, camera, scale, shapeRenderer);
    }

    public BackgroundUI(Batch batch, TextureRegion image, Camera camera, Vector2 scale, ShapeRenderer shapeRenderer) {
        super(batch, camera, shapeRenderer);
        this.image = image;
        this.scale = scale;
        
        if (image != null) {
            numDrawX = (Unlucky.V_WIDTH * 2) / image.getRegionWidth() + 1;
            numDrawY = (Unlucky.V_HEIGHT * 2) / image.getRegionHeight() + 1;
            fixBleeding(image);
        }
    }

    public void setImage(TextureRegion image) {
        this.image = image;
        numDrawX = (Unlucky.V_WIDTH * 2) / image.getRegionWidth() + 1;
        numDrawY = (Unlucky.V_HEIGHT * 2) / image.getRegionHeight() + 1;
        fixBleeding(image);
    }

    /**
     * Fixes the slight 1 pixel offset when moving the background to create
     * a smooth cycling image
     *
     * @param region
     */
    public void fixBleeding(TextureRegion region) {
        float fix = 0.01f;

        float x = region.getRegionX();
        float y = region.getRegionY();
        float width = region.getRegionWidth();
        float height = region.getRegionHeight();
        float invTexWidth = 1f / region.getTexture().getWidth();
        float invTexHeight = 1f / region.getTexture().getHeight();
        region.setRegion((x + fix) * invTexWidth, (y + fix) * invTexHeight, (x + width - fix) * invTexWidth, (y + height - fix) * invTexHeight);
    }

    public void setVector(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void update(float dt) {
        ax += (dx * scale.x) * dt;
        ay += (dy * scale.y) * dt;
    }


    @Override
    public void render(float dt) {
     // bg not moving
        if (dx == 0 && dy == 0) {
            batch.draw(image, 0, 0);
        }
        else {
            float x = ((ax + camera.viewportWidth / 2 - camera.position.x) * scale.x) % image.getRegionWidth();
            float y = ((ay + camera.viewportHeight / 2 - camera.position.y) * scale.y) % image.getRegionHeight();

            int colOffset = x > 0 ? -1 : 0;
            int rowOffset = y > 0 ? -1 : 0;

            for (int row = 0; row < numDrawY; row++) {
                for (int col = 0; col < numDrawX; col++) {
                    batch.draw(image,
                        x + (col + colOffset) * image.getRegionWidth(),
                        y + (row + rowOffset) * image.getRegionHeight());
                }
            }
        }
    }

}
