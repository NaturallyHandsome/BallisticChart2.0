package sample;

import javax.vecmath.Vector3d;

public class Vec3d extends Vector3d {
    // my own implementation of Vector3d

    public Vec3d(Vec3d v){
        this.set(v.x, v.y, v.z);
    }

    public Vec3d(){
        this.set(0,0,0);
    }
    public Vec3d(double x, double y, double z) {
        this.set(x,y,z);

    }
    public Vec3d add(Vec3d a, Vec3d b){
        Vec3d c=new Vec3d();
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        c.z = a.z+b.z;
        return c;
    }
    public Vec3d add(Vec3d a){
        Vec3d c = new Vec3d();
        c.x = this.x +a.x;
        c.y = this.y + a.y;
        c.z = this.z + a.z;
        return c;
    }

    public  Vec3d scale2(double s){
        Vec3d c = new Vec3d();
        c.x = this.x*s;
        c.y = this.y*s;
        c.z = this.z*s;
        return c;
    }
    public Vec3d scaleAdd(double s, Vec3d v){
        Vec3d c = this.scale2(s);
        c = this.add(c,v);
        return c;

    }
}
