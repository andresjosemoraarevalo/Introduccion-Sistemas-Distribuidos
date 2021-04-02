package com.grupoE;

import java.nio.charset.StandardCharsets;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class GestorDeCarga {
    private ZMQ.Socket server;
    public static void main(String[] args) {
        try(ZContext context = new ZContext()){
            ZMQ.Socket server = context.createSocket(SocketType.REP);
            int port = 5556;
            server.bind("tcp://*:"+port);
            while(!Thread.currentThread().isInterrupted()){
                byte[] message = server.recv();
                String decodeMessage = new String(message, StandardCharsets.UTF_8);
                System.out.println(decodeMessage);
                Thread.sleep(1000);
                server.send("world");
            }
        } catch (Exception e){
            System.err.println("No se pudo inicializar el servidor");
        }
    }
}
