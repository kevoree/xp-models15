package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KEventListener;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.api.meta.Meta;
import org.kevoree.modeling.databases.websocket.WebSocketClient;
import org.kevoree.proto.models15.*;
import org.kevoree.test.models15.SmartGrid;
import org.kevoree.test.models15.meta.MetaSmartGrid;
import org.kevoree.test.models15.meta.MetaSmartMeter;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST1_Client {

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
                                int sizeOfMeters = smartGridRoot.sizeOfMeters();
                                //long start = System.nanoTime();
                                smartGridRoot.traversal().traverse(MetaSmartGrid.REF_METERS).withAttribute(MetaSmartMeter.ATT_NAME, "" + (int) (sizeOfMeters / 3)).done().then(new Callback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] kObjects) {
                                        long end = System.nanoTime();
                                        //int accessTime = (int)((end - start)/1000000);
                                        //System.out.println(accessTime);
                                        kObjects[0].listen(0, new KEventListener() {
                                            @Override
                                            public void on(KObject src, Meta[] modifications) {
                                                System.out.println(""+(System.currentTimeMillis() - src.now()));
                                            }
                                        });
                                    }
                                });
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
            TEST1_Client c = new TEST1_Client();
            c.start();
        }
    }

}
