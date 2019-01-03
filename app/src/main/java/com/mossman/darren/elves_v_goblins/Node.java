package com.mossman.darren.elves_v_goblins;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
    int x, y, dist;
    int tx, ty;
    Node previous;

    public Node(int x, int y) {
        this(x, y, 0, 0, 0);
    }
    public Node(int x, int y, int dist, int tx, int ty) {
        this.x = x;
        this.y = y;
        this.dist = dist;
        this.tx = tx;
        this.ty = ty;
    }

    public int compareTo(@NonNull Node other) {
        return Integer.compare(dist, other.dist);
    }

    public List<Node> adjacents(Node[][] grid) {
        List<Node> res = new ArrayList<>();
        Node nd = grid[y - 1][x];
        if (nd != null) res.add(nd);
        nd = grid[y][x - 1];
        if (nd != null) res.add(nd);
        nd = grid[y][x + 1];
        if (nd != null) res.add(nd);
        nd = grid[y + 1][x];
        if (nd != null) res.add(nd);
        return res;
    }

    public Node reversePath() {
        Node res = this;
        Node nd = this;
        while (nd.previous != null) {
            res = nd;
            nd = nd.previous;
        }
        return res;
    }
}
