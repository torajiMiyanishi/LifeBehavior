package jp.soars.modules.gis_otp.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import org.opentripplanner.api.model.Itinerary;
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
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.error.TrivialPathException;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.DefaultStreetVertexIndexFactory;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;

import jp.soars.modules.gis_otp.otp.TSimpleOpenStreetMapContentHandler;
import jp.soars.utils.csv.TCCsvData;

/**
 * shapeが定義されていない路線を含むGTFSデータに対して，バス停間を車で移動する経路を探索することにより
 * shapeを生成して追加するプログラム．
 */
public class TAddingShapesToGtfs {

    /**
     * グラフを作成する．
     * @param inputDir PBFファイル，GTFSファイルがあるディレクトリ
     * @param pbfFile PBFファイル名
     * @return グラフ
     */
    private static Graph makeGraph(String inputDir, String pbfFile) {
        Graph graph = new Graph();
        HashMap<Class<?>, Object> extra = new HashMap<>();
        BinaryFileBasedOpenStreetMapProviderImpl osmProvider = new BinaryFileBasedOpenStreetMapProviderImpl();
        osmProvider.setPath(new File(inputDir + File.separator + pbfFile));
        TSimpleOpenStreetMapContentHandler handler = new TSimpleOpenStreetMapContentHandler();
        osmProvider.readOSM(handler);
        OpenStreetMapModule osmModule = new OpenStreetMapModule(Collections.singletonList(osmProvider));
        osmModule.buildGraph(graph, extra);
        List<GtfsBundle> gtfsBundles = null;
        try (final Stream<Path> pathStream = Files.list(Paths.get(inputDir))) {
            gtfsBundles = pathStream.map(Path::toFile).filter(file -> file.getName().toLowerCase().endsWith(".zip"))
                    .map(file -> {
                        GtfsBundle gtfsBundle = new GtfsBundle(file);
                        gtfsBundle.setTransfersTxtDefinesStationPaths(true);
                        String id = file.getName().substring(0, file.getName().length() - 4);
                        gtfsBundle.setFeedId(new GtfsFeedId.Builder().id(id).build());
                        return gtfsBundle;
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GtfsModule gtfsModule = new GtfsModule(gtfsBundles);
        gtfsModule.buildGraph(graph, null);
        TransitToStreetNetworkModule linkModule = new TransitToStreetNetworkModule();
        linkModule.buildGraph(graph, null);
        graph.index(new DefaultStreetVertexIndexFactory());
        return graph;
    }

    /**
     * 2点間の経路を補間する
     * @param graph グラフ
     * @param stop_lat1 始点の緯度
     * @param stop_lon1 始点の経度
     * @param stop_lat2 終点の緯度
     * @param stop_lon2 終点の経度
     * @return 座標のリスト
     * @throws IOException
     */
    private static List<double[]> interpolate(Graph graph, double stop_lat1, double stop_lon1, 
                                              double stop_lat2, double stop_lon2) throws IOException {
        RoutingRequest routingRequest = new RoutingRequest();
        routingRequest.from = new GenericLocation(stop_lat1, stop_lon1);
        routingRequest.to = new GenericLocation(stop_lat2, stop_lon2);
        routingRequest.setMode(TraverseMode.CAR); // 移動手段
        routingRequest.setNumItineraries(3);
        routingRequest.setArriveBy(false);
        routingRequest.ignoreRealtimeUpdates = true;
        routingRequest.reverseOptimizing = true;
        routingRequest.onlyTransitTrips = false;
        routingRequest.setRoutingContext(graph);
        Router router = new Router("OTP_GTFS", graph);
        List<GraphPath> paths = new GraphPathFinder(router).getPaths(routingRequest);
        if (paths.isEmpty()) {
            return null;
        }
        TripPlan tripPlan = null;
        try {
            tripPlan = GraphPathToTripPlanConverter.generatePlan(paths, routingRequest);
        } catch (TrivialPathException tpe) {
            return null;
        }
        Itinerary plan = tripPlan.itinerary.get(0);
        if (plan.legs.size() != 1) {
            throw new RuntimeException();
        }
        String latlonHash = plan.legs.get(0).legGeometry.getPoints();
        List<double[]> decodedPath = PolylineDecoder.decode(latlonHash);
        return decodedPath;
    }

    /**
     * Shapesデータを作成する
     * @param graph グラフ
     * @param stopTimes stop_time.txtのCSVデータ
     * @param stops stops.txtのCSVデータ
     * @param shapes shapes.txtのCSVデータ
     * @param shapeIDs シェイプIDの集合
     * @param workDir 作業ディレクトリ
     * @param route_id route_id
     * @param trip_id trip_id
     * @return shape_id
     * @throws IOException
     */
    private static String makeShapeFileOfTripID(Graph graph, TCCsvData stopTimes, TCCsvData stops, 
                                                TCCsvData shapes, Set<String> shapeIDs, String workDir, 
                                                String route_id, String trip_id, 
                                                PrintWriter pwNoRoute) throws IOException {
        // String[] tokens = route_id.split("-");
        // String shape_id = tokens[0] + "-shapes_" + tokens[1] + "_1";
        String shape_id = route_id + "-shapes";
        if (shapeIDs.contains(shape_id)) {
            return shape_id;
        } else {
            shapeIDs.add(shape_id);
        }
        TCCsvData stopTimesWithTripID = stopTimes.extractDataByEqualToFilter("trip_id", trip_id);
        stopTimesWithTripID.sortAsNumberBy("stop_sequence");
        String stop_id = stopTimesWithTripID.getElement(0, "stop_id");
        String stop_sequence = stopTimesWithTripID.getElement(0, "stop_sequence");
        TCCsvData stop_id_info = stops.extractDataByEqualToFilter("stop_id", stop_id);
        String stop_name = stop_id_info.getElement(0, "stop_name");
        String stop_lat = stop_id_info.getElement(0, "stop_lat");
        String stop_lon = stop_id_info.getElement(0, "stop_lon");
        // System.out.println(stop_id_info);
        String prev_stop_name = stop_name;
        String prev_stop_lat = stop_lat;
        String prev_stop_lon = stop_lon;
        // System.out.println(stop_name + "," + prev_stop_lat + "," + prev_stop_lon);
        int shape_pt_sequence = 0;
        shapes.addRow("\"" + shape_id + "\"", "\"" + prev_stop_lat + "\"", "\"" + prev_stop_lon + "\"", 
                      "\"" + shape_pt_sequence + "\"", "\"0.0\"");
        ++shape_pt_sequence;
        for (int i = 1; i < stopTimesWithTripID.getNoOfRows(); ++i) {
            stop_id = stopTimesWithTripID.getElement(i, "stop_id");
            stop_sequence = stopTimesWithTripID.getElement(i, "stop_sequence");
            stop_id_info = stops.extractDataByEqualToFilter("stop_id", stop_id);
            stop_name = stop_id_info.getElement(0, "stop_name");
            stop_lat = stop_id_info.getElement(0, "stop_lat"); 
            stop_lon = stop_id_info.getElement(0, "stop_lon");
            List<double[]> latlonList = interpolate(graph, Double.parseDouble(prev_stop_lat), 
                                                    Double.parseDouble(prev_stop_lon), 
                                                    Double.parseDouble(stop_lat), 
                                                    Double.parseDouble(stop_lon));
            if (latlonList != null) {
                for (double[] latlon: latlonList) {
                    shapes.addRow("\"" + shape_id + "\"", "\"" + latlon[0] + "\"", "\"" + latlon[1] + "\"", 
                                  "\"" + shape_pt_sequence + "\"", "\"0.0\"");
                    ++shape_pt_sequence;
                }
            } else {
                pwNoRoute.println(prev_stop_name + "," + stop_name);
            }
            shapes.addRow("\"" + shape_id + "\"", "\"" + stop_lat + "\"", "\"" + stop_lon + "\"", 
                          "\"" + shape_pt_sequence + "\"", "\"0.0\"");
            ++shape_pt_sequence;
            prev_stop_name = stop_name;
            prev_stop_lat = stop_lat;
            prev_stop_lon = stop_lon;
        }        
        return shape_id;
    }


    /**
     * メイン
     * @param args 経路検索用入力ファイルのディレクトリ PBFファイル 作業ディレクトリ gtfsファイル
     * 経路検索用入力ファイルのディレクトリ：経路検索に用いるPBFファイルと補間前のGTFSファイルが入っているディレクトリ
     * PBFファイル：経路検索に用いるPBFファイル
     * 作業ディレクトリ：補間前のオリジナルのGTFSファイルが格納されているディレクトリ．
     * 　　　　　　　　　ここでオリジナルのGTFSファイルが解凍され，新たに補間後のGTFSファイル
     * 　　　　　　　　　（ファイル名末尾に"-new"がついたもの）が生成される．
     * gtfsファイル：補間前のオリジナルのGTFSファイル名から".zip"を取り除いたもの
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("usage: java TAddingShapesToGtfs inputDir pbfFile workDir gtfsFile");
            System.exit(1);
        }
        String inputDirectory = args[0];
        String pbfFile = args[1];
        String workDirectory = args[2];
        String gtfsFilename = args[3];
        try {
            TCAdvancedUnzip unzip = new TCAdvancedUnzip();
            Files.createDirectory(Paths.get(workDirectory + File.separator + gtfsFilename));
            Files.copy(Paths.get(workDirectory + File.separator + gtfsFilename + ".zip"), Paths.get(workDirectory + File.separator + gtfsFilename + File.separator + gtfsFilename + ".zip"));
            unzip.doIt(workDirectory + File.separator + gtfsFilename, gtfsFilename + ".zip");
            String workDir = workDirectory + File.separator + gtfsFilename + File.separator;
            String newDir = workDirectory + File.separator + gtfsFilename + "-new" + File.separator;
            Files.createDirectory(Paths.get(newDir));
            TCCsvData trips = new TCCsvData(workDir + "trips.txt");
            List<String> tripsKeys = trips.getKeys();
            if (!tripsKeys.contains("shape_id")) {
                tripsKeys.add("shape_id");
                for (int i = 0; i < trips.getNoOfRows(); ++i) {
                    trips.putData(i, "shape_id", "");
                }
            }
            TCCsvData newTrips = trips.clone();
            PrintWriter pw = new PrintWriter(workDir + "no_shape_id.txt");
            pw.println("\"route_id\",\"trip_id\",\"shape_id\"");
            Graph graph = makeGraph(inputDirectory, pbfFile);
            System.out.println("Graph has been generated.");
            TCCsvData stopTimes = new TCCsvData(workDir + "stop_times.txt");
            TCCsvData stops = new TCCsvData(workDir + "stops.txt");
            TCCsvData shapes = new TCCsvData();
            File shapesFile = new File(workDir + "shapes.txt");
            if (shapesFile.exists()) {
                shapes.readFrom(workDir + "shapes.txt");
            } else {
                shapes.setKeys(List.of("shape_id",
                                       "shape_pt_lat",
                                       "shape_pt_lon",
                                       "shape_pt_sequence",
                                       "shape_dist_traveled"));
            }
            PrintWriter pwNoRoute = new PrintWriter(workDirectory + File.separator + gtfsFilename + "_no_route_csv");
            pwNoRoute.println("stop1,stop2");
            HashSet<String> shapeIDs = new HashSet<>();
            for (int i = 0; i < trips.getNoOfRows(); ++i) {
                String shape_id = trips.getElement(i, "shape_id");
                String route_id = trips.getElement(i, "route_id");
                String trip_id = trips.getElement(i, "trip_id");
                if (shape_id.equals("")) {
                    pw.println(route_id + "," + trip_id + "," + shape_id);
                    pw.flush();
                    String newShapeID = makeShapeFileOfTripID(graph, stopTimes, stops, shapes, shapeIDs, 
                                                              workDir, route_id, trip_id, pwNoRoute);
                    if (newShapeID != null) {
                        newTrips.putData(i, "shape_id", newShapeID);
                        System.out.println(newShapeID + " has been made.");                    
                    }
                }
            }
            pw.close();
            pwNoRoute.close();
            shapes.writeTo(newDir + "shapes.txt");
            newTrips.writeTo(newDir + "trips.txt");
            String[] filenamesToBeCopied = {"agency.txt", "fare_attributes.txt", "feed_info.txt", 
                                            "routes.txt", "stops.txt", "calendar.txt", "fare_rules.txt", 
                                            "stop_times.txt"};
            for (String filename: filenamesToBeCopied) {
                File f = new File(workDir + File.separator + filename);
                if (f.exists()) {
                    Files.copy(Paths.get(workDir + File.separator + filename), 
                    Paths.get(newDir + File.separator + filename));
                }
            }
            TCAdvancedZip zip = new TCAdvancedZip();
            zip.doIt(workDirectory + File.separator + gtfsFilename + "-new", ".", workDirectory + File.separator + gtfsFilename + "-new.zip");
            FileUtils.deleteDirectory(new File(workDir));
            FileUtils.deleteDirectory(new File(newDir));    
        } catch (IOException ioex) {
            ioex.printStackTrace();
            System.exit(1);
        }
    }

}
