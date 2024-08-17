package sample;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;

import java.sql.SQLException;

import static sample.CartridgeConnection.addBulletToDB;

public class NewBulletController {
    @FXML private JFXTextField NameField;
    @FXML private JFXTextField massField;
    @FXML private JFXTextField speedField;
    @FXML private JFXTextField caliberField;
    @FXML private JFXTextField bcField;

    public void addBulletToDButton() throws SQLException, ClassNotFoundException {
        addBulletToDB(new Bullet(String.valueOf(NameField.getText()), Double.parseDouble(String.valueOf(massField.getText())),
                String.valueOf(bcField.getText()), Double.parseDouble(String.valueOf(caliberField.getText())), Double.parseDouble(String.valueOf(speedField.getText()))));
    }
}
