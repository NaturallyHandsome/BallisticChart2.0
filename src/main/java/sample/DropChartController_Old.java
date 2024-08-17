package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DropChartController_Old implements Initializable {
    private Bullet bullet;

    @FXML private LineChart<String, Double> dropChart;
    //TODO: create list of series for multiple path plotting
    @FXML private  XYChart.Series<String, Double> series;

    static ArrayList<Point3d> position = new ArrayList<>();
    static ArrayList<Vector3d> velocity;
    static Double finalVel;
    static Double finalDrop;

    public DropChartController_Old(Bullet bullet) {
        this.bullet = bullet;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        series = new XYChart.Series<>();
        //series.setName(bullet.getName()); // bullet is null

        dropChart.getData().add(series);
        dropChart.setCreateSymbols(false);
        //position= new ArrayList<>();
        //velocity= new ArrayList<>();
        //PlotEulerMethod();
        //Calculator.Calculate(Controller.bullet, Controller.wind, Controller.range, 7);
        //Calculator.VerletIntegration(Controller.bullet, Controller.wind, Controller.range, 7, position);
       // Calculator.HeunIntegration(Controller.bullet, Controller.wind, Controller.range, 7);
        double stepSize;
        if (position.size()<20)
            stepSize=1;
        else
            stepSize=Math.floor(position.size()/20 *100)/100;
        for (float i=0; i<Controller.velocities.size(); i+=stepSize) {
            Double point_x = Controller.velocities.get( Math.round(i)).x;
            Double point_y = Controller.velocities.get( Math.round(i)).y;
            //Double point_z = position.get( Math.round(i)).z;
            series.getData().add(new XYChart.Data<>(point_x.toString(), point_y));
        }
    }


}