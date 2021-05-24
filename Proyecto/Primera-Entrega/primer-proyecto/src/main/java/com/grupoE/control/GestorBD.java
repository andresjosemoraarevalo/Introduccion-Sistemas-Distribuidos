package com.grupoE.control;

import java.nio.charset.Charset;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GestorBD {
    private ZContext context;
    private ZMQ.Socket client;
    

    public GestorBD(){
        try{
            //Se establece un contexto ZeroMQ
            context= new ZContext();
            //Crea socket tipo SUB
            client = context.createSocket(SocketType.SUB);
            int port = 8886;
            //Ata el socket a el puerto
            //Usando localhost
            client.connect("tcp://*:" + port);
            //client.connect("tcp://25.92.125.22:" + port);
            String filter = "1";
            client.subscribe(filter.getBytes(Charset.forName("UTF-8")));
        } catch (Exception e) {
            System.err.println("No se pudo conectar al servidor" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
    public static void main(String[] args) {
        System.out.println("Conectando al servidor...");
        // Se crea el contexto, el socket y se ata a un puerto
        GestorBD ar = new GestorBD();
        // Envia las peticiones al servidor con el patrón requesr-reply
        ar.leerCambios();
    }
    public void leerCambios(){ 
        try{
            while(!Thread.currentThread().isInterrupted()){
                String peticionStr = client.recvStr(0).trim();
                // Separa la palabra por espacios
                StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
                //Se obtiene topico
                Integer.parseInt(strTok.nextToken());
                // Se obtiene el ID del libro
                int idLibro = Integer.parseInt(strTok.nextToken());
                // Se obtiene el tipo de proceso
                int tipo = Integer.parseInt(strTok.nextToken());
                // Se obtiene la fecha del proceso
                String fecha = strTok.nextToken();
                //Se arma la petición
                Peticion peticionAux = new Peticion(idLibro,tipo,fecha);
                //Se muestra en consola para saber en cual petición va
                System.out.println("BD"+peticionAux.toString());
            }
        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje" + e.getMessage());
            System.exit(-1);
        }
    }
}
