package com.grupoE.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;
import com.grupoE.entity.Libro;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GestorBD {
    private ZContext context;
    public static ZMQ.Socket client_dev;
    public static ZMQ.Socket client_pres;
    public static ZMQ.Socket client_rnv;
    public static ZMQ.Socket client_dev_local;
    public static ZMQ.Socket client_pres_local;
    public static ZMQ.Socket client_rnv_local;
    public static ZMQ.Socket pres_persist;
    public static ZMQ.Socket pres_persist_local;
    
    private HashMap<Integer,Libro> libros = new HashMap<>();
    private BufferedReader br;

    public GestorBD(String opcion){
        try{
            String direccion;
            if(opcion.equals("A")){
                //Usando Hamachi A 
                direccion = "25.92.125.22";
            }else if(opcion.equals("B")){
                //Usando Hamachi B
                direccion = "25.104.197.200";
            }else{
                direccion = opcion;
            }
            //Se establece un contexto ZeroMQ
            context= new ZContext();

            //Crea socket tipo SUB
            client_dev = context.createSocket(SocketType.SUB);
            int portDEV = 8886;
            //Ata el socket a el puerto
            //Usando localhost
            client_dev.connect("tcp://"+ direccion + ":" + portDEV);
            //client_dev.connect("tcp://25.92.125.22:" + port);
            String filter = "1";
            client_dev.subscribe(filter.getBytes(Charset.forName("UTF-8")));

            //Crea socket tipo SUB
            client_dev_local = context.createSocket(SocketType.SUB);
            //Ata el socket a el puerto
            //Usando localhost
            client_dev_local.connect("tcp://localhost:" + portDEV);
            //client_dev.connect("tcp://25.92.125.22:" + port);
            client_dev_local.subscribe(filter.getBytes(Charset.forName("UTF-8")));

            //Crea socket tipo SUB
            pres_persist = context.createSocket(SocketType.SUB);
            int portPRES_per = 8887;
            //Ata el socket a el puerto
            //Usando localhost
            pres_persist.connect("tcp://"+ direccion + ":" + portPRES_per);
            //client_dev.connect("tcp://25.92.125.22:" + port);t
            filter = "4";
            pres_persist.subscribe(filter.getBytes(Charset.forName("UTF-8")));

            //Crea socket tipo SUB
            pres_persist_local = context.createSocket(SocketType.SUB);
            //Ata el socket a el puerto
            //Usando localhost
            pres_persist_local.connect("tcp://localhost:" + portPRES_per);
            //client_dev.connect("tcp://25.92.125.22:" + port);
            filter = "3";
            pres_persist.subscribe(filter.getBytes(Charset.forName("UTF-8")));

            //Crea socket tipo REP
            client_pres = context.createSocket(SocketType.REP);
            int portPRES = 8888;
            //Ata el socket a el puerto
            //Usando el localhost abre el puerto TCP para todas las interfaces disponibles
            client_pres.bind("tcp://"+ direccion + ":" + portPRES); 
            //Usando hamachi
            //clientAP.bind("tcp://25.93.151.39:"+portAP);
            
            //Crea socket tipo REP
            client_pres_local = context.createSocket(SocketType.REP);
            int portPRES_local = 8889;
            //Ata el socket a el puerto
            //Usando el localhost abre el puerto TCP para todas las interfaces disponibles
            client_pres.bind("tcp://localhost:" + portPRES_local); 
            //Usando hamachi
            //clientAP.bind("tcp://25.93.151.39:"+portAP);

            //Crea socket tipo SUB
            client_rnv = context.createSocket(SocketType.SUB);
            int portRNV = 9886;
            //Ata el socket a el puerto
            //Usando localhost
            client_rnv.connect("tcp://"+ direccion + ":" + portRNV);
            filter = "2";
            //client_rnv.connect("tcp://25.92.125.22:" + port);
            client_rnv.subscribe(filter.getBytes(Charset.forName("UTF-8")));
        
            //Crea socket tipo SUB
            client_rnv_local = context.createSocket(SocketType.SUB);
            //Ata el socket a el puerto
            //Usando localhost
            client_rnv_local.connect("tcp://localhost:" + portRNV);
            filter = "2";
            //client_rnv.connect("tcp://25.92.125.22:" + port);
            client_rnv_local.subscribe(filter.getBytes(Charset.forName("UTF-8")));
        } catch (Exception e) {
            System.err.println("No se pudo conectar al servidor" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
    public static void main(String[] args) {
        if(args.length==0){ // Verifica que se ingrese los argumentos correctos
            System.out.println("Ingrese: java [path] [sede]");
            System.out.println("La sede puede ser A, B o la que desee (XXX.XXX.XXXX.XXXX)");
            System.exit(-1);
        }
        System.out.println("Conectando al servidor...");
        // Se crea el contexto, el socket y se ata a un puerto
        GestorBD ar = new GestorBD(args[0]);
        // Envia las peticiones al servidor con el patrón requesr-reply
        ar.leerCambios();
        ar.cargarLibros();
        ar.persistirLibros();
    }
    public void leerCambios(){ 
        try{
            Thread hilo = new MsgDevolucion("BDExternaDev");
            Thread hilo2 = new MsgPrestamo("BDExternaPres");
            Thread hilo3 = new MsgRenovacion("BDExternaRnv");
            Thread hilo4 = new GestPrestamo("BDPresExt");
            
            Thread hilo_local = new MsgDevolucionL("BDLocalDev");
            Thread hilo2_local = new MsgPrestamoL("BDLocalPres");
            Thread hilo3_local = new MsgRenovacionL("BDLocalRnv");
            Thread hilo4_local = new GestPrestamoL("BDPresL");
            

            hilo.start();
            hilo2.start();
            hilo3.start();
            hilo4.start();

            hilo_local.start();
            hilo2_local.start();
            hilo3_local.start();
            hilo4_local.start();
        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje" + e.getMessage());
            System.exit(-1);
        }
    }
    public void cargarLibros(){
        String actual = System.getProperty("user.dir");
        //SE DEBE CAMBIAR DEPENDIENDO SI ES WINDOWS O LINUX
        // - PARA LINUX
        //String PATH_CSV = actual+"/Primera-Entrega/primer-proyecto/src/DB/libros.csv"; // Si se va a leer peticiones
        String PATH_CSV = actual+"/primer-proyecto/src/DB/libros.csv"; // Si se va a leer peticiones 2
        // - PARA WINDOWS
        //PATH_CSV.replace('/', '\\'); // QUITAR COMENTARIO
        String line = "";
        try{
            br = new BufferedReader(new FileReader(PATH_CSV));//se lee el archivo
            while((line = br.readLine()) != null){
                String[] peticionR = line.split(","); // Se separa la linea por comas
                Libro l = new Libro();
                l.setIdLibro(Integer.parseInt(peticionR[0].replace(" ", "")));
                l.setTitulo(peticionR[1].replace(" ", ""));
                l.setAutor(peticionR[2].replace(" ", ""));
                l.setISBN(peticionR[3].replace(" ", ""));
                l.setEditorial(peticionR[4].replace(" ", ""));
                l.setIdioma(peticionR[5].replace(" ", ""));
                // Da formato a la fecha
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy"); 
                // Obtiene la fecha de la petición y le da formato
                l.setCategoria(peticionR[6].replace(" ", ""));
                l.setEstado(Boolean.valueOf(peticionR[7].replace(" ", "")));
                l.setNumEjemplares(Integer.parseInt(peticionR[8].replace(" ", "")));
                l.setFechaPublicacion(LocalDate.parse(peticionR[9].replace(" ", ""), dateFormat));
                libros.put(l.getIdLibro(), l);
            }
        }catch(IOException e){
            System.err.println("No se pudo leer el archivo :(" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
    public void persistirLibros(){
        String actual = System.getProperty("user.dir");
        try (PrintWriter writer = new PrintWriter(new File(actual+"/primer-proyecto/src/DB/libros.csv"))) {
            StringBuilder sb = new StringBuilder();
            for (Integer n : libros.keySet()) {
                sb.append(libros.get(n).toString());
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}

class MsgDevolucion extends Thread{
    public MsgDevolucion(String msg){
        super(msg);
    }
    public void run(){ 
        while(true){
            String str_dev = GestorBD.client_dev.recvStr(0).trim();
            String peticionStr = str_dev;
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
            System.out.println(System.currentTimeMillis());
        }
        
    }
}
class MsgPrestamo extends Thread{
    public MsgPrestamo(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_pres = GestorBD.pres_persist.recvStr(0).trim();
            String peticionStr = str_pres;
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
            System.out.println(System.currentTimeMillis());
        }
    }
}
class MsgRenovacion extends Thread{
    public MsgRenovacion(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_rnv = GestorBD.client_rnv.recvStr(0).trim();
            String peticionStr = str_rnv;
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
            System.out.println(System.currentTimeMillis());
        }
    }
}
class MsgDevolucionL extends Thread{
    public MsgDevolucionL(String msg){
        super(msg);
    }
    public void run(){ 
        while(true){
            String str_dev = GestorBD.client_dev_local.recvStr(0).trim();
            String peticionStr = str_dev;
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
            System.out.println(System.currentTimeMillis());
        }
    }
}
class MsgPrestamoL extends Thread{
    public MsgPrestamoL(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_pres = GestorBD.pres_persist_local.recvStr(0).trim();
            String peticionStr = str_pres;
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
            System.out.println(System.currentTimeMillis());
        }
    }
}
class MsgRenovacionL extends Thread{
    public MsgRenovacionL(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_rnv = GestorBD.client_rnv_local.recvStr(0).trim();
            String peticionStr = str_rnv;
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
            System.out.println(System.currentTimeMillis());
        }
    }
}
class GestPrestamoL extends Thread{
    public GestPrestamoL(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_pres = GestorBD.client_pres_local.recvStr(0).trim();
            String peticionStr = str_pres;
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
            GestorBD.client_pres_local.send("true");
            System.out.println(System.currentTimeMillis());
        }
    }
}
class GestPrestamo extends Thread{
    public GestPrestamo(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_pres = GestorBD.client_pres.recvStr(0).trim();
            String peticionStr = str_pres;
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
            GestorBD.client_pres.send("true");
            System.out.println(System.currentTimeMillis());
        }
    }
}