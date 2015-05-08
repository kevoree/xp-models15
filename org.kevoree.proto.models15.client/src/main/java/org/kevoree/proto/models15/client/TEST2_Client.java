package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KDefer;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.api.data.manager.AccessMode;
import org.kevoree.modeling.databases.redis.RedisContentDeliveryDriver;
import org.kevoree.proto.models15.SmartGridModel;
import org.kevoree.proto.models15.SmartGridUniverse;
import org.kevoree.proto.models15.SmartGridView;
import org.kevoree.test.models15.Concentrator;
import org.kevoree.test.models15.SmartGrid;
import org.kevoree.test.models15.meta.MetaConcentrator;
import org.kevoree.test.models15.meta.MetaSmartGrid;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST2_Client {

    private long rootUuid;
    public long originOfTime = 0L;
    private String clientId;

    public KDefer<Concentrator> getDeepestConcentrator(SmartGridView smartGridView, long concentratorUUID) {
        KDefer resultTask = smartGridView.universe().model().defer();
        smartGridView.lookup(concentratorUUID).then(new Callback<KObject>() {
            @Override
            public void on(KObject kObject) {
                Concentrator c = (Concentrator) kObject;
                if (c.sizeOfConcentrators() > 0) {
                    int concentratorIndex = c.sizeOfConcentrators() / 3;
                    long lowerConcentratorUUID = c.universe().model().manager().entry(c, AccessMode.READ).getRef(MetaConcentrator.REF_CONCENTRATORS.index())[concentratorIndex];
                    getDeepestConcentrator(smartGridView, lowerConcentratorUUID).then(new Callback<Concentrator>() {
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

    private SmartGridModel smartGridModel;

    public void start(Runnable next) {
        long nodeId = System.currentTimeMillis() + (0 + (int) (Math.random() * 100000));
//        System.out.println("nodeId: " + nodeId);

        smartGridModel = new SmartGridModel();
        //smartGridModel.setContentDeliveryDriver(new WebSocketClient("ws://localhost:8080"));

        RedisContentDeliveryDriver driver = new RedisContentDeliveryDriver("192.168.1.124", 6379);
        smartGridModel.setContentDeliveryDriver(driver);

        smartGridModel.connect().then(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    SmartGridUniverse baseUniverse = smartGridModel.universe(0);
                    SmartGridView smartGridView = baseUniverse.time(originOfTime);
                    long start = System.nanoTime();
                    smartGridView.getRoot().then(new Callback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject != null) {
                                SmartGrid smartGridRoot = (SmartGrid) kObject;

                                // listen to changes from the server -> concentrator c
                                KDefer getter = smartGridRoot.traversal().traverse(MetaSmartGrid.REF_CONCENTRATORS)
                                        .withAttribute(MetaConcentrator.ATT_NAME, "c")
                                        .done();

                                KDefer defer = smartGridView.universe().model().defer();
                                defer.wait(getter);
                                defer.setJob(kCurrentDefer -> {
                                    KObject[] arr = (KObject[]) kCurrentDefer.resultByDefer(getter);
                                    Concentrator c = (Concentrator) arr[0];
                                    //System.out.println("client found: " + c.getName());

                                    c.listen(0, (kObject1, metas) -> {
                                        System.out.println("/" + (System.currentTimeMillis() - kObject1.now()) + "/");
                                    });
                                });
                                defer.ready();


//                                int concentratorIndex = smartGridRoot.sizeOfConcentrators() / 3;
//                                long concentratorUUID = smartGridRoot.universe().model().manager().entry(smartGridRoot, AccessMode.READ).getRef(MetaSmartGrid.REF_CONCENTRATORS.index())[concentratorIndex];
//                                getDeepestConcentrator(smartGridView, concentratorUUID).then(new Callback<Concentrator>() {
//                                    @Override
//                                    public void on(Concentrator concentrator) {
//                                        //System.out.println("Concentrator:" + concentrator);
//                                        concentrator.getMeters().then(new Callback<SmartMeter[]>() {
//                                            public void on(SmartMeter[] smartMeters) {
//                                                long end = System.nanoTime();
//                                                System.out.println("ReadTime:" + ((end - start) / 1000000));
//                                                if (next != null) {
//                                                    next.run();
//                                                }
//                                            }
//                                        });
//                                    }
//                                });

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
            TEST2_Client c = new TEST2_Client();
            c.start(null);
        }
    }

}
