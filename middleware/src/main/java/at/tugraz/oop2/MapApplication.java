package at.tugraz.oop2;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import mapserviceGRPC.mapserviceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class MapApplication {
    private static mapserviceGrpc.mapserviceBlockingStub blockingStubInstance = null;

    //------------------------------------------------------------------------------------------------------------------
    // Getter for Stub singleton which handles the gRPC communication with the backend
    //------------------------------------------------------------------------------------------------------------------
    public static mapserviceGrpc.mapserviceBlockingStub getStub() {
        if (blockingStubInstance != null)
            return blockingStubInstance;
        else
            throw new RuntimeException("No connection is here yet");
    }

    public static void main(String[] args)
    {
        var serverport = System.getenv().getOrDefault("JMAP_MIDDLEWARE_PORT", "8010");
        var backend = System.getenv().getOrDefault("JMAP_BACKEND_TARGET", "localhost:8020");
        int port;

        //Arg parsing
        try {
             port = Integer.parseInt(serverport);
             if(port < 0 || port > 65535)
                 throw new Exception("Invalid range");
        }catch (Exception e)
        {
            System.out.println("Something went wrong with the port. Reverting to defaults...");
            port = 8010;
        }

        MapLogger.middlewareStartup(port, backend);
        try {
            create_backend_conn(backend);
            var app = new SpringApplication((MapApplication.class));
            app.setDefaultProperties(Collections.singletonMap("server.port", port));
            app.run();
        }
        catch (Exception e)
        {
            System.out.println("FATAL: Something went wrong with setting up the connection to the backend or starting" +
                    "the Springboot app: " + e.getMessage());
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Starter function to set up the channel for communication with the backend. If the channel cannot be setup this
    // excepts as I believe it does not make any sence running our Service without any Data :D
    // @param backend_target the Port of the BACKEND channel
    //------------------------------------------------------------------------------------------------------------------
    private static void create_backend_conn(String backend_target)
    {
        if (blockingStubInstance == null)
        {
            try {
                ManagedChannel chann = Grpc.newChannelBuilder(backend_target, InsecureChannelCredentials.create())
                        .build();
                blockingStubInstance = mapserviceGrpc.newBlockingStub(chann);
            }
            catch (Exception e) {
                throw e;
            }
        }
    }
}