package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListResourceBundle;

public class Chatconnection {
    //ServerSocket ss;
    Socket s;
    Socket objectsocket;
    DataOutputStream dos;
    DataInputStream dis;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    int server;
    String mssg;
    int conID;
    String loginSTR;
    Chatconnection(String ip,int port,ServerSocket ss) throws Exception{

        if(ip == null){
            //ss = new ServerSocket(port);
            //s = ss.accept();
            objectsocket = ss.accept();
            //dos = new DataOutputStream(s.getOutputStream());
            //dis = new DataInputStream(s.getInputStream());
            oos = new ObjectOutputStream(objectsocket.getOutputStream());
            ois = new ObjectInputStream(objectsocket.getInputStream());
        }
        else {
            //s = new Socket(ip, port);
            objectsocket = new Socket(ip, port);
            //dos = new DataOutputStream(s.getOutputStream());
            //dis = new DataInputStream(s.getInputStream());
            oos = new ObjectOutputStream(objectsocket.getOutputStream());
            ois = new ObjectInputStream(objectsocket.getInputStream());
            System.out.println("construct");
        }
    }
    void setServer(int val){
        this.server = val;
    }
    int isServer(){
        return server;
    }
    void sendMessage(String mssg) throws IOException {
        dos.writeUTF(mssg);
    }
    String receiveMessage() throws IOException {
        mssg = dis.readUTF();
        return mssg;
    }
    void sendObject(Object o) throws IOException {
        oos.writeObject(o);
    }
    Object receiveObject() throws IOException, ClassNotFoundException {
        Object o = ois.readObject();
        return o;
    }
}

class CurrOnline extends Thread{
    ArrayList<Chatconnection> conarray;
    Message message;
    CurrOnline(ArrayList<Chatconnection> conarray){
        this.conarray = conarray;
    }

    @Override
    public void run() {
        super.run();
        while(true) {
            ArrayList<String> currlogins = new ArrayList<>();
            for (Chatconnection cc : conarray) {
                currlogins.add(cc.loginSTR);
            }
            message = new Message();
            message.setCurrlogins(currlogins);
            message.setLoginInfo(true);

            for (Chatconnection cc : conarray) {
                try {
                    cc.sendObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class ParallelReceiver extends Thread{
    Chatconnection con;
    ArrayList<Chatconnection> connarray;
    TextArea textarea;
    String loginSTR;
    Message message;

    public ParallelReceiver(Chatconnection con, ArrayList<Chatconnection> connarray, TextArea textArea) throws IOException, ClassNotFoundException {
        this.textarea = textArea;
        this.con = con;
        this.connarray = connarray;
        message = (Message) con.receiveObject();
        con.loginSTR = message.getMsg();
    }

    @Override
    public void run() {
        super.run();
        while(true){
            Message message = null;
            try {
                message = (Message) con.receiveObject();
            }
            catch (Exception e){
                System.out.print("Could not receive : ");
                e.printStackTrace();
                //System.exit(0);
                System.out.println("Client " + con.conID + ": went offline");
                textarea.appendText("Client " + con.conID + ": went offline" + "\n");
                connarray.remove(con);
                try {
                    con.objectsocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            String displaymssg = con.loginSTR + " :" + message.getMsg();
            System.out.println("Client" + con.conID + " :" + message.getMsg());
            textarea.appendText("Client" + con.conID + " :" + message.getMsg() + "\n");
            for(Chatconnection c : connarray){
                if(c.conID != con.conID) {
                    try {
                        message.setMsg(displaymssg);
                        c.sendObject(message);
                    } catch (Exception e) {
                        System.out.print("Could not send : ");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

class ParallelClientReceiver extends Thread{
    Chatconnection con;
    TextArea textArea,listtext  ;
    ListView<String> listView;
    ParallelClientReceiver(Chatconnection con,TextArea textArea,ListView<String> listView) throws IOException, ClassNotFoundException {
        this.textArea = textArea;
        this.con = con;
        this.listView = listView;
        Message message = (Message) con.receiveObject();
        con.conID = Integer.parseInt(message.getMsg());
        System.out.println("conID : " + con.conID);
        message.setMsg(con.loginSTR);
        con.sendObject(message);
    }
    @Override
    public void run() {
        super.run();
        while (true) {
            Message message = null;
            try {
                message = (Message) con.receiveObject();
            } catch (Exception e) {
                System.out.print("could not recieve : ");
                e.printStackTrace();
                textArea.appendText("Could not receive message, server maybe offline" + "\n");
                return ;
            }

            if(message.isLoginInfo()){
                //listView.getItems().removeAll(listView.getItems());
                //listtext.clear();
                listView.getItems().clear();
                ArrayList<String> currloggedin = message.getCurrlogins();
                listView.getItems().addAll(currloggedin);
                System.out.println(currloggedin);
                /*for(String s : currloggedin) {
                    listtext.appendText(s + "\n");
                }*/
            }
            else {
                System.out.println(message.getMsg());
                textArea.appendText(message.getMsg() + "\n");
            }
        }
    }
}

class Message implements Serializable{

    private String msg;
    private ArrayList<String> currlogins;
    private String from,to;
    private boolean islogininfo;


    void setMsg(String str){
        msg = str;
    }
    void setCurrlogins(ArrayList<String> currlogins){
        this.currlogins = currlogins;
    }
    String getMsg(){
        return msg;
    }
    ArrayList<String> getCurrlogins(){
        return currlogins;
    }
    boolean isLoginInfo(){
        return islogininfo;
    }
    void setLoginInfo(boolean b){
        islogininfo = b;
    }
}