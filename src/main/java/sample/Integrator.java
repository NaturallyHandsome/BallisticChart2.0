package sample;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Vector;

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




// Old code, don't use
    private class Stage{
        Vec3d x;
        Vec3d u;
        public Stage(){
            this.u = new Vec3d();
            this.x = new Vec3d();
        }
        public Stage(Vec3d u, Vec3d x) {
            this.u = u;
            this.x = x;
        }
    }
    private Stage dudt(Vec3d u, Vec3d x){
        /*
        Returns:
            x: accelleration
            u: velocity
        dx/dt = u
        du/dt = f(t,u) = -1/2 * rho/m *A*Cd(u(t)) * u(t)^2 + g
        x0 = 0;
        u0 = muzzleVelocity
         */
        Vec3d ul = new Vec3d(u); // check if they're independent
        ul.add(wind.getWind());
        double rho = air_density(x.z);
        double speed = ul.length();
        double mach = speed/Math.sqrt(401.87*(288-0.0065*x.z));
        double cd = CdFunctions.get_Cd(mach, "G7")/ bullet.getBCd();
        double mass = bullet.getMass()*0.000064799; //convert grains to kg
        double A = bullet.getFrontalArea();
        Vec3d acc = new Vec3d(-0.5*A*rho/mass * cd*Math.pow(speed,2), 0, 9.80655);
        //we ignore drag in Y and Z
        Stage f = new Stage(ul,acc);

        return f;
    }

    public  void Dormand_Prince(double range){
        this.solution.position.add(new Vec3d(0,0,0));
        this.solution.velocity.add(this.bullet.getMuzzleVector());
        this.solution.t.add(0.D);
        double dt = 0.5/this.bullet.getMuzzleVelocity(); //0.5m every timestep

        /*
        Butcher's table
        b = {0, 1/5, 3/10, 4/5, 8/9, 1, 1}; //dt weights
        a = {5179/57600, 0, 7571/16695, 393/640, -92097/339200, 187/2100, 1/40}; //final weights
        c = {   {1/5, 0, 0, 0, 0, 0,0},
                {3/40, 9/40, 0,0,0,0,0},
                {44/45, -56/15, 32/9, 0, 0, 0, 0},
                {19372/6561, -25360/2187, 64448/6561, -212/729, 0,0,0},
                {9017/3168, -355/33, 46732/5247, 49/176, -5103/18656},
                {35/384, 0, 500/1113, 125/192, -2187/6784, 11/84}}; //k weights
         */

        Stage k1;
        Stage k2;
        Stage k3;
        Stage k4;
        Stage k5;
        Stage k6;
        Stage k7;
        int j=0;
        while (solution.position.get(solution.position.size()-1).length() < range) {

            Vec3d u = this.solution.velocity.get(j);
            Vec3d x = this.solution.position.get(j);
            // Problem found, dudt needs a deep copy of u
            k1 = dudt(u, x);
            k1.u.scale(dt / 5);

            k2 = dudt(u.scaleAdd(dt / 5, k1.u), x.scaleAdd(dt / 5, k1.x));
            k3 = dudt(u.add(k1.u.scale2(dt * 3 / 40)).add(k2.u.scale2(dt * 9 / 40)), x.add(k1.x.scale2(dt * 3 / 40)).add(k2.x.scale2(dt * 9 / 40)));
            k4 = dudt(u.add(k1.u.scale2(dt * 44 / 45)).add(k2.u.scale2(-dt * 56 / 15)).add(k3.u.scale2(dt * 32 / 9)),
                    x.add(k1.x.scale2(dt * 44 / 45)).add(k2.x.scale2(-dt * 56 / 15)).add(k3.x.scale2(dt * 32 / 9)));
            k5 = dudt(u.add(k1.u.scale2(dt * 19372 / 6561)).add(k2.u.scale2(-dt * 25360 / 2187)).add(k3.u.scale2(dt * 64448 / 6561)).add(k4.u.scale2(-dt * 212 / 729)),
                    x.add(k1.x.scale2(dt * 19372 / 6561)).add(k2.x.scale2(-dt * 25360 / 2187)).add(k3.x.scale2(dt * 64448 / 6561)).add(k4.x.scale2(-dt * 212 / 729)));
            k6 = dudt(u.add(k1.u.scale2(dt * 9017 / 3168)).add(k2.u.scale2(-dt * 355 / 33)).add(k3.u.scale2(dt * 46732 / 5247)).add(k4.u.scale2(dt * 49 / 176)).add(k5.u.scale2(-dt * 5103 / 18656)),
                    x.add(k1.x.scale2(dt * 9017 / 3168)).add(k2.x.scale2(-dt * 355 / 33)).add(k3.x.scale2(dt * 46732 / 5247)).add(k4.x.scale2(dt * 49 / 176)).add(k5.x.scale2(-dt * 5103 / 18656)));
            k7 = dudt(u.add(k1.u.scale2(dt * 35 / 384)).add(k3.u.scale2(dt * 500 / 1113)).add(k4.u.scale2(dt * 125 / 192)).add(k5.u.scale2(-dt * 2187 / 6784)).add(k6.u.scale2(dt * 11 / 84)),
                    x.add(k1.x.scale2(dt * 35 / 384)).add(k3.x.scale2(dt * 500 / 1113)).add(k4.x.scale2(dt * 125 / 192)).add(k5.x.scale2(-dt * 2187 / 6784)).add(k6.x.scale2(dt * 11 / 84)));

            u = u.add(k1.u.scale2(dt * 5179 / 57600)).add(k3.u.scale2(dt * 7571 / 16695)).add(k4.u.scale2(dt * 393 / 640)).add(k5.u.scale2(-dt * 92097 / 339200)).add(k6.u.scale2(dt * 187 / 2100)).add(k7.u.scale2(dt * 1 / 40));
            x = x.add(k1.x.scale2(dt * 5179 / 57600)).add(k3.x.scale2(dt * 7571 / 16695)).add(k4.x.scale2(dt * 393 / 640)).add(k5.x.scale2(-dt * 92097 / 339200)).add(k6.x.scale2(dt * 187 / 2100)).add(k7.x.scale2(dt * 1 / 40));
            this.solution.velocity.add(u);
            this.solution.position.add(x);

        }
    }

}
