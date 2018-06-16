package org.kevoree.proto.models15.client;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KDefer;
import org.kevoree.modeling.api.KDeferBlock;
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
public class TEST1_Client {

    private long rootUuid;
    public long originOfTime = 0L;
    private String clientId;



/*
    public KDefer<Concentrator> searchConcentratorOf(Entity e) {
        KDefer resultTask = grid.universe().model().defer();

        if (!(e instanceof SmartMeter)) {
            resultTask.setJob(job -> {
                job.addDeferResult(null);
            }).ready();
            return resultTask;
        }

        SmartMeter smartMeter = (SmartMeter) e;
        if (!smartMeter.getCommunicationActive()) {
            resultTask.setJob(job -> {
                job.addDeferResult(null);
            }).ready();
            return resultTask;
        }

        KDefer<Set<Entity>> reachableEntitiesTask = searchReachableEntities(smartMeter);

        KDefer task = grid.universe().model().defer();
        task.wait(reachableEntitiesTask);
        task.setJob(job -> {
            Set<Entity> reachableEntities = (Set<Entity>) job.resultByDefer(reachableEntitiesTask);

            Entity elem = smartMeter;
            if (elem != null && !(elem instanceof Concentrator)) {
                registeredByRecursion(elem, reachableEntities).setName("registeredByRecursion")
                        .next().setJob(j -> {

                    Entity entity = (Entity) j.resultByName("registeredByRecursion");
                    resultTask.setJob(resultJob -> {
                        resultJob.clearResults();
                        resultJob.addDeferResult(entity);
                    });

                }).ready();

            } else {
                resultTask.setJob(resultJob -> {
                    resultJob.clearResults();
                    resultJob.addDeferResult(elem);
                });
            }
        }).ready();

        resultTask.ready();
        return resultTask;
    }

    */


    public KDefer<Concentrator> getDeepestConcentrator(SmartGridView smartGridView, long concentratorUUID) {
        KDefer resultTask = smartGridModel.defer();
        smartGridView.lookup(concentratorUUID).then(new Callback<KObject>() {
            @Override
            public void on(KObject kObject) {
                Concentrator c = (Concentrator) kObject;
                if (c.sizeOfConcentrators() > 0) {
                    int concentratorIndex = c.sizeOfConcentrators() / 3;
                    long lowerConcentratorUUID = smartGridModel.manager().entry(c, AccessMode.READ).getRef(MetaConcentrator.REF_CONCENTRATORS.index())[concentratorIndex];
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
                    long start = System.nanoTime();
                    smartGridView.getRoot().then(new Callback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject != null) {
                                SmartGrid smartGridRoot = (SmartGrid) kObject;

                                int concentratorIndex = smartGridRoot.sizeOfConcentrators() / 3;
                                long concentratorUUID = smartGridModel.manager().entry(smartGridRoot, AccessMode.READ).getRef(MetaSmartGrid.REF_CONCENTRATORS.index())[concentratorIndex];
                                getDeepestConcentrator(smartGridView, concentratorUUID).then(new Callback<Concentrator>() {
                                    @Override
                                    public void on(Concentrator concentrator) {
                                        //System.out.println("Concentrator:" + concentrator);
                                        concentrator.getMeters().then(new Callback<SmartMeter[]>() {
                                            public void on(SmartMeter[] smartMeters) {
                                                long end = System.nanoTime();
                                                System.out.println("ReadTime:" + ((end - start) / 1000000));
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
            TEST1_Client c = new TEST1_Client();
            c.start(null);
        }
    }

}
