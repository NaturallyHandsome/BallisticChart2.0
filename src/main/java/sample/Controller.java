package sample;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.textfield.TextFields;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import static sample.CartridgeConnection.*;

public class Controller implements Initializable{

    @FXML private JFXToggleButton SpeedGraphSwitch;
    @FXML private JFXTextField RangeField;
    @FXML private JFXTextField NameField;
    @FXML private JFXSlider RangeSlider;
    @FXML private Label dropLabel;
    @FXML private Label finalVelLabel;
    @FXML private ComboBox<Label> GCombo;
    @FXML private  JFXTextField windSpeedField;
    @FXML private  JFXTextField windOrientationField;
    @FXML private  JFXTextField shootingAngleField;
    @FXML private  JFXTextField targetAngleField;

    @FXML private LineChart<Number, Number> startChart;

    static Bullet bullet; //changed from BulletPhysical
    static Wind wind;
    static Double range;
    static  String GModel;
    static double shootAngle; // stored in radians

    static ArrayList<Vector3d> velocities = new ArrayList<>(); // not used anymore


    public void setRangeSlider() {
        RangeSlider.setValue(Double.parseDouble(RangeField.getText()));
    }

    private void loadController(String path, boolean resizable) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(resizable);


        loader.getController();
        stage.show();
    }

    public void Calculate() throws IOException, SQLException, ClassNotFoundException {

        try {
            shootAngle = Double.parseDouble(shootingAngleField.getText())/60*Math.PI/180; //minutes to radians
            bullet = selectedBullet(NameField.getText());
            range = Double.parseDouble(RangeField.getText()); //flight time in ms for testing
            wind = new Wind(Double.parseDouble(windSpeedField.getText()), Double.parseDouble(windOrientationField.getText()));
            bullet.setWind(wind);
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Fill all fields");
            alert.showAndWait();
            return;
        }
        // Calculate here, then pass data to DropChartController
        // Iterative solution to find target angle field
        double toll = 0.004; // tolerance
        int maxIter = 10;
        int iter = 0;
        Integrator.Solution solution = bullet.Calculate(range, shootAngle); // initial estimate
        double lastZ = solution.getLastZ();
        while (Math.abs(lastZ) >= toll && iter<maxIter){
            double spread = lastZ/range; // no dimensional
            if(lastZ>0){
                // shootAngle too big
                shootAngle = shootAngle - Math.abs(spread);
            }
            if (lastZ<0){
                shootAngle = shootAngle+ Math.abs(spread);
            }
            solution = bullet.Calculate(range, shootAngle);
            lastZ = solution.getLastZ();
            iter+=1;
        }

        //loadController("/fxml/BulletDrop.fxml", true);

        // Adding data
        Collection<XYChart.Data<Number, Number>> collection = new ArrayList<XYChart.Data<Number, Number>>();
        int size = solution.position.size();
        int step = Math.round(size/100); // max 100 points
        for(int i=0; i<size; i+=step){
            double x = bullet.getPosition().get(i).x;
            double y = bullet.getPosition().get(i).z * 100; // to cm
            collection.add(new XYChart.Data<>(x, y));
            //series.getData().add(new XYChart.Data<>(x,y));
        }
        ObservableList<XYChart.Data<Number, Number>> list = FXCollections.observableArrayList(collection);
        XYChart.Series<Number, Number> series = new XYChart.Series<>(list);
        series.setName(bullet.getName());
        startChart.getData().add(series);

        targetAngleField.setText(Math.round(shootAngle * 10800 / Math.PI) +"'"); // radians to minutes

        dropLabel.setVisible(true);
        finalVelLabel.setVisible(true);
        dropLabel.setText("Vertical Error:    "+ Math.round(solution.position.get(size - 1).z*100) +"cm");
        finalVelLabel.setText("Final Velocity:  "+ Math.round(solution.velocity.get(size - 1).x) +"m/s");
        if (SpeedGraphSwitch.isSelected()) openVelocityChart();

    }
    private void openVelocityChart() throws IOException {
        loadController("/fxml/VelocityChart.fxml", false);
    }

    public void openAddBullet() throws IOException {
        loadController("/fxml/addNewBullet.fxml", false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GModel="G7";
        RangeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Long value = Math.round(Double.parseDouble(newValue.toString()));
            RangeField.setText(value.toString());
        });

        GCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue.getText();
            GModel = value;
        });
        try {
            TextFields.bindAutoCompletion(NameField, GetBulletNames());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        //dropLabel.setVisible(false);
        //finalVelLabel.setVisible(false);
        GCombo.getItems().add(new Label("G1"));
        GCombo.getItems().add(new Label("G2"));
        GCombo.getItems().add(new Label("G5"));
        GCombo.getItems().add(new Label("G6"));
        GCombo.getItems().add(new Label("G7"));
        GCombo.getItems().add(new Label("G8"));

        //startChart.getXAxis().setTickLength(500);


    }


}
