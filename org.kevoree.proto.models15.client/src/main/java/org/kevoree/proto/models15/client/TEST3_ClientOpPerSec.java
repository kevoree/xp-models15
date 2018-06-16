package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KDefer;
import org.kevoree.modeling.api.KObject;
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
public class TEST3_ClientOpPerSec {

    private long rootUuid;
    public long originOfTime = 0L;
    private String clientId;

    public KDefer<Concentrator> getDeepestConcentrator(SmartGridModel smartGridModel, SmartGridView smartGridView, long concentratorUUID) {
        KDefer resultTask = smartGridModel.defer();
        smartGridView.lookup(concentratorUUID).then(new Callback<KObject>() {
            @Override
            public void on(KObject kObject) {
                Concentrator c = (Concentrator) kObject;
                if (c.sizeOfConcentrators() > 0) {
                    int concentratorIndex = c.sizeOfConcentrators() / 3;
                    long lowerConcentratorUUID = smartGridModel.manager().entry(c, AccessMode.READ).getRef(MetaConcentrator.REF_CONCENTRATORS.index())[concentratorIndex];
                    getDeepestConcentrator(smartGridModel, smartGridView, lowerConcentratorUUID).then(new Callback<Concentrator>() {
                        @Override
                        public void on(Concentrator localConcentrator) {
                            resultTask.setJob(resultJob -> {
                                resultJob.setResult(localConcentrator);
                            }).ready();
                        }
                    });

                } else {
                    resultTask.setJob(resultJob -> {
                        resultJob.setResult(c);
                    }).ready();
                }
            }
        });
        return resultTask;
    }

    SmartGridModel smartGridModel;

    public void start(Runnable next) {

        smartGridModel = new SmartGridModel();
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

                                int concentratorIndex = smartGridRoot.sizeOfConcentrators() / 3;
                                long concentratorUUID = smartGridModel.manager().entry(smartGridRoot, AccessMode.READ).getRef(MetaSmartGrid.REF_CONCENTRATORS.index())[concentratorIndex];
                                getDeepestConcentrator(smartGridModel, smartGridView, concentratorUUID).then(new Callback<Concentrator>() {
                                    @Override
                                    public void on(Concentrator concentrator) {
                                        int meterIndex = concentrator.sizeOfMeters() / 3;
                                        long meterUUID = smartGridModel.manager().entry(concentrator, AccessMode.READ).getRef(MetaConcentrator.REF_METERS.index())[meterIndex];
                                        smartGridView.lookup(meterUUID).then(new Callback<KObject>() {
                                            @Override
                                            public void on(KObject kObject) {
                                                SmartMeter m = (SmartMeter) kObject;

                                                long endPeriod = System.currentTimeMillis() + 1000;
                                                long start = System.nanoTime();
                                                while (System.currentTimeMillis() < endPeriod) {
                                                    m.setName("set");
                                                    smartGridModel.save();
                                                }
                                                long end = System.nanoTime();
                                                System.out.println("Operations per second:" + ((end - start) / 1000000));
                                                if (next != null) {
                                                    next.run();
                                                }
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
            TEST3_ClientOpPerSec c = new TEST3_ClientOpPerSec();
            //c.start();
        }
    }

}
