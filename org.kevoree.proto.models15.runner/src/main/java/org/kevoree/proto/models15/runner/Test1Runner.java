package org.kevoree.proto.models15.runner;


import org.kevoree.modeling.api.Callback;
import org.kevoree.proto.models15.client.TEST1_Client;
import org.kevoree.proto.models15.server.TEST1_Server;

/**
 * Created by gnain on 07/05/15.
 */
public class Test1Runner {

    public static void main(String[] args) {

        TEST1_Server server = new TEST1_Server();
        server.start(3, 3, 25000, new Runnable() {
            public void run() {
                TEST1_Client client = new TEST1_Client();
                client.start(new Runnable() {
                    @Override
                    public void run() {
                        client.stop(new Runnable() {
                            @Override
                            public void run() {
                                server.stop(new Callback<Throwable>() {
                                    @Override
                                    public void on(Throwable throwable) {
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });


    }

}
