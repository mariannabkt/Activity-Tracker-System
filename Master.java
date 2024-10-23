import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Master {

    /* Define the sockets that receives requests */
    ServerSocket s_App;
    ServerSocket s_Worker;
    /* Define the sockets that is used to handle the connection */
    Socket providerSocket_App;
    Socket providerSocket_Worker;
    public static int worker_num;
    public static ArrayList<ArrayList<ActionsForWorkers>> connections_list = new ArrayList<>();

    /* Gets the number of Workers */
    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        int firstArg = 1;
        if (args.length > 0) {
            try {
                firstArg = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
        worker_num = firstArg;
        new Master().openServer();
    }

    void openServer() {
        try {
            /* Create Server Sockets */
            s_App = new ServerSocket(4321, 100);
            s_Worker = new ServerSocket(4322, 100);

            for (int i = 0; i < worker_num; i++) {
                /* Accept the connection for Workers */
                providerSocket_Worker = s_Worker.accept();
                connections_list.add(new ArrayList<>());

                /* Handle the request */
                Thread d_Workers = new ActionsForWorkers(providerSocket_Worker);
                d_Workers.start();
            }

            new Thread(() -> {
                try {
                    while (true) {
                        /* Accept the connection for Apps */
                        providerSocket_App = s_App.accept();

                        /* Handle the request */
                        Thread d_App = new ActionsForApp(providerSocket_App);
                        d_App.start();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            while (true) {
                /* Accept the connection for Workers */
                providerSocket_Worker = s_Worker.accept();

                /* Handle the request */
                Thread d_Workers = new ActionsForWorkers(providerSocket_Worker);
                d_Workers.start();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket_App.close();
                providerSocket_Worker.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

    public static int getWorkersNum() {
        return worker_num;
    }

    public static ArrayList<ArrayList<ActionsForWorkers>> getConnections_list() {
        return connections_list;
    }
    public static synchronized void setConnections_list(int worker_id, ActionsForWorkers handler) {
        connections_list.get(worker_id).add(handler);
    }
}
