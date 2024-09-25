package jp.soars.modules.gis_otp.otp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.resource.GraphPathToTripPlanConverter;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.graph_builder.linking.TransitToStreetNetworkModule;
import org.opentripplanner.graph_builder.model.GtfsBundle;
import org.opentripplanner.graph_builder.module.GtfsFeedId;
import org.opentripplanner.graph_builder.module.GtfsModule;
import org.opentripplanner.graph_builder.module.osm.OpenStreetMapModule;
import org.opentripplanner.openstreetmap.impl.BinaryFileBasedOpenStreetMapProviderImpl;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.error.TrivialPathException;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.DefaultStreetVertexIndexFactory;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;

import jp.soars.modules.gis_otp.otp.TOtpResult.EOtpStatus;

/**
 * OpenTripPlanner Ver.1.5を用いて経路探索を行うクラス
 */
public class TOtpRouter {

    /** グラフオブジェクト */
    private Graph fGraph;

    /** GraphPathFinderのgetPathsメソッドが呼び出された回数 */
    private int fNoOfTimesCalled;

    /** pbfファイルへのパス */
    private String fPathToPbf;

    /** gtfsファイルがあるディレクトリへのパス */
    private String fDirToGtfs;

    /** GraphPathFinderのgetPathsメソッドの呼び出し回数の最大値 */
    public static final int MAX_CALL = 5000;

    /**
     * コンストラクタ
     * @param pathToPbf pbfファイルのパス
     * @param dirToGtfs gtfsファイルがあるディレクトリのパス
     * @throws IOException 
     */
    public TOtpRouter(String pathToPbf, String dirToGtfs) throws IOException {
        fPathToPbf = pathToPbf;
        fDirToGtfs = dirToGtfs;
        initializeGraph();
    }

    /**
     * グラフオブジェクトを初期化する。
     * @throws IOException
     */
    private void initializeGraph() throws IOException {
        fGraph = new Graph();
        // OSMモジュールの設定
        HashMap<Class<?>, Object> extra = new HashMap<>();
        BinaryFileBasedOpenStreetMapProviderImpl osmProvider = new BinaryFileBasedOpenStreetMapProviderImpl();
        osmProvider.setPath(new File(fPathToPbf));
        TSimpleOpenStreetMapContentHandler handler = new TSimpleOpenStreetMapContentHandler();
        osmProvider.readOSM(handler);
        OpenStreetMapModule osmModule = new OpenStreetMapModule(Collections.singletonList(osmProvider));
        osmModule.buildGraph(fGraph, extra);
        // GTFSモジュールの設定
        List<GtfsBundle> gtfsBundles = null;
        final Stream<Path> pathStream = Files.list(Paths.get(fDirToGtfs));
        gtfsBundles = pathStream.map(Path::toFile)
                                .filter(file -> file.getName().toLowerCase().endsWith(".zip"))
                                .map(file -> {
                                    GtfsBundle gtfsBundle = new GtfsBundle(file);
                                    gtfsBundle.setTransfersTxtDefinesStationPaths(true);
                                    String id = file.getName().substring(0, file.getName().length() - 4);
                                    gtfsBundle.setFeedId(new GtfsFeedId.Builder().id(id).build());
                                    return gtfsBundle;
                                }).collect(Collectors.toList());
        pathStream.close();
        GtfsModule gtfsModule = new GtfsModule(gtfsBundles);
        gtfsModule.buildGraph(fGraph, null);
        TransitToStreetNetworkModule linkModule = new TransitToStreetNetworkModule();
        linkModule.buildGraph(fGraph, null);
        fGraph.index(new DefaultStreetVertexIndexFactory());
        fNoOfTimesCalled = 0;
    }

    /**
     * 経路検索を実行する。
     * @param noOfItineraries 検索する旅程の最大数
     * @param traverseModeSet 移動手段の集合(MODE_BICYCLE, MODE_WALK, MODE_CAR, 
     *                                      MODE_BUS, MODE_TRAM, MODE_SUBWAY, 
     *                                      MODE_RAIL, MODE_FERRY, MODE_CABLE_CAR, 
     *                                      MODE_GONDOLA, MODE_FUNICULAR, 
     *                                      MODE_AIRPLANE, MODE_TRANSIT, MODE_ALL)
     * @param arriveBy true:到着時刻で検索する、false:出発時刻で検索する
     * @param year 年
     * @param month 月
     * @param date 日
     * @param hour 時
     * @param minute 分
     * @param lat1 出発地点の緯度
     * @param lon1 出発地点の経度
     * @param lat2 到着地点の緯度
     * @param lon2 到着地点の経度
     * @return 経路検索結果
     * @throws IOException 
     */
    public TOtpResult doIt(int noOfItineraries, TraverseModeSet traverseModeSet, boolean arriveBy, 
                           int year, int month, int date, int hour, int minute, 
                           double lat1, double lon1, double lat2, double lon2) {
        if (fNoOfTimesCalled > MAX_CALL) {
            try {
                initializeGraph();
            } catch (IOException ioex) {
                throw new RuntimeException("Failed to initialize the graph object!!");
            }
        } else {
            ++fNoOfTimesCalled;
        }
        RoutingRequest routingRequest = new RoutingRequest();
        LocalDateTime ldt = LocalDateTime.of(year, month, date, hour, minute);
        routingRequest.dateTime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()).getTime() / 1000;
        routingRequest.from = new GenericLocation(lat1, lon1);
        routingRequest.to = new GenericLocation(lat2, lon2);
        routingRequest.setNumItineraries(noOfItineraries); //旅程の検索数
        routingRequest.setArriveBy(arriveBy);
        routingRequest.ignoreRealtimeUpdates = true;
        routingRequest.reverseOptimizing = true;
        routingRequest.onlyTransitTrips = false;
        try {
            routingRequest.setRoutingContext(fGraph);
        } catch(TrivialPathException tpe) {
            return new TOtpResult(EOtpStatus.TRIVIAL_PATH, null, null);
        }
        routingRequest.setModes(traverseModeSet); // 移動手段
        Router router = new Router("OTP_GTFS", fGraph);
        GraphPathFinder gpf = new GraphPathFinder(router);
        List<GraphPath> paths = gpf.getPaths(routingRequest);
        if (paths.isEmpty()) {
            return new TOtpResult(EOtpStatus.NO_ROUTE, null, null);
        }
        try {
            TripPlan tripPlan = GraphPathToTripPlanConverter.generatePlan(paths, routingRequest);
            return new TOtpResult(EOtpStatus.SUCCESS, paths, tripPlan);
        } catch(TrivialPathException tpe) {
            return new TOtpResult(EOtpStatus.TRIVIAL_PATH, null, null);
        }
    }
}
