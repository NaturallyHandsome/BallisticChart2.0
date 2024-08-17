package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;


import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class VelocityController implements Initializable {

    @FXML
    private LineChart<Number, Number> velocityChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        ArrayList<Vec3d> vels = Controller.bullet.getVelocity();
        ArrayList<Vec3d> pos = Controller.bullet.getPosition();
        int size = vels.size();
        int step = Math.round(size/100);
        for (int i=0; i<size; i+=step){
            double x = pos.get(i).x;
            double y = vels.get(i).length();
            series.getData().add(new XYChart.Data<>(x, y));
        }
        series.setName(Controller.bullet.getName());
        velocityChart.getData().add(series);
    }
}
