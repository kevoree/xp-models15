package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.databases.websocket.WebSocketClient;
import org.kevoree.proto.models15.SmartGridModel;
import org.kevoree.proto.models15.SmartGridUniverse;
import org.kevoree.proto.models15.SmartGridView;
import org.kevoree.test.models15.SmartGrid;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST3_ClientOpPerSec {

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
                                //int sizeOfMeters = smartGridRoot.sizeOfMeters();
                                long start = System.nanoTime();
                                /*
                                smartGridRoot.traversal().traverse(MetaSmartGrid.REF_METERS).withAttribute(MetaSmartMeter.ATT_NAME, "" + (int) (sizeOfMeters / 3)).done().then(new Callback<KObject[]>() {
                                    @Override
                                    public void on(KObject[] kObjects) {
                                        long end = System.nanoTime();
                                        System.out.println("Read:" + ((end-start)/1000000));
                                        SmartMeter meter = (SmartMeter) kObjects[0];
                                        meter.jump(System.currentTimeMillis()).then(new Callback<KObject>() {
                                            @Override
                                            public void on(KObject kObject) {
                                                SmartMeter meter2 = (SmartMeter) kObject;
                                                int opNum = 0;
                                                long timeLimit = System.currentTimeMillis() + 1000;
                                                while (System.currentTimeMillis() <= timeLimit) {
                                                    meter2.setConsumption(opNum);
                                                    smartGridModel.save();
                                                    opNum++;
                                                }
                                                System.out.println("OpNum:" + opNum);
                                            }
                                        });

                                    }
                                });*/
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
        for (int i = 0; i < 1; i++) {
            TEST3_ClientOpPerSec c = new TEST3_ClientOpPerSec();
            c.start();
        }
    }

}
