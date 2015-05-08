package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.databases.websocket.WebSocketClient;
import org.kevoree.proto.models15.*;
import org.kevoree.test.models15.SmartGrid;

/**
 * Created by gnain on 07/05/15.
 */
/*
public class TEST3_Client {

    private long rootUuid;
    public long originOfTime = 0L;
    private String clientId;


    public void start() {

        SmartGridModel smartGridModel = new SmartGridModel();
        smartGridModel.setContentDeliveryDriver(new WebSocketClient("ws://localhost:8080"));

        smartGridModel.connect().then(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    SmartGridUniverse baseUniverse = smartGridModel.universe(0);
                    SmartGridView smartGridView = baseUniverse.time(originOfTime);
                    smartGridView.getRoot().then(new Callback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject != null) {
                                SmartGrid smartGridRoot = (SmartGrid) kObject;
                               // int sizeOfMeters = smartGridRoot.sizeOfMeters();
                                //long start = System.nanoTime();

                            } else {
                                System.err.println("Root not found");
                            }
                        }
                    });
                }
            }
        });
    }

    public static void main(String[] args) {
        for(int i = 0; i < 1; i++) {
            TEST3_Client c = new TEST3_Client();
            c.start();
        }
    }

}
*/
