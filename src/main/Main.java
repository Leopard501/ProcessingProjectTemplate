package main;

import main.gui.Hand;
import main.gui.LevelBuilderGui;
import main.gui.LoadingScreen;
import main.gui.TitleScreen;
import main.misc.InputManager;
import main.misc.Tile;
import main.sound.FadeSoundLoop;
import main.sound.SoundWithAlts;
import main.sound.StartStopSoundLoop;
import main.world.World;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.sound.Sound;
import processing.sound.SoundFile;

import java.awt.*;
import java.util.HashMap;

import static java.lang.Character.toLowerCase;
import static main.misc.SpriteLoader.loadAnimations;
import static main.misc.SpriteLoader.loadSprites;
import static main.sound.SoundLoader.loadSounds;

public class Main extends PApplet {

    public enum Scene {
        World(0),
        TitleScreen(1),
        LoadingScreen(2);

        public final int ID;

        Scene(int id) {
            ID = id;
        }
    }

    public static final int TILE_SIZE = 50;
    public static final int FRAMERATE = 60;
    public static final int DEFAULT_MODE = CORNER;
    public static final PVector BOARD_SIZE = new PVector(1100, 900);
    public static final String TITLE = "template";

    public static float globalVolume = 0.25f;
    public static boolean debug = false;

    private static final Color BACKGROUND_COLOR = new Color(0, 15, 45);
    private static final boolean FULLSCREEN = true;

    private static float matrixScale;
    private static float matrixOffset;

    public static HashMap<String, PImage> sprites;
    public static HashMap<String, PImage[]> animations;

    public static Sound sound;
    public static HashMap<String, SoundFile> sounds;
    public static HashMap<String, StartStopSoundLoop> startStopSoundLoops;
    public static HashMap<String, FadeSoundLoop> fadeSoundLoops;
    public static HashMap<String, SoundWithAlts> soundsWithAlts;

    public static Tile.TileDS tiles;
    public static PVector matrixMousePosition;

    private final InputManager inputManager = InputManager.getInstance();

    public static Hand hand;
    public static LevelBuilderGui levelBuilderGui;

    public static World world;
    public static TitleScreen titleScreen;
    public static LoadingScreen loadingScreen;

    public static Scene scene = Scene.LoadingScreen;

    public static void main(String[] args) {
        PApplet.main("main.Main", args);
    }

    @Override
    public void settings() {
        if (FULLSCREEN) {
            fullScreen(P2D);
            noSmooth();
        } else size((int) BOARD_SIZE.x, (int) BOARD_SIZE.y, P2D);
    }

    @Override
    public void setup() {
        frameRate(FRAMERATE);
        surface.setTitle(TITLE);
        loadingScreen = new LoadingScreen(this, createFont("Helvetica", 32));

        setupSound();
        setupFullscreen();
    }

    public static void setupTiles(PApplet p) {
        tiles = new Tile.TileDS();
        for (int y = 0; y <= BOARD_SIZE.y / TILE_SIZE; y++) {
            for (int x = 0; x <= BOARD_SIZE.x / TILE_SIZE; x++) {
                tiles.add(new Tile(p, new PVector(x * TILE_SIZE, y * TILE_SIZE), tiles.size()), x, y);
            }
        }
    }

    public static void setupMisc(PApplet p) {
        hand = new Hand(p);
        levelBuilderGui = new LevelBuilderGui(p);
    }

    private void setupFullscreen() {
        if (hasVerticalBars()) {
            matrixScale = height / BOARD_SIZE.y;
            matrixOffset = (width - (BOARD_SIZE.x * matrixScale)) / 2;
        } else {
            matrixScale = width / BOARD_SIZE.x;
            matrixOffset = (height - (BOARD_SIZE.y * matrixScale)) / 2;
        }
    }

    private void setupSound() {
        sound = new Sound(this);
        sounds = new HashMap<>();
        startStopSoundLoops = new HashMap<>();
        fadeSoundLoops = new HashMap<>();
        soundsWithAlts = new HashMap<>();
        loadSounds(this);
    }

    public static void setupSprites(PApplet p) {
        sprites = new HashMap<>();
        animations = new HashMap<>();
        loadSprites(p);
        loadAnimations(p);
    }

    @Override
    public void draw() {
        inputManager.update();
        background(BACKGROUND_COLOR.getRGB());
        drawSound();

        pushFullscreen();

        switch (scene) {
            case World:
                world.main();
                levelBuilderGui.main();
                hand.main();
                break;
            case TitleScreen:
                titleScreen.main();
                break;
            case LoadingScreen:
                loadingScreen.main();
                break;
        }


        popFullscreen();
    }

    private void drawSound() {
        sound.volume(globalVolume);
        for (StartStopSoundLoop startStopSoundLoop : startStopSoundLoops.values()) startStopSoundLoop.continueLoop();
        for (FadeSoundLoop fadeSoundLoop : fadeSoundLoops.values()) fadeSoundLoop.main();
    }

    private void pushFullscreen() {
        pushMatrix();
        if (hasVerticalBars()) translate(matrixOffset, 0);
        else translate(0, matrixOffset);
        scale(matrixScale);
        if (hasVerticalBars()) {
            matrixMousePosition = new PVector((mouseX - matrixOffset) / matrixScale, mouseY / matrixScale);
        } else {
            matrixMousePosition = new PVector(mouseX / matrixScale, (mouseY - matrixOffset) / matrixScale);
        }
    }

    private void popFullscreen() {
        popMatrix();
        drawBlackBars();
    }

    private boolean hasVerticalBars() {
        float screenRatio = width / (float) height;
        float boardRatio = BOARD_SIZE.x / BOARD_SIZE.y;
        return boardRatio < screenRatio;
    }

    private void drawBlackBars() {
        fill(0);
        rectMode(CORNER);
        noStroke();
        if (hasVerticalBars()) {
            rect(0, 0, matrixOffset, height);
            rect(width - matrixOffset, 0, matrixOffset, height);
        } else {
            rect(0, 0, width, matrixOffset);
            rect(0, height - matrixOffset, width, matrixOffset);
        }
        rectMode(DEFAULT_MODE);
    }

    @Override
    public void keyPressed() {
        inputManager.testPresses((short) keyCode);
    }

    @Override
    public void keyReleased() {
        inputManager.testReleases((short) keyCode);
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT) inputManager.leftMouse.setState(true);
        else if (mouseButton == RIGHT) inputManager.rightMouse.setState(true);
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == LEFT) inputManager.leftMouse.setState(false);
        else if (mouseButton == RIGHT) inputManager.rightMouse.setState(false);
    }
}
