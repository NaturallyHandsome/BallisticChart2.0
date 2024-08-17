//This is the Bullet class used to perform calculations.
package sample;

import org.apache.commons.lang3.ObjectUtils;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.util.ArrayList;

public class Bullet {

    private String name;
    private Double mass;
    private String BC;



    private Wind wind;
    private Double caliber;
    private Vec3d muzzleVelocity;
    private ArrayList<Vector4d> trajectory; //stores position and time

    public Integrator getIntegrator() {
        return integrator;
    }

    private Integrator integrator;

    //used in application
    public Bullet(String name, Double mass, String BC, Double caliber, Double muzzleVelocity) {
        this.name = name;
        this.mass = mass;
        this.BC = BC;
        this.caliber = caliber;
        this.muzzleVelocity = new Vec3d(muzzleVelocity, 0d, 0d);
        this.trajectory = new ArrayList<>();
        this.trajectory.add(new Vector4d(this.muzzleVelocity.x, this.muzzleVelocity.y, this.muzzleVelocity.z, 0));
        this.wind = new Wind(0, 0);
    }
    public Integrator.Solution Calculate(double range, double shootAngle){
        integrator = new Integrator(this, this.wind, range, shootAngle);

        integrator.DormandPrince45();
        return integrator.solution;

    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    ArrayList<Vec3d> getPosition(){
        if(integrator!= null){
            return this.integrator.solution.position;
        }
        System.out.println("Solution not available");
        return new ArrayList<Vec3d>();
    }

    ArrayList<Vec3d> getVelocity(){
        if(integrator!= null){
            return  this.integrator.solution.velocity;
        }
        System.out.println("Solution not available");
        return new ArrayList<>();
    }

    String getName() {
        return name;
    }


    Double getMass() {
        return mass;
    }


    Double getMuzzleVelocity() {
        return muzzleVelocity.length();
    }
    Vec3d getMuzzleVector(){
        return muzzleVelocity;
    }
    Double getCaliber() {
        return caliber;
    }

    Double getFrontalArea(){
        return Math.pow((0.0254*this.caliber),2)*Math.PI/4; // m^2
    }

    String getBC() {
        return BC;
    }
    Double getBCd(){
        return Double.parseDouble(this.BC);
    }

}
