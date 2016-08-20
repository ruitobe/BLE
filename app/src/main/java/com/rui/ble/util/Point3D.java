package com.rui.ble.util;

/**
 * Created by rhuang on 8/19/16.
 */
public class Point3D {

    public double x, y, z;

    public Point3D(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        long temp;

        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Point3D other = (Point3D) obj;

        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;

        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;

        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
            return false;

        return true;
    }
}
