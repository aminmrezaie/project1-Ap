package model.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Position {

    private final int q;
    private final int r;

    private static final int[][] DIRECTIONS = {
            {+1,  0}, {+1, -1}, { 0, -1},
            {-1,  0}, {-1, +1}, { 0, +1}
    };

    public Position(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return -q - r;
    }

    public List<Position> neighbors() {
        List<Position> result = new ArrayList<>(6);
        for (int[] dir : DIRECTIONS) {
            result.add(new Position(q + dir[0], r + dir[1]));
        }
        return result;
    }

    public int distanceTo(Position other) {
        return (Math.abs(q - other.q)
                + Math.abs(r - other.r)
                + Math.abs(getS() - other.getS())) / 2;
    }


    public List<Position> positionsInRadius(int radius) {
        List<Position> result = new ArrayList<>();
        for (int dq = -radius; dq <= radius; dq++) {
            int rMin = Math.max(-radius, -dq - radius);
            int rMax = Math.min(radius, -dq + radius);
            for (int dr = rMin; dr <= rMax; dr++) {
                result.add(new Position(q + dq, r + dr));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return q == p.q && r == p.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    @Override
    public String toString() {
        return "Position(" + q + ", " + r + ")";
    }
}
