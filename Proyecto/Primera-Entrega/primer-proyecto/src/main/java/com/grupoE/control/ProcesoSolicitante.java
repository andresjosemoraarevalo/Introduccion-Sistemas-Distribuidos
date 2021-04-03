package com.grupoE.control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
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
            client.connect("tcp://25.93.151.39:" + port);
        } catch (Exception e) {
            System.err.println("No se pudo conectar al servidor" + "\n" + e.getMessage());
        }
    }
    public static void main(String[] args) {
        System.err.println("Conectando al servidor...");
        ProcesoSolicitante ps = new ProcesoSolicitante();
        ps.enviarPeticiones();
    }

    public void enviarPeticiones(){
        List<Peticion> peticiones = new ArrayList<>();
        peticiones = leerPeticiones();
        try{
            for (Peticion peticion : peticiones) {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
                String date = peticion.getFecha().format(dateFormat).toString();
                String msgSend = String.format("%s %s %s",peticion.getIdLibro(), peticion.getTipo().getNumSolicitud(), date);
                client.send(msgSend);
                Thread.sleep(1000);
                String message = client.recvStr(0).trim();
                System.out.println(message);
            }
        } catch (Exception e ){
            System.err.println("No se pudieron enviar las peticiones" + "\n" + e.getMessage());
        }
        
        
    }

    public List<Peticion> leerPeticiones(){
        String actual = System.getProperty("user.dir");
        //String PATH_CSV = actual+"/src/main/java/com/grupoE/peticiones/peticiones.csv";
        String PATH_CSV = actual+"/src/main/java/com/grupoE/peticiones/peticiones_2.csv";
        String line = "";
        List<Peticion> peticiones = new ArrayList<>();
        try{
            br = new BufferedReader(new FileReader(PATH_CSV));
            while((line = br.readLine()) != null){
                String[] peticionR = line.split(",");
                Peticion p = new Peticion();
                TipoPeticion tipo = p.buscarPeticion(Integer.parseInt(peticionR[0]));
                if(tipo!=null){
                    p.setTipo(tipo);
                    p.setIdLibro(Integer.parseInt(peticionR[1].substring(1)));
                    peticiones.add(p);
                }
            }
        }catch(IOException e){
            System.err.println("No se pudo leer el archivo :(" + "\n" + e.getMessage());
        }
        return peticiones;
    }
}