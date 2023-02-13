package part3;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
public class ServerTCP {
    private static final int MAX_Client_MESSAGE_LENGTH = 1024;
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Usage: ServerTCP <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));


        while(true){
            //accept() is a blocking call
            //it will return only when it receives a new
            //connection request from a client
            //accept() performs the three-way handshake
            //with the client before it returns
            SocketChannel serveChannel = listenChannel.accept();

            ByteBuffer buffer = ByteBuffer.allocate(MAX_Client_MESSAGE_LENGTH);


            //ensures that we read the whole message
            while(serveChannel.read((buffer)) >= 0);
            buffer.flip();
            /*
            while(serveChannel.read((buffer2)) >= 0);
            buffer2.flip();
            */
            //get the first character from the client message
            char command = (char)buffer.get();
            System.out.println("Command from client: "+ command);
            switch (command){
                case 'G':
                    //"Get": client wants to get the file
                    byte[] a = new byte[buffer.remaining()];
                    // copy the rest of the client message (i.e., the file name)
                    // to the byte array
                    buffer.get(a);
                    String fileName = new String(a);
                    File file = new File(fileName);
                    System.out.println("The requested File name is: " + file);
                    if (!file.exists() || file.isDirectory()) {
                        sendReplyCode(serveChannel, 'N');
                        System.out.println("The reply code is: N");
                    }else{
                        System.out.println("The reply code is Y");
                        sendReplyCode(serveChannel, 'Y');
                        //read contents of file
                        //here
                        BufferedReader br = new BufferedReader(new
                                FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            //write contents of file to client
                            line = line+"\n";
                            serveChannel.write(ByteBuffer.wrap(line.getBytes()));
                        //to here
                        }
                    }
                    serveChannel.close();
                    break;


                case 'D':
                    byte[] b = new byte[buffer.remaining()];
                    buffer.get(b);
                    String delFileName = new String(b);
                    File fileDel = new File(delFileName);
                    if (!fileDel.exists() || fileDel.isDirectory()) {
                        sendReplyCode(serveChannel, 'N');
                    }
                    else{
                        sendReplyCode(serveChannel, 'Y');
                        if (fileDel.delete()){
                            System.out.println("Deleted the File: " + delFileName);
                        }
                        else{
                            System.out.println("Failed to Delete the File ");
                        }
                    }
                    serveChannel.close();
                    break;

                case 'R':
                    byte[] rn = new byte[buffer.remaining()];
                    buffer.get(rn);
                    String seperateString = new String(rn);
                    String[] newString = seperateString.split("|");
                    String old = newString[0];
                    String newS = newString[1];

                    File rnmd = new File(old);

                    //delete old file

                    File newlyNamedFile = new File(newS);

                    if (!rnmd.exists() || rnmd.isDirectory()) {
                        sendReplyCode(serveChannel, 'N');
                    }
                    else {
                        sendReplyCode(serveChannel, 'Y');
                        Path source = Paths.get("C:\\Users\\seand\\316 part 3\\"+rnmd);
                        try{
                            Files.move(source,source.resolveSibling(String.valueOf(newlyNamedFile)));

                        }catch(IOException e)
                        {

                        }
                        System.out.println("File was renamed to " + newlyNamedFile);
                    }
                    //a little unconventional, i can send a stack overflow article i based this on if you need
                    serveChannel.close();

                    break;


                case 'Q':
                    byte[] e = new byte[buffer.remaining()];
                    buffer.get(e);
                    System.out.println("Thank you for using our client/server program!");
                    System.exit(0);
                    serveChannel.close();
                    break;


                case 'L':
                    byte[] l = new byte[buffer.remaining()];
                    buffer.get(l);
                    File[] filesList = new File(":\\Users\\seand\\316 part 3").listFiles();
                    for (File f : filesList){
                        if (!f.isDirectory()){
                            System.out.println(f.getName());
                        }
                    }

                    serveChannel.close();
                    break;

            }
        }
    }
    private static void sendReplyCode (SocketChannel channel, char code) throws
            IOException{
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        channel.write(data);
    }
}