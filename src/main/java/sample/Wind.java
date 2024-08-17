package sample;

import javax.vecmath.Vector3d;

public class Wind {
    public Vector3d getWind() {
        return wind;
    }

    Vector3d wind = new Vector3d();
    public Wind(double speed, double orientation) {
        double angleRad = (Math.PI * orientation)/180;
        wind.x = Math.cos(angleRad) * speed;
        wind.y = -Math.sin(angleRad) * speed;
        wind.z = 0d;
    }

}
