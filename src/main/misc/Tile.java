package main.misc;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.*;

import static main.Main.*;
import static processing.core.PConstants.CORNER;

public class Tile {

    private final PApplet P;

    public int id;
    public PVector position;
    public PImage base;
    public String baseName;
    public PImage decoration;
    public String decorationName;
    public PImage breakable;
    public String breakableName;
    public PImage obstacle;
    public String obstacleName;

    int baseHierarchy;
    PImage[] baseEdges;

    private int obstacleShadowLength;

    public Tile(PApplet p, PVector position, int id) {
        this.P = p;

        this.position = position;
        this.id = id;
        obstacleShadowLength = 3;
        baseEdges = new PImage[4];
    }

    public void highlight(Color color) {
        P.noFill();
        P.stroke(color.getRGB());
        P.rectMode(CORNER);
        P.rect(position.x, position.y, TILE_SIZE, TILE_SIZE);
        P.rectMode(DEFAULT_MODE);
    }

    public void displayBaseAndDecoration() {
        if (base != null) P.image(base, position.x, position.y);
        spillBaseEdges();
        if (decoration != null) P.image(decoration, position.x, position.y);
    }

    public void displayBreakableAndShadow() {
        if (breakable != null) P.image(breakable, position.x, position.y);
        if (obstacle != null) {
            P.tint(0, 60);
            P.image(obstacle, position.x + obstacleShadowLength, position.y + obstacleShadowLength);
            P.tint(255);
        }
    }

    public void displayObstacle() {
        P.tint(255);
        if (obstacle != null) P.image(obstacle, position.x, position.y);
    }

    /**
     * Spills the edges of the base into surrounding tiles
     */
    private void spillBaseEdges() {
        int x = (int) (position.x / 50);
        int y = (int) (position.y / 50);
        if (y != 0) {
            Tile tile = tiles.get(x, y - 1);
            if (canSpill(0, tile)) P.image(tile.baseEdges[0], position.x, position.y);
        }
        if (x != 0) {
            Tile tile = tiles.get(x - 1, y);
            if (canSpill(3, tile)) P.image(tile.baseEdges[3], position.x, position.y);
        }
        if (y != 18) {
            Tile tile = tiles.get(x, y + 1);
            if (canSpill(2, tile)) P.image(tile.baseEdges[2], position.x, position.y);
        }
        if (x != 18) {
            Tile tile = tiles.get(x + 1, y);
            if (canSpill(1, tile)) P.image(tile.baseEdges[1], position.x, position.y);
        }
    }

    /**
     * @param i what edge to check
     * @param tile neighbor tile to check
     * @return whether this tile can spill its base
     */
    private boolean canSpill(int i, Tile tile) {
        if (baseName == null) return false;
        boolean nameDoesNotMatch = !baseName.equals(tile.baseName);
        boolean tileTypeCanSpill = tile.baseEdges[i] != null;
        boolean higherHierarchy = baseHierarchy < tile.baseHierarchy;
        return nameDoesNotMatch && tileTypeCanSpill && higherHierarchy;
    }

    /**
     * Sets the base layer of the tile.
     * @param name name of base
     */
    public void setBase(String name) {
        name = name.replace("Ba_TL", "");
        baseName = name;
        base = sprites.get(name + "Ba_TL");
        baseEdges[0] = sprites.get(name + "Ba_T_TL");
        baseEdges[1] = sprites.get(name + "Ba_R_TL");
        baseEdges[2] = sprites.get(name + "Ba_B_TL");
        baseEdges[3] = sprites.get(name + "Ba_L_TL");
        //update hierarchies as tiles are added
        switch (name) {
            case "snow":
                baseHierarchy = 6;
                break;
            case "grass":
                baseHierarchy = 5;
                break;
            case "yellowGrass":
                baseHierarchy = 4;
                break;
            case "dirt":
                baseHierarchy = 3;
                break;
            case "sand":
                baseHierarchy = 2;
                break;
            case "stone":
                baseHierarchy = 1;
                break;
            case "water":
                baseHierarchy = 0;
                break;
        }
    }

    public void setDecoration(String name) {
        if (name == null) {
            decoration = null;
            decorationName = null;
        } else {
            decoration = sprites.get(name);
            decorationName = name;
        }
    }

    public void setBreakable(String name) {
        if (name != null) name = name.replace("ultimate","titanium");
        if (name == null) {
            breakable = null;
            breakableName = null;
        } else {
            breakable = sprites.get(name);
            breakableName = name;
        }
    }

    public void setObstacle(String name) {
        if (name == null) {
            obstacle = null;
            obstacleName = null;
        } else {
            setDecoration(null);
            setBreakable(null);
            obstacle = sprites.get(name);
            obstacleName = name;
            if (name.contains("smallTree")) obstacleShadowLength = 3;
            if (containsCorners(name,"tree")) obstacleShadowLength = 8;
        }
    }

    private boolean containsCorners(String name, String subName) {
        boolean bl = name.contains(subName + "BL");
        boolean br = name.contains(subName + "BR");
        boolean tl = name.contains(subName + "TL");
        boolean tr = name.contains(subName + "TR");
        return bl || br || tl || tr;
    }

    public PVector getGridPosition() {
        int x = (int) (position.x / 50);
        int y = (int) (position.y / 50);
        return new PVector(x, y);
    }

    public static class TileDS {

        public TileDSItem[] items;

        public TileDS() {
            items = new TileDSItem[0];
        }

        public Tile get(int id) {
            return items[id].tile;
        }

        public Tile get(int x, int y) {
            Tile r = null;
            for (TileDSItem item : items) if (item.x == x && item.y == y) r = item.tile;
            return r;
        }

        public Tile get(IntVector gridPosition) {
            return get(gridPosition.x, gridPosition.y);
        }

        public void add(Tile tile, int x, int y) {
            TileDSItem[] newItems = new TileDSItem[items.length + 1];
            System.arraycopy(items, 0, newItems, 0, items.length);
            newItems[items.length] = new TileDSItem(tile, x, y);
            items = newItems;
        }

        public int size() {
            return items.length;
        }

        public void remove(int id) {
            TileDSItem removeItem = items[id];
            if (removeItem != null) {
                TileDSItem[] newItems = new TileDSItem[items.length - 1];
                for (int i = 0; i < items.length; i++) if (items[i] != removeItem) newItems[i] = items[i];
                items = newItems;
            }
        }

        public void remove(int x, int y) {
            TileDSItem removeItem = null;
            for (TileDSItem item : items) if (item.x == x && item.y == y) removeItem = item;
            if (removeItem != null) {
                TileDSItem[] newItems = new TileDSItem[items.length - 1];
                for (int i = 0; i < items.length; i++) if (items[i] != removeItem) newItems[i] = items[i];
                items = newItems;
            }
        }

        public void remove(Tile tile) {
            TileDSItem removeItem = null;
            for (TileDSItem item : items) if (item.tile == tile) removeItem = item;
            if (removeItem != null) {
                TileDSItem[] newItems = new TileDSItem[items.length - 1];
                for (int i = 0; i < items.length; i++) if (items[i] != removeItem) newItems[i] = items[i];
                items = newItems;
            }
        }

        public static class TileDSItem {

            public Tile tile;
            public int x;
            public int y;

            public TileDSItem(Tile tile, int x, int y) {
                this.tile = tile;
                this.x = x;
                this.y = y;
            }
        }
    }
}
