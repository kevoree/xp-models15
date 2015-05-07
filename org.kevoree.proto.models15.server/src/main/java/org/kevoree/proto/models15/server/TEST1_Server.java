package org.kevoree.proto.models15.server;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.databases.websocket.WebSocketWrapper;
import org.kevoree.proto.models15.*;
import org.kevoree.test.models15.Concentrator;
import org.kevoree.test.models15.SmartGrid;
import org.kevoree.test.models15.SmartMeter;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST1_Server {

    public static long originOfTime = 0L;

    private SmartGridModel smartGridModel;
    private SmartGridView smartGridView;


    public void start(int modelSize) {

        smartGridModel = new SmartGridModel();
        smartGridModel.setContentDeliveryDriver(new WebSocketWrapper(smartGridModel.manager().cdn(), 8080));
        smartGridModel.connect().then(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    SmartGridUniverse baseUniverse = smartGridModel.universe(0);
                    smartGridView = baseUniverse.time(originOfTime);
                    smartGridView.getRoot().then(new Callback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject == null) {
                                SmartGrid smartGridRoot = smartGridView.createSmartGrid();

                                int sizeOfMeters = 0;
                                int sizeOfConcentrators = modelSize / 20;

                                System.out.println("Creating grid:" + sizeOfConcentrators);
                                for (int i = 0; i < sizeOfConcentrators; i++) {
                                    //New concentrator
                                    Concentrator c = smartGridView.createConcentrator();
                                    //sizeOfConcentrators++;
                                    c.setName("" + i);
                                    smartGridRoot.addConcentrators(c);
                                    if(i%1000==0){
                                        System.out.println("Created:" + i);
                                    }

                                    for (int j = 0; j < 20; j++) {
                                        SmartMeter m = smartGridView.createSmartMeter();
                                        sizeOfMeters++;
                                        m.setName("" + sizeOfMeters);
                                        smartGridRoot.addMeters(m);
                                        c.addMeters(m);
                                    }
                                }

                                final int finalSizeOfConcentrators = sizeOfConcentrators;
                                final int finalSizeOfMeters = sizeOfMeters;
                                System.out.println("Setting root");
                                smartGridView.setRoot(smartGridRoot).then(new Callback<Throwable>() {
                                    @Override
                                    public void on(Throwable throwable) {
                                        if (throwable != null) {
                                            throwable.printStackTrace();
                                        }

                                        smartGridModel.save().then(new Callback<Throwable>() {
                                            @Override
                                            public void on(Throwable throwable) {

                                                if (throwable != null) {
                                                    throwable.printStackTrace();
                                                } else {
                                                    System.out.println("Base model committed: " + (finalSizeOfMeters + finalSizeOfConcentrators + 1));
                                                }
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    public void stop(Callback<Throwable> callback) {
        smartGridModel.manager().cdn().close(callback);
    }


    public static void main(String[] args) {
        new TEST1_Server().start(100000);
    }

}
