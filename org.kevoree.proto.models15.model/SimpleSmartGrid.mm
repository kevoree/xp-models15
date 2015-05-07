//KMF_VERSION=4.18.4-SNAPSHOT
//VERSION=1.0.0-SNAPSHOT
class org.kevoree.test.models15.SmartGrid {
    @contained meters : org.kevoree.test.models15.SmartMeter[0,*]
    @contained concentrators : org.kevoree.test.models15.Concentrator[0,*]
}

class org.kevoree.test.models15.SmartMeter {
    name : String
    consumption : Int
}

class org.kevoree.test.models15.Concentrator {
    name : String
    meters : org.kevoree.test.models15.SmartMeter[0,*]
}
