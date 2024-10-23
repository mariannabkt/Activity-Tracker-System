import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Worker extends Thread {
    String server_ip;
    int worker_id;
    ArrayList<Mapper> chunks = new ArrayList<>();

    /* Gets the IP address of the Server and its personal ID */
    public static void main(String[] args) {
        String ipArg = "";
        int idArg = 0;
        if (args.length == 2) {
            try {
                ipArg = args[0];
                idArg = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
        else {
            System.err.println("You must give the IP address of the server and the Worker ID.");
            return;
        }
        new Worker(ipArg, idArg).start();
    }

    Worker(String server_ip, int worker_id) {
        this.server_ip = server_ip;
        this.worker_id = worker_id;
    }

    public void run() {
        ObjectOutputStream out;
        ObjectInputStream in;
        Socket requestSocket;

        try {
            String host = server_ip;
            /* Create socket for contacting the server on port 4322*/
            requestSocket = new Socket(host,4322);
            System.out.println("Connected to sever.");

            /* Create the streams to send and receive data from server */
            in = new ObjectInputStream(requestSocket.getInputStream());
            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeInt(this.worker_id);
            out.flush();

            while (requestSocket.isConnected()) {

                int chunk_num = in.readInt();

                if (chunk_num == 1) {
                    // new Worker thread -> new connection
                    new Worker(this.server_ip, this.worker_id).start();
                }

                String fileName = in.readUTF();

                System.out.println(fileName);


                int bytes;
                FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.dir") + "\\worker_directory\\" + fileName);

                long size = in.readLong(); // read file size
                byte[] buffer = new byte[4 * 1024];
                while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {

                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes; // read up to file size
                }

                fileOutputStream.close();

                boolean last_chunk = in.readBoolean();

                /* Create a new Thread to process each Chunk */
                Mapper mapper = new Mapper(out, fileName, System.getProperty("user.dir") + "\\worker_directory\\");
                chunks.add(mapper);
                Thread thread = new Thread(mapper);
                thread.start();

                /* If all Mappers have completed processing the Chunks break the loop */
                boolean processing_completed = false;
                if (last_chunk) {
                    int mappers_completed = 0;
                    while (!processing_completed) {
                        for (Mapper eachMapper : chunks) {
                            if (eachMapper.isCompleted()) {
                                mappers_completed++;
                            }
                        }
                        if (mappers_completed == chunks.size()) {
                            processing_completed = true;
                        }
                    }
                }

                if (processing_completed) {
                    break;
                }
            }

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
}
