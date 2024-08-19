package io.github.flappyClone;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FlappyBirdGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new GameScreen(this));  // Ustawiamy GameScreen jako pierwszy ekran
    }

    @Override
    public void render() {
        super.render();  // Wywołuje renderowanie bieżącego ekranu
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
