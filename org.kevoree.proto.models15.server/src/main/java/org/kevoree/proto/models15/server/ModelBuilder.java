package org.kevoree.proto.models15.server;

import org.kevoree.proto.models15.SmartGridView;
import org.kevoree.proto.models15.impl.SmartGridViewImpl;
import org.kevoree.test.models15.Concentrator;
import org.kevoree.test.models15.SmartGrid;

import java.util.HashMap;

/**
 * Created by gnain on 08/05/15.
 */
public class ModelBuilder {

    /*
    * Stats
    * 0 : TOTAL ELEMENTS
    * 1 : Concentrators
    * 2 : Meters
    *
    * */

    public static SmartGrid buildModel(SmartGridView factory, int concentratorLevels, int subStations, int meters, HashMap<Integer, Integer> stats) {

        SmartGrid grid = factory.createSmartGrid();
        stats.put(0,stats.getOrDefault(0,0)+1);
        for(int i = 0; i < subStations; i++) {
            Concentrator c = factory.createConcentrator();
            grid.addConcentrators(c);
            stats.put(0, stats.getOrDefault(0, 0) + 1);
            stats.put(1,stats.getOrDefault(1,0)+1);
            if(concentratorLevels > 1) {
                addSubStrations(factory, c, 1, concentratorLevels, subStations, meters, stats);
            } else {
                addMeters(factory, c, meters, stats);
            }
        }
        return grid;
    }


    private static void addSubStrations(SmartGridView factory, Concentrator parent, int currentLevel, int concentratorLevels, int subStations, int meters, HashMap<Integer, Integer> stats) {
        for(int i = 0; i < subStations; i++) {
            Concentrator c = factory.createConcentrator();
            parent.addConcentrators(c);
            stats.put(0,stats.getOrDefault(0,0)+1);
            stats.put(1, stats.getOrDefault(1, 0) + 1);
            if(concentratorLevels > currentLevel) {
                addSubStrations(factory, c, currentLevel+1, concentratorLevels, subStations, meters, stats);
            } else {
                addMeters(factory, c, meters, stats);
            }
        }
    }


    private static void addMeters(SmartGridView factory, Concentrator parent, int meters, HashMap<Integer, Integer> stats) {
        for(int i = 0; i < meters; i++) {
            parent.addMeters(factory.createSmartMeter());
            stats.put(0,stats.getOrDefault(0,0)+1);
            stats.put(2,stats.getOrDefault(2,0)+1);
        }
    }

}
