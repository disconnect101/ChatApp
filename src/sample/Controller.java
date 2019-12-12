package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;


public class Controller {
    @FXML
    TextArea textArea;
    @FXML
    RadioButton popServer,popClient;
    @FXML
    Button send,login,signUP,config,makeServer;
    @FXML
    TextField textfield,popIP,popPort,loginID,loginPW,signUpLoginID,signUpPW;
    ToggleGroup tg = new ToggleGroup();
    private Stage stage;

    private static int port;
    private static String ip,port,lo;
    private Controller configcontroller,parentcontroller;
    private JdbcConnection jdbccon;
    private Connection DBconn;


    private static Chatconnection con = null;
    private static ArrayList<ParallelReceiver> prarray = new ArrayList<>();
    private static ArrayList<Chatconnection> connarray = new ArrayList<>();

    public void openConfig(javafx.event.ActionEvent actionEvent){
        stage = new Stage();
        //stage = (Stage)config.getScene().getWindow();
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("login.fxml"));
            root = (Parent)loader.load();
        }
        catch(Exception e){
            System.out.println("Something went wrong");
            textArea.appendText("Something went wrong" + "\n");
            e.printStackTrace();
        }

        configcontroller = loader.getController();
        {   //initialisations
            configcontroller.parentcontroller = this;
            configcontroller.textArea = textArea;
            configcontroller.popIP.setDisable(true);
            configcontroller.popPort.setDisable(false);
            configcontroller.loginID.setDisable(true);
            configcontroller.loginPW.setDisable(true);
            configcontroller.signUpLoginID.setDisable(true);
            configcontroller.signUpPW.setDisable(true);
            configcontroller.login.setDisable(true);
            configcontroller.signUP.setDisable(true);
            configcontroller.makeServer.setDisable(true);
            configcontroller.stage = stage;
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(config.getScene().getWindow());
        stage.showAndWait();

        /*popServer.setToggleGroup(tg);
        popClient.setToggleGroup(tg);*/
        //login.setDisable(true);
    }

    public void serverClient(javafx.event.ActionEvent actionEvent){
        if(popServer.isSelected()){
            popIP.setDisable(true);
            popPort.setDisable(false);
            loginID.setDisable(true);
            loginPW.setDisable(true);
            signUpLoginID.setDisable(true);
            signUpPW.setDisable(true);
            login.setDisable(true);
            signUP.setDisable(true);
            makeServer.setDisable(false);
        }
        if(popClient.isSelected()){
            popIP.setDisable(false);
            popPort.setDisable(false);
            loginID.setDisable(false);
            loginPW.setDisable(false);
            signUpLoginID.setDisable(false);
            signUpPW.setDisable(false);
            login.setDisable(false);
            signUP.setDisable(false);
            makeServer.setDisable(true);
        }
    }

    public void makeServer(javafx.event.ActionEvent actionEvent) throws SQLException {
        parentcontroller.popPort.setText(popPort.getText());

        parentcontroller._makeServer(actionEvent);
        stage.close();

    }


    public void login(javafx.event.ActionEvent actionEvent) throws SQLException {
        if(loginID.getText().equals("") || loginPW.getText().equals("") || popIP.getText().equals("") || popPort.getText().equals("")){
            return ;
        }

        DBconn = jdbccon.getConnection();
        Statement st = DBconn.createStatement();
        st.execute("USE chatapp;");

        String loginid = loginID.getText();
        String pw = loginPW.getText();

        ResultSet rs = st.executeQuery("SELECT COUNT(loginID) AS cnt FROM `login` WHERE loginID='" + loginid + "' and password='" + pw + "';");
        int cnt = rs.getInt("login");
        if(cnt == 0){
            textArea.appendText("Wrong loginID or password: cannot login" + "\n");
            stage.close();
        }

        parentcontroller.popIP.setText(popIP.getText());
        parentcontroller.popPort.setText(popPort.getText());
        parentcontroller.loginID.setText(loginID.getText());
        parentcontroller.loginPW.setText(loginPW.getText());
        parentcontroller.signUpLoginID.setText(signUpLoginID.getText());
        parentcontroller.signUpPW.setText(signUpPW.getText());
        parentcontroller.login.setText(login.getText());
        parentcontroller.signUP.setText(signUP.getText());
        parentcontroller.makeServer.setText(makeServer.getText());

        parentcontroller._login(actionEvent);
        stage.close();
    }



    public void signUp(javafx.event.ActionEvent actionEvent) throws SQLException {
        if(signUpLoginID.getText().equals("") || signUpPW.getText().equals("") || popIP.getText().equals("") || popPort.getText().equals("")){
            return ;
        }

        DBconn = jdbccon.getConnection();
        Statement st = DBconn.createStatement();
        st.execute("USE chatapp;");
        String loginid = signUpLoginID.getText();
        String pw = signUpPW.getText();
        st.execute("INERT INTO `login` VALUES('" + loginid + "','" + pw + "');");

        parentcontroller.popIP.setText(popIP.getText());
        parentcontroller.popPort.setText(popPort.getText());
        parentcontroller.loginID.setText(loginID.getText());
        parentcontroller.loginPW.setText(loginPW.getText());
        parentcontroller.signUpLoginID.setText(signUpLoginID.getText());
        parentcontroller.signUpPW.setText(signUpPW.getText());
        parentcontroller.login.setText(login.getText());
        parentcontroller.signUP.setText(signUP.getText());
        parentcontroller.makeServer.setText(makeServer.getText());

        parentcontroller._signUp(actionEvent);
        stage.close();
    }




    public void _makeServer(javafx.event.ActionEvent actionEvent){
        serverOn(actionEvent);
    }

    public void _login(javafx.event.ActionEvent actionEvent){
        clientOn(actionEvent);
    }

    public void _signUp(javafx.event.ActionEvent actionEvent){
        clientOn(actionEvent);
    }





    public void serverOn(javafx.event.ActionEvent actionEvent) {
            port = Integer.parseInt(popPort.getText());

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    ServerSocket ss = null;
                    try {
                        ss = new ServerSocket(port);
                    } catch (IOException e) {
                        textArea.appendText("Could not set up server, Maybe another server or service is already running on this port; enter different port or ip" + "\n");
                        e.printStackTrace();
                    }
                    while (true) {
                        int size = connarray.size();
                        try {
                            con = new Chatconnection(null, port, ss);
                            connarray.add(con);
                            con.conID = size;
                            con.sendMessage(String.valueOf(size));
                        } catch (Exception e) {
                            System.out.print("Could not connect : ");
                            e.printStackTrace();
                            return;
                        }
                        ParallelReceiver pr = new ParallelReceiver(con, connarray, textArea);
                        prarray.add(pr);
                        pr.start();
                    }
                }
            });
            t.start();
    }





    public void clientOn(javafx.event.ActionEvent actionEvent) {
            port = Integer.parseInt(popPort.getText());
            ip = popIP.getText();
            try {
                con = new Chatconnection(ip, port, null);
                System.out.println("con : " + con);
                con.conID = Integer.parseInt(con.receiveMessage());
                System.out.println("conID : " + con.conID);
            } catch (Exception ex) {
                System.out.print("could not connect : ");
                ex.printStackTrace();
                return;
            }

            ParallelClientReceiver pcr = new ParallelClientReceiver(con, textArea);
            pcr.start();

        /*while(true){
            Scanner sc = new Scanner(System.in);
            String s = sc.nextLine();
            try{
                s = "Client." + String.valueOf(con.conID) + ": " + s ;
                con.sendMessage(s);
            }
            catch (Exception ex){
                System.out.println("Could not send : ");
                ex.printStackTrace();
            }
        }*/
    }



    public void sendMssg(javafx.event.ActionEvent actionEvent) {
        String s = textfield.getText();
        textfield.clear();
        textArea.appendText("You: " + s + "\n");
        System.out.println(con);

        try{
            s = "Client." + con.conID + ": " + s ;
            System.out.println(con);
            con.sendMessage(s);
        }
        catch (Exception ex){
            System.out.println("Could not send : ");
            ex.printStackTrace();
            return;
        }
    }


    @FXML
    public void initialize() throws SQLException {
        jdbccon = new JdbcConnection("root","","localhost");    }

}


