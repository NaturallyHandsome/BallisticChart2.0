//This file establish a connection with SQLite, doesn't define the Bullet class

package sample;
import java.sql.*;
import java.util.ArrayList;

import static java.lang.String.valueOf;
import static sample.PhyConstants.MACH_MS;

public class CartridgeConnection {

    private static Connection con;

    static Bullet selectedBullet(String name) throws SQLException, ClassNotFoundException {
        if (con == null) getConnection();
        PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM BulletTable WHERE name=?;");
        preparedStatement.setString(1,name);
        ResultSet resultSet=preparedStatement.executeQuery();
        return new Bullet(resultSet.getString("name"), resultSet.getDouble("mass"),
                resultSet.getString("bc"), resultSet.getDouble("caliber"), resultSet.getDouble("muzzleVelocity"));
    }

    private static void getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:CartridgeDB.db");
    }

    /*
    // shall not be used anymore
    @Deprecated static ArrayList<CdFunctions> GetGFunctions() throws SQLException, ClassNotFoundException {
        ArrayList<CdFunctions> gFunctions = new ArrayList<>();
        if (con == null) getConnection();
        Statement statement = con.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * from GFunctionMach");
     /*   while (resultSet.next()){
            CdFunctions gf = new CdFunctions();
            gf.Speed=resultSet.getDouble("ma") * MACH_MS.get(); //converts mach to m/s
            gf.G1 = resultSet.getDouble("G1");
            gf.G2 = resultSet.getDouble("G2");
            gf.G5 = resultSet.getDouble("G5");
            gf.G6 = resultSet.getDouble("G6");
            gf.G7 = resultSet.getDouble("G7");
            gf.G8 = resultSet.getDouble("G8");
            gFunctions.add(gf);
        }
        return gFunctions;
    }
    */
    static ArrayList<String> GetBulletNames() throws SQLException, ClassNotFoundException {
        ArrayList<String> bulletNames=new ArrayList<>();
        if(con==null)
            getConnection();
        Statement state = con.createStatement();
        ResultSet resultSet = state.executeQuery("SELECT name from BulletTable");
        String temp;
        while ( resultSet.next() ){
            temp=resultSet.getString("name");
            bulletNames.add(temp);
        }
        return bulletNames;
    }

    static void addBulletToDB(Bullet bullet) throws SQLException, ClassNotFoundException {
        if(con==null) getConnection();
        PreparedStatement prep= con.prepareStatement("INSERT INTO BulletTable values(?,?,?,?,?,?);");
        prep.setString(2, valueOf(String.valueOf(bullet.getName())));
        prep.setString(3, valueOf(String.valueOf(bullet.getMass())));
        prep.setString(4, valueOf(String.valueOf(bullet.getMuzzleVelocity())));
        prep.setString(5, valueOf(String.valueOf(bullet.getCaliber())));
        prep.setString(6, valueOf(String.valueOf(bullet.getBC())));
        prep.execute();
    }


}
