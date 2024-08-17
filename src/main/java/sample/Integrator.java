package sample;

import java.util.ArrayList;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;


public class Integrator implements FirstOrderDifferentialEquations{
    @Override
    public int getDimension() {
        return 6; // 3 position + 3 velocity
    }

    @Override
    public void computeDerivatives(double t, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
        // y[0:1] = position;   y[3:5] = velocity
        // yDot[3:5] = acceleration
        yDot[0] = y[3];
        yDot[1] = y[4];
        yDot[2] = y[5];
        double rho = air_density(y[2]);
        double speed = y[3] ; // - this.wind.getWind().x;
        double mach = speed/Math.sqrt(401.87*(288-0.0065*y[5]));
        double cd = CdFunctions.get_Cd(mach, "G7")/ bullet.getBCd();
        double mass = bullet.getMass()*0.000064799; //convert grains to kg
        double A = bullet.getFrontalArea();
        yDot[3] = -0.5*rho*A/mass*cd*y[3]*y[3]; // accell in y are negligible

        double wy = this.wind.getWind().y - yDot[1]; // relative velocity y
        // Cdy for a cylinder L - D = 4pi / [log(2L/D) + 0.5]
        // L/D is approximated as 8 (45/5.56 mm)
        final double Cdy = 3.83;
        // side Area of G7 bullet = 3.1*A
        yDot[4] = 0.5*rho*3.1*A/mass*Cdy*wy*wy;
        yDot[5] = -0.5*rho*3.1*A/mass*Cdy*y[5]*y[5] - 9.80655;

    }


    public void DormandPrince45(){
        // Wrapper for DormandPrice
        FirstOrderIntegrator dp45 = new DormandPrince54Integrator(1.0e-8, 1.0e-4, 1.0e-6, 1.0e-6);
        FirstOrderDifferentialEquations ode = this;
        double u0 = bullet.getMuzzleVelocity()*Math.cos(shootAngle); // x component
        double w0 = bullet.getMuzzleVelocity()*Math.sin(shootAngle); // z component
        double[] y = new double[] {0.0, 0.0, 0.0, u0, 0.0, w0}; //initial state

//        EventHandler stopCriteria = new EventHandler(){}
        StepHandler stepHandler = new StepHandler() {
            @Override
            public void init(double v, double[] doubles, double t) {

            }

            @Override
            public void handleStep(StepInterpolator stepInterpolator, boolean b) throws MaxCountExceededException {
                double t = stepInterpolator.getCurrentTime();
                double[] y = stepInterpolator.getInterpolatedState();
                //System.out.println(t + " " + y[0] + " " + y[1]+ " " + y[2]+ " " + y[3]+ " " + y[4]+ " " + y[5]);
                solution.position.add(new Vec3d(y[0], y[1], y[2]));
                solution.velocity.add(new Vec3d(y[3], y[4], y[5]));
                solution.t.add(t);
            }
        };
        EventHandler stopCriteria = new EventHandler() {
            @Override
            public void init(double v, double[] doubles, double v1) {

            }

            @Override
            public double g(double v, double[] y) {
                return y[0] - range;
            }

            @Override
            public Action eventOccurred(double v, double[] doubles, boolean b) {
                return Action.STOP;
            }

            @Override
            public void resetState(double v, double[] doubles) {

            }
        };
        double t = 2.0; // 2 seconds max
        dp45.addStepHandler(stepHandler);
        // addEventhandler(eventHandler, maxSearchTime, convergence_threshold, max_iterations)
        dp45.addEventHandler(stopCriteria,t,1e-5,20000);
        dp45.integrate(ode, 0.0, y, t, y);
        //System.out.println("Size: " + this.solution.position.size());
        if (this.solution.position.get(this.solution.position.size()-1).x < range){
            System.out.println("Maximum airborne time of 2 seconds reached");
        }
    }

    // Implementing Dormand-Prince method
    public class Solution{
        ArrayList<Vec3d> position;
        ArrayList<Vec3d> velocity;
        ArrayList<Double> t;

        public Solution() {
            this.position = new ArrayList<Vec3d>();
            this.velocity = new ArrayList<Vec3d>();
            this.t = new ArrayList<Double>();
        }
        public double getLastZ(){
            return position.get(position.size()-1).z;
        }
        public ArrayList<Vec3d> getPosition() {
            return position;
        }

        public ArrayList<Vec3d> getVelocity() {
            return velocity;
        }

        public ArrayList<Double> getT() {
            return t;
        }
    }
    Bullet bullet;
    Wind wind;
    Solution solution;
    double range;
    double shootAngle;
    public Integrator(Bullet bullet, Wind wind, double range, double shootAngle) {
        this.bullet = bullet;
        this.wind = wind;
        this.solution = new Solution();
        this.range = range;
        this.shootAngle = shootAngle;
    }

    private double air_density(double altitude){
        double rho_0 = 1.225;
        return rho_0*Math.pow((1 - 0.0065/288 * altitude), 4.256);
    }

}
