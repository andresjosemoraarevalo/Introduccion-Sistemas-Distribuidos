package com.grupoE.control;

import java.nio.charset.StandardCharsets;


import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GestorDeCarga {
    private ZContext context;
    private ZMQ.Socket server;

    public GestorDeCarga(){
        try{
            context = new ZContext();
            server = context.createSocket(SocketType.REP);
            int port = 5556;
            server.bind("tcp://*:"+port);
            
        } catch (Exception e){
            System.err.println("No se pudo inicializar el servidor");
        }
    }
    public static void main(String[] args) {
        GestorDeCarga gc = new GestorDeCarga();
        gc.leerProcesosSolicitantes();
    }
    private void leerProcesosSolicitantes(){
        try{
            while(!Thread.currentThread().isInterrupted()){
                byte[] message = server.recv();
                String decodeMessage = new String(message, StandardCharsets.UTF_8);
                System.out.println(decodeMessage);
                Thread.sleep(1000);
                server.send("world");
            }
    
        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje");
        }
    }
}