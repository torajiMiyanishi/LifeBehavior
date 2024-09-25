package jp.soars.modules.gis_otp.otp;

import org.opentripplanner.openstreetmap.model.OSMNode;
import org.opentripplanner.openstreetmap.model.OSMRelation;
import org.opentripplanner.openstreetmap.model.OSMWay;
import org.opentripplanner.openstreetmap.services.OpenStreetMapContentHandler;

/**
 * OpenTripPlannerがデータを読み込む際に利用するコールバック
 */
public class TSimpleOpenStreetMapContentHandler implements OpenStreetMapContentHandler {
    @Override
    public void addNode(OSMNode node) {
        // System.out.println("Added node: " + node.getId() + " Lat: " + node.lat + " Lon: " + node.lon);
    }

    @Override
    public void addWay(OSMWay way) {
        // System.out.println("Added way: " + way.getId() + " with nodes: " + way.getNodeRefs().size());
    }

    @Override
    public void addRelation(OSMRelation relation) {
        // System.out.println("Added relation: " + relation.getId() + " with members: " + relation.getMembers().size());
    }

    @Override
    public void doneFirstPhaseRelations() {
        // System.out.println("Completed first phase of relations.");
    }

    @Override
    public void doneSecondPhaseWays() {
        // System.out.println("Completed second phase of ways.");
    }

    @Override
    public void doneThirdPhaseNodes() {
        // System.out.println("Completed third phase of nodes.");
    }
}
