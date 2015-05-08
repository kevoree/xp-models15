package org.kevoree.proto.models15.server;

import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.databases.websocket.WebSocketWrapper;
import org.kevoree.proto.models15.*;
import org.kevoree.test.models15.SmartGrid;

import java.util.HashMap;

/**
 * Created by gnain on 07/05/15.
 */
public class TEST2_Server {

    public static long originOfTime = 0L;

    private SmartGridModel smartGridModel;
    private SmartGridView smartGridView;


    public void start(int concentratorLayers, int subStations, int meters, Runnable next) {

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

                    HashMap<Integer, Integer> stats = new HashMap<Integer, Integer>();
                    SmartGrid grid = ModelBuilder.buildModel(smartGridView, concentratorLayers, subStations, meters, stats);

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
        new TEST2_Server().start(3, 5, 10, null);
    }

}
