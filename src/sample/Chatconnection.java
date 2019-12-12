package sample;

import javafx.scene.control.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Chatconnection {
    //ServerSocket ss;
    Socket s;
    DataOutputStream dos;
    DataInputStream dis;
    int server;
    String mssg;
    int conID;
    Chatconnection(String ip,int port,ServerSocket ss) throws Exception{

        if(ip == null){
            //ss = new ServerSocket(port);
            s = ss.accept();
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());
        }
        else {
            s = new Socket(ip, port);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());
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
}


class ParallelReceiver extends Thread{
    Chatconnection con;
    ArrayList<Chatconnection> connarray;
    TextArea textarea;

    public ParallelReceiver(Chatconnection con, ArrayList<Chatconnection> connarray, TextArea textArea) {
        this.textarea = textArea;
        this.con = con;
        this.connarray = connarray;
    }

    @Override
    public void run() {
        super.run();
        while(true){
            String str = null;
            try {
                str = con.receiveMessage();
            }
            catch (Exception e){
                System.out.print("Could not receive : ");
                e.printStackTrace();
                System.exit(0);
            }
            System.out.println(str);
            textarea.appendText(str + "\n");
            for(Chatconnection c : connarray){
                if(c.conID != con.conID) {
                    try {
                        c.sendMessage(str);
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
    TextArea textArea;
    ParallelClientReceiver(Chatconnection con,TextArea textArea){
        this.textArea = textArea;
        this.con = con;
    }
    @Override
    public void run() {
        super.run();
        while (true) {
            String str = null;
            try {
                str = con.receiveMessage();
            } catch (Exception e) {
                System.out.print("could not recieve : ");
                e.printStackTrace();
                textArea.appendText("Could not receive message, server maybe offline" + "\n");
            }
            System.out.println(str);
            textArea.appendText(str + "\n");
        }
    }
}