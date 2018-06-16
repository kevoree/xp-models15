package org.kevoree.proto.models15.server;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.KDefer;
import org.kevoree.modeling.api.KObject;
import org.kevoree.modeling.databases.redis.RedisContentDeliveryDriver;
import org.kevoree.proto.models15.SmartGridModel;
import org.kevoree.proto.models15.SmartGridUniverse;
import org.kevoree.proto.models15.SmartGridView;
import org.kevoree.test.models15.Concentrator;
import org.kevoree.test.models15.SmartGrid;
import org.kevoree.test.models15.meta.MetaConcentrator;
import org.kevoree.test.models15.meta.MetaSmartGrid;

import java.util.HashMap;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST2_Server {

    public static long originOfTime = 0L;

    private SmartGridModel smartGridModel;
    private SmartGridView smartGridView;
    private SmartGrid grid;

    public void start(int concentratorLayers, int subStations, int meters, Runnable next) {

        smartGridModel = new SmartGridModel();

        RedisContentDeliveryDriver driver = new RedisContentDeliveryDriver("192.168.1.124", 6379);
        smartGridModel.setContentDeliveryDriver(driver);

//        smartGridModel.setContentDeliveryDriver(new WebSocketWrapper(smartGridModel.manager().cdn(), 8080));

        smartGridModel.connect().then(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    SmartGridUniverse baseUniverse = smartGridModel.universe(0);
                    smartGridView = baseUniverse.time(originOfTime);

                    HashMap<Integer, Integer> stats = new HashMap<Integer, Integer>();
                    grid = ModelBuilder.buildModel(smartGridView, concentratorLayers, subStations, meters, stats);

                    Concentrator c = smartGridView.createConcentrator();
                    c.setName("c");
                    grid.addConcentrators(c);

                    System.out.println("Concentrators: " + stats.get(1) + " Meters: " + stats.get(2) + " ALL:" + stats.get(0));
                    smartGridView.setRoot(grid).then(new Callback<Throwable>() {
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
                                        System.out.println("Base model committed.");
                                        if (next != null) {
                                            next.run();
                                        }
                                    }
                                }
                            });
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
        TEST2_Server server = new TEST2_Server();

        server.start(3, 5, 10, null);

        // wait
        System.out.println("waiting...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // load smart meter 1_1
        KDefer defer = server.grid.traversal().traverse(MetaSmartGrid.REF_CONCENTRATORS)
                .withAttribute(MetaConcentrator.ATT_NAME, "c")
                .done();

        KDefer result = server.smartGridModel.defer();
        result.wait(defer);

        result.setJob(kCurrentDefer -> {
            try {
                KObject[] resultArr = (KObject[]) kCurrentDefer.resultByDefer(defer);
                Concentrator c = (Concentrator) resultArr[0];

                while (true) {
                    // changing value
                    Thread.sleep(5000);
                    long time = System.currentTimeMillis();
                    System.out.println("change value..." + time);

                    c.jump(time).then(kObject -> {
                        ((Concentrator) kObject).setConsumption(5);
                        server.smartGridModel.save();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        result.ready();

    }

}
