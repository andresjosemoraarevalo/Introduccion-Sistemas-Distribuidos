package com.grupoE;

import java.nio.charset.StandardCharsets;

import javax.print.event.PrintEvent;
import javax.sound.sampled.SourceDataLine;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ProcesoSolicitante {

    public static void main(String[] args) {
        System.err.println("Conectando al servidor...");
        try(ZContext context = new ZContext()){
            ZMQ.Socket client = context.createSocket(SocketType.REQ);
            int port = 5556;
            client.connect("tcp://localhost:"+port);
            client.send("Hello");
            byte[] message = client.recv();
            String decodeMessage = new String(message, StandardCharsets.UTF_8);
            System.out.println(decodeMessage);
        } catch (Exception e) {
            System.err.println("No se pudo conectar al servidor");
        }
    }
}