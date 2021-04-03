package com.grupoE.control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.grupoE.entity.TipoPeticion;
import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ProcesoSolicitante {
    private ZContext context;
    private ZMQ.Socket client;
    private BufferedReader br;

    public ProcesoSolicitante(){
        try{
            context= new ZContext();
            client = context.createSocket(SocketType.REQ);
            int port = 5556;
            client.connect("tcp://localhost:" + port);
        } catch (Exception e) {
            System.err.println("No se pudo conectar al servidor");
        }
    }
    public static void main(String[] args) {
        System.err.println("Conectando al servidor...");
        ProcesoSolicitante ps = new ProcesoSolicitante();
        ps.enviarPeticiones();
    }

    public void enviarPeticiones(){
        client.send("Hello");
        byte[] message = client.recv();
        String decodeMessage = new String(message, StandardCharsets.UTF_8);
        System.out.println(decodeMessage);
        System.out.println(leerPeticiones());
    }

    public List<Peticion> leerPeticiones(){
        String PATH_CSV = "/home/andres/Documentos/U/Introduccion-Sistemas-Distribuidos/Proyecto/Primera-Entrega/primer-proyecto/peticiones.csv";
        String line = "";
        List<Peticion> peticiones = new ArrayList<>();
        try{
            br = new BufferedReader(new FileReader(PATH_CSV));
            while((line = br.readLine()) != null){
                String[] peticionR = line.split(",");
                Peticion p = new Peticion();
                TipoPeticion tipo = buscarPeticion(Integer.parseInt(peticionR[0]));
                if(tipo!=null){
                    p.setTipo(tipo);
                    p.setIdLibro(Integer.parseInt(peticionR[1].substring(1)));
                    peticiones.add(p);
                }
            }
        }catch(IOException e){
            System.err.println("No se pudo leer el archivo :(");
        }
        return peticiones;
    }

    public TipoPeticion buscarPeticion(int valor){
        switch(valor){
            case 1:
                return TipoPeticion.Devolver;
            case 2:
                return TipoPeticion.Renovar;
            case 3:
                return TipoPeticion.Solicitar;
            default:
                return null;
        }
    }
}