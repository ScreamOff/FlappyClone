package io.github.flappyClone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lombok.extern.java.Log;

@Log
public class GameScreen implements Screen {
    private final Sprite pipeSprite;
    private final Array<Float> pipeXs;
    private final Array<Float> pipeYs;
    private final int pipeWidth;
    private final int pipeHeight;
    private final int spaceBetweenPipes;
    private float pipeSpeed;
    private final FlappyBirdGame game;

    // Sprite ptaka
    private final Sprite birdSprite;
    private final Animation<TextureRegion> birdAnimation;
    private float animationTime = 0;

    private float birdY;
    private float velocity = 0;

    // Tło
    private final Sprite backgroundSprite;
    private static final int BACKGROUND_SEGMENTS = 2;
    private static final float BACKGROUND_SPEED = 200;
    private final float[] backgroundX;

    // Ziemia
    private final Sprite groundSprite;
    private final float[] groundX;
    private static final int GROUND_SEGMENTS = 2;
    private static final float GROUND_HEIGHT = 100;
    private static final float GROUND_SPEED = 200;
    private boolean isBirdOnGround;

    // Punktacja
    private int score;
    private final BitmapFont font;

    public GameScreen(FlappyBirdGame game) {
        this.game = game;

        // Animacja ptaka
        Texture birdTexture1 = new Texture("redbird-downflap.png");
        Texture birdTexture2 = new Texture("redbird-midflap.png");
        Texture birdTexture3 = new Texture("redbird-upflap.png");
        Array<TextureRegion> birdFrames = new Array<>();
        birdFrames.add(new TextureRegion(birdTexture1));
        birdFrames.add(new TextureRegion(birdTexture2));
        birdFrames.add(new TextureRegion(birdTexture3));
        birdAnimation = new Animation<>(0.1f, birdFrames, Animation.PlayMode.LOOP);

        // Tło, ziemia i rury
        backgroundSprite = new Sprite(new Texture("background-night.png"));
        groundSprite = new Sprite(new Texture("base.png"));
        pipeSprite = new Sprite(new Texture("pipe-red.png"));

        // Inicjalizacja zmiennych
        pipeXs = new Array<>();
        pipeYs = new Array<>();
        spaceBetweenPipes = 125; // Odległość między rurkami
        pipeSpeed = 100;         // Prędkość przesuwania rurek
        pipeWidth = (int) pipeSprite.getWidth();
        pipeHeight = (int) pipeSprite.getHeight();

        groundX = new float[GROUND_SEGMENTS];
        for (int i = 0; i < GROUND_SEGMENTS; i++) {
            groundX[i] = i * Gdx.graphics.getWidth();
        }
        backgroundX = new float[BACKGROUND_SEGMENTS];
        for (int i = 0; i < BACKGROUND_SEGMENTS; i++) {
            backgroundX[i] = i * Gdx.graphics.getWidth();
        }

        birdSprite = new Sprite(birdAnimation.getKeyFrame(animationTime, true));
        birdY = (float) Gdx.graphics.getHeight() / 2;
        isBirdOnGround = false;

        // Inicjalizacja punktacji
        score = 0;
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2); // Powiększenie czcionki
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit(); // Wyjście z aplikacji
        }
        // Sprawdź, czy nadszedł czas na wygenerowanie nowej rurki
        if (pipeXs.size == 0 || pipeXs.peek() < Gdx.graphics.getWidth() - 400) {
            generatePipe();
            pipeSpeed += 0.1F;
        }
        update(delta);

        // Rysowanie (po aktualizacjach)
        clearScreen();
        game.batch.begin();
        draw(delta);
        drawScore(); // Rysowanie punktacji
        game.batch.end();

        // Sprawdź kolizje
        if (checkCollision()) {
            log.info("Kolizja!");
        }
    }

    private void draw(float delta) {
        drawBackground();  // Tło
        drawPipes();       // Rurki
        drawGround();      // Ziemia
        drawBird(delta);   // Ptak
    }

    private void update(float delta) {
        updateBird();   // Aktualizacja pozycji ptaka
        updateGround(delta); // Aktualizacja pozycji ziemi
        updatePipes(delta);  // Aktualizacja pozycji rurek
        updateBackground(delta);
        checkForScore(); // Sprawdzenie, czy ptak przeleciał przez rurę
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateBird() {
        if (isBirdOnGround) {
            if (Gdx.input.justTouched()) {
                velocity = -15;
                isBirdOnGround = false;
            }
            birdY = GROUND_HEIGHT;
        } else {
            if (Gdx.input.justTouched()) {
                velocity = -10;
            }

            float gravity = 0.80F;
            velocity += gravity;
            birdY -= velocity;

            if (birdY < GROUND_HEIGHT) {
                birdY = GROUND_HEIGHT;
                velocity = 0;
                isBirdOnGround = true;
            }
        }
    }

    private void drawBird(float delta) {
        animationTime += delta;
        birdSprite.setRegion(birdAnimation.getKeyFrame(animationTime, true));
        birdSprite.setPosition(50, birdY);
        birdSprite.draw(game.batch);
    }

    private void updateBackground(float delta) {
        for (int i = 0; i < BACKGROUND_SEGMENTS; i++) {
            backgroundX[i] -= BACKGROUND_SPEED * delta;
            if (backgroundX[i] + Gdx.graphics.getWidth() < 0) {
                backgroundX[i] += BACKGROUND_SEGMENTS * Gdx.graphics.getWidth();
            }
        }
    }

    private void updateGround(float delta) {
        for (int i = 0; i < GROUND_SEGMENTS; i++) {
            groundX[i] -= GROUND_SPEED * delta;
            if (groundX[i] + Gdx.graphics.getWidth() < 0) {
                groundX[i] += GROUND_SEGMENTS * Gdx.graphics.getWidth();
            }
        }
    }

    private void drawPipeUp(float x, float y) {
        pipeSprite.setPosition(x, y);
        pipeSprite.draw(game.batch);
    }

    private void drawPipeDown(float x, float y) {
        pipeSprite.setPosition(x, y);
        pipeSprite.setRotation(180);
        pipeSprite.draw(game.batch);
        pipeSprite.setRotation(0); // Przywróć rotację po rysowaniu
    }

    private void drawGround() {
        for (int i = 0; i < GROUND_SEGMENTS; i++) {
            groundSprite.setPosition(groundX[i], 0);
            groundSprite.setSize(Gdx.graphics.getWidth(), GROUND_HEIGHT);
            groundSprite.draw(game.batch);
        }
    }

    private void drawBackground() {
        for (int i = 0; i < BACKGROUND_SEGMENTS; i++) {
            backgroundSprite.setPosition(backgroundX[i], 0);
            backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            backgroundSprite.draw(game.batch);
        }
    }

    private void generatePipe() {
        int minY = (int) -GROUND_HEIGHT; // Minimalna wysokość dolnej rury
        int downPipeMargin = 50;
        int maxY = (int) (Gdx.graphics.getHeight() - pipeHeight - GROUND_HEIGHT - downPipeMargin); // Maksymalna wysokość dolnej rury

        int y = MathUtils.random(minY, maxY);
        // Dodanie nowych współrzędnych rur do listy
        pipeXs.add((float) Gdx.graphics.getWidth());
        pipeYs.add((float) y);
    }

    private void updatePipes(float delta) {
        for (int i = 0; i < pipeXs.size; i++) {
            pipeXs.set(i, pipeXs.get(i) - pipeSpeed * delta);
        }

        // Usunięcie rurek, które wyszły poza ekran
        if (pipeXs.size > 0 && pipeXs.first() < -pipeWidth) {
            pipeXs.removeIndex(0);
            pipeYs.removeIndex(0);
        }
    }

    private void drawPipes() {
        for (int i = 0; i < pipeXs.size; i++) {
            // Rysowanie dolnej rury
            drawPipeUp(pipeXs.get(i), pipeYs.get(i));

            // Rysowanie górnej rury, odwróconej do góry nogami, powyżej dolnej rury
            drawPipeDown(pipeXs.get(i), pipeYs.get(i) + spaceBetweenPipes + pipeHeight);
        }
    }

    private boolean checkCollision() {
        Rectangle birdRectangle = new Rectangle(50, birdY, birdSprite.getWidth(), birdSprite.getHeight());

        for (int i = 0; i < pipeXs.size; i++) {
            Rectangle pipeUpRectangle = new Rectangle(pipeXs.get(i), pipeYs.get(i), pipeWidth, pipeHeight);
            Rectangle pipeDownRectangle = new Rectangle(pipeXs.get(i), pipeYs.get(i) + spaceBetweenPipes + pipeHeight, pipeWidth, pipeHeight);

            if (birdRectangle.overlaps(pipeUpRectangle) || birdRectangle.overlaps(pipeDownRectangle)) {
                score = 0;
                return true;
            }
        }

        return false;
    }

    private void checkForScore() {
        for (int i = 0; i < pipeXs.size; i++) {
            // Ptak przeleciał przez rurę, więc zwiększamy punktację
            if (pipeXs.get(i) + pipeWidth < 50 && pipeXs.get(i) + pipeWidth + pipeSpeed >= 50) {
                score++;
                log.info("Punktacja: " + score);
            }
        }
    }

    private void drawScore() {
        font.draw(game.batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        birdSprite.getTexture().dispose();
        backgroundSprite.getTexture().dispose();
        groundSprite.getTexture().dispose();
        pipeSprite.getTexture().dispose();
        font.dispose();
    }

    @Override
    public void show() {}
}
