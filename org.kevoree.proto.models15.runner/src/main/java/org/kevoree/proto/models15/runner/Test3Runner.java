package org.kevoree.proto.models15.runner;


import org.kevoree.modeling.api.Callback;
import org.kevoree.proto.models15.client.TEST1_Client;
import org.kevoree.proto.models15.client.TEST3_ClientOpPerSec;
import org.kevoree.proto.models15.server.TEST1_Server;

/**
 * Created by gnain on 07/05/15.
 */
public class Test3Runner {

    public static void main(String[] args) {

        TEST1_Server server = new TEST1_Server();
        server.start(5, 7, 10, new Runnable() {
            public void run() {
                TEST3_ClientOpPerSec client = new TEST3_ClientOpPerSec();
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
