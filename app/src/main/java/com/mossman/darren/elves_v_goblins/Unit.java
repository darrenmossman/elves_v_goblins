package com.mossman.darren.elves_v_goblins;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mossman.darren.elves_v_goblins.Unit.Type.Elf;
import static com.mossman.darren.elves_v_goblins.Unit.Type.Goblin;

public class Unit implements Comparable<Unit>{

    public enum Type {Elf, Goblin};

    public enum Direction {none, up, left, down, right};

    public static int elfAttackPower = 8;
    final static private int goblinAttachPower = 3;

    Type type;
    int x, y;
    int attackPower;
    int hitPoints = 200;
    Unit attackTarget;
    Node shortestPath;
    boolean isDead;
    long dying = AnimationThread.FPS;
    long bloody = 0;

    private char[][] map;
    private Bitmap bmp;

    private static final int BMP_ROWS = 4;
    private static final int BMP_COLUMNS = 3;
    private int width, height;
    private int currentFrame = 0;
    private int xx, yy;

    private int px, py;
    private int dx, dy;
    private double p = 0;
    private double d = 1;

    public boolean arrived() {
        return px == x && py == py;
    }

    private void updateFrame(int sx, int sy) {
        int sz = Math.min(sx, sy);

        if (dx != x || dy != y) {
            px = dx; py = dy;
            dx = x; dy = y;
            p = 0;
            d = d*1.2;
        } else if (dx != px || dy != py) {
            p = p + d;
            if (p > sz) {
                d = d*0.7;
                p = 0;
                px = dx; py = dy;
            }
        }

        int nx = x*sx;
        int ny = y*sy;
        int ox = px*sx;
        int oy = py*sy;

        xx = (int)(ox + (nx-ox) * p / sz);
        yy = (int)(oy + (ny-oy) * p / sz);
        currentFrame = ++currentFrame % BMP_COLUMNS;
    }

    Direction dir = Direction.none;
    private int getAnimationRow() {
        switch (dir) {
            case up: return 3;
            case left: return 1;
            case down: return 0;
            case right: return 2;
            default: return 0;
        }
    }

    public void drawBlood(Canvas canvas, int sx, int sy, Bitmap bmpBlood) {
        if (!arrived()) return;

        if (isDead) {
            if (--dying > 0) {
                canvas.drawBitmap(bmpBlood, xx - bmpBlood.getWidth() / 2, yy - bmpBlood.getHeight() / 2, null);
            }
        } else {
            if (--bloody > 0) {
                canvas.drawBitmap(bmpBlood, xx - bmpBlood.getWidth() / 2, yy - bmpBlood.getHeight() / 2, null);
            }
        }
    }

    public void draw(Canvas canvas, int sx, int sy) {
        if (isDead) return;

        updateFrame(sx, sy);
        int srcY = getAnimationRow() * height;
        if (dir == Direction.none) currentFrame = 0;
        int srcX = currentFrame * width;
        Rect src = new Rect(srcX, srcY, srcX + width, srcY + height);
        Rect dst = new Rect(xx - width / 2, yy - height / 2, xx + width / 2, yy + height / 2);
        canvas.drawBitmap(bmp, src, dst, null);
    }

    @Override
    public Unit clone() {
        Unit u = new Unit(bmp, map, typeChar(), x, y);
        u.attackPower = attackPower;
        u.hitPoints = hitPoints;
        return u;
    }


    public Unit(Bitmap bmp, char[][] map, char type, int x, int y) {
        this.bmp = bmp;
        this.width = bmp.getWidth() / BMP_COLUMNS;
        this.height = bmp.getHeight() / BMP_ROWS;

        this.map = map;

        this.type = type == 'E' ? Elf : Goblin;
        this.x = x; px = x; dx = x;
        this.y = y; py = y; dy = y;
        if (this.type == Elf) {
            attackPower = elfAttackPower;
        } else {
            attackPower = goblinAttachPower;
        }
    }

    public int compareTo(Unit other) {
        if (y == other.y) {
            return Integer.compare(x, other.x);
        } else {
            return Integer.compare(y, other.y);
        }
    }

    private char typeChar() {
        return type==Elf ? 'E' : 'G';
    }
    private String typeString() {
        return type==Elf ? "Elf" : "Goblin";
    }


    public String toString() {
        return String.format("%s(%d)", typeChar(), hitPoints);
    }

    public String toLocation() {
        return String.format("%s (x: %d, y: %d)", typeString(), x, y);
    }


    private Node getPath(int tx, int ty, int xx, int yy, Node shortestPath) {

        // Discard if Manhattan distance > shortest path
        if (shortestPath != null) {
            if (Math.abs(tx-xx) + Math.abs(ty-yy) > shortestPath.dist) {
                return null;
            }
        }

        int height = map.length;
        int width = map[0].length;

        List<Node> unvisited = new ArrayList<>();
        Node[][] grid = new Node[height][width];


        /*
         * Dijkstra's algorithm finds shortest path, but is not amenable to selecting
         * correct node (using the required reading order) when two have same distance
         */

        Node node = new Node(xx, yy, 0, tx, ty);
        grid[yy][xx] = node;
        unvisited.add(node);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (map[y][x] == '.') {
                    Node nd = new Node(x,y, Integer.MAX_VALUE, tx, ty);
                    grid[y][x] = nd;
                    unvisited.add(nd);
                } else {
                    grid[y][x] = null;
                }
            }
        }

        while (!unvisited.isEmpty()) {
            node = unvisited.get(0);
            if (node.dist == Integer.MAX_VALUE) {
                break;
            }
            if (shortestPath != null && node.dist > shortestPath.dist) {
                break;
            }
            if (node.x == tx && node.y == ty) {
                return node;
            }
            for (Node nd: node.adjacents(grid)) {
                if (unvisited.contains(nd)) {
                    if (nd.dist > 1 + node.dist) {
                        nd.dist = 1 + node.dist;
                        nd.previous = node;
                    }
                }
            }
            unvisited.remove(node);
            Collections.sort(unvisited);
        }
        return null;
    }

    public Node getShortestPath(Unit target) {
        Node shortestPath = null;
        int tx, ty, xx, yy;

        ArrayList<Node> adjacents = new ArrayList<>();
        xx = this.x; yy = this.y;             // actual unit
        adjacents.add(new Node(xx, yy));
/*
        xx = this.x; yy = this.y-1;             // above unit
        if (map[yy][xx] == '.') adjacents.add(new Node(xx, yy));
        xx = this.x-1; yy = this.y;             // left of unit
        if (map[yy][xx] == '.') adjacents.add(new Node(xx, yy));
        xx = this.x+1; yy = this.y;             // right of unit
        if (map[yy][xx] == '.') adjacents.add(new Node(xx, yy));
        xx = this.x; yy = this.y+1;             // below unit
        if (map[yy][xx] == '.') adjacents.add(new Node(xx, yy));
*/
        if (adjacents.size() == 0) return null;

        tx = target.x; ty = target.y - 1;       // above target
        if (map[ty][tx] == '.') {
            for (Node nd: adjacents) {
                Node node = getPath(tx, ty, nd.x, nd.y, shortestPath);
                if (node != null && (shortestPath == null || node.dist < shortestPath.dist)) {
                    shortestPath = node;
                    if (shortestPath.dist == 0) return shortestPath;
                }
            }
        }
        tx = target.x-1; ty = target.y;         // left of target
        if (map[ty][tx] == '.') {
            for (Node nd: adjacents) {
                Node node = getPath(tx, ty, nd.x, nd.y, shortestPath);
                if (node != null && (shortestPath == null || node.dist < shortestPath.dist)) {
                    shortestPath = node;
                    if (shortestPath.dist == 0) return shortestPath;
                }
            }
        }
        tx = target.x+1; ty = target.y;         // right of target
        if (map[ty][tx] == '.') {
            for (Node nd: adjacents) {
                Node node = getPath(tx, ty, nd.x, nd.y, shortestPath);
                if (node != null && (shortestPath == null || node.dist < shortestPath.dist)) {
                    shortestPath = node;
                    if (shortestPath.dist == 0) return shortestPath;
                }
            }
        }
        tx = target.x; ty = target.y+1;         // below target
        if (map[ty][tx] == '.') {
            for (Node nd: adjacents) {
                Node node = getPath(tx, ty, nd.x, nd.y, shortestPath);
                if (node != null && (shortestPath == null || node.dist < shortestPath.dist)) {
                    shortestPath = node;
                    if (shortestPath.dist == 0) return shortestPath;
                }
            }
        }
        return shortestPath;
    }

    public boolean inRangeOf(Unit target) {
        if (target.x == x && (target.y == y-1 || target.y == y+1)) {
            return true;
        }
        if (target.y == y && (target.x == x-1 || target.x == x+1)) {
            return true;
        }
        return false;
    }

    public void attack(Unit target) {
        target.hitPoints -= this.attackPower;
        if (target.hitPoints <= 0) {
            target.isDead = true;
            map[target.y][target.x] = '.';
        } else {
            target.bloody = 3;
        }
        if (target.x > x) dir = Direction.right;
        else if (target.x < x) dir = Direction.left;
        else if (target.y < y) dir = Direction.up;
        else if (target.y > y) dir = Direction.down;
    }

    public void moveTowards(Node node) {
        Node start = node.reversePath();

        if (start.x > x) dir = Direction.right;
        else if (start.x < x) dir = Direction.left;
        else if (start.y < y) dir = Direction.up;
        else if (start.y > y) dir = Direction.down;

        map[y][x] = '.';
        x = start.x;
        y = start.y;
        map[y][x] = typeChar();
    }

    public List<Unit> getTargets(List<Unit> units) {
        List<Unit> targets = new ArrayList<>();
        for (Unit target: units) {
            if (target.type != type && !target.isDead) targets.add(target);
        }
        return targets;
    }

    public boolean isCollition(float x2, float y2) {
        return x2 > x && x2 < x + width && y2 > y && y2 < y + height;
    }
}
