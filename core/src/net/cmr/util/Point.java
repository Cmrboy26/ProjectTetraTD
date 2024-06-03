package net.cmr.util;

public class Point {
    
    public int x, y;
    public Point() {
        this(0, 0);
    }
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point)obj;
            return (x == pt.x) && (y == pt.y);
        }
        return super.equals(obj);
    }

    public String toString() {
        return "Point[x=" + x + ",y=" + y + "]";
    }

}
