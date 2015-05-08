package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.*;
import org.kevoree.modeling.api.data.manager.AccessMode;
import org.kevoree.modeling.databases.websocket.WebSocketClient;
import org.kevoree.proto.models15.SmartGridModel;
import org.kevoree.proto.models15.SmartGridUniverse;
import org.kevoree.proto.models15.SmartGridView;
import org.kevoree.test.models15.Concentrator;
import org.kevoree.test.models15.SmartGrid;
import org.kevoree.test.models15.SmartMeter;
import org.kevoree.test.models15.meta.MetaConcentrator;
import org.kevoree.test.models15.meta.MetaSmartGrid;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST3_Volatility {

    private int maxChanges = (int) (1313747 * 0.8);


    SmartGridModel smartGridModel;

    public void start() {

        smartGridModel = new SmartGridModel();
        smartGridModel.setContentDeliveryDriver(new WebSocketClient("ws://localhost:8080"));

        smartGridModel.connect().then(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    final int[] changes = {0};
                    SmartGridUniverse baseUniverse = smartGridModel.universe(0);
                    SmartGridView smartGridView = baseUniverse.time(0);
                    smartGridView.getRoot().then(new Callback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject != null) {
                                SmartGrid smartGridRoot = (SmartGrid) kObject;
                                long start = System.nanoTime();
                                smartGridRoot.visit(VisitRequest.CONTAINED, new ModelVisitor() {
                                    @Override
                                    public VisitResult visit(KObject kObject1) {
                                        if (kObject1 instanceof SmartMeter) {
                                            SmartMeter meter = (SmartMeter) kObject1;
                                            meter.setName("re");

                                        }
                                        changes[0]++;
                                        if (changes[0] >= maxChanges) {
                                            long endChange = System.nanoTime();
                                            smartGridModel.save().then(new Callback<Throwable>() {
                                                @Override
                                                public void on(Throwable throwable) {
                                                    long endSave = System.nanoTime();
                                                    System.out.println("Change time:" + ((endChange - start) / 1000000) + " Save time:" + ((endSave - endChange) / 1000000) +" changed:" + changes[0]);
                                                }
                                            });
                                            return VisitResult.STOP;
                                        }
                                        return VisitResult.CONTINUE;
                                    }
                                });
                                System.out.println("out");


                            } else {
                                System.err.println("Root not found");
                            }
                        }
                    });
                }
            }
        });
    }


    public void stop(Runnable next) {
        smartGridModel.manager().cdn().close(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (next != null) {
                    next.run();
                }
            }
        });
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
            TEST3_Volatility c = new TEST3_Volatility();
            c.start();
        }
    }

}
