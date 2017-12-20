package game;

import game.Graph.Vertex;

import java.util.Map;

import protocol.data.River;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class GraphController {
    private List<Integer> mines;
    private Map<River, RiverState> rivers;
    private SubGraphs.SubGraphInfo graphInfo;

    private List<River> wayBetweenMinesRivers;

    private List<River> optimalPath;
    private boolean connectMines;

    void update(List<Integer> mines, Map<River, RiverState> rivers) {
        graphInfo = new SubGraphs(rivers, mines).getGraphsInfo().poll();
        this.mines = graphInfo.mines;
        this.rivers = graphInfo.rivers;
        if (!connectMines && graphInfo.countMines > 1) shortestPathsMines();
        else optimalPath();
    }

    private void shortestPathsMines() {
        wayBetweenMinesRivers = new ArrayList<>();
        List<Vertex> wayBetweenMinesVertex = new ArrayList<>();
        List<List<Vertex>> pathsVertex = new CopyOnWriteArrayList<>();
        for (int i = 0; i < mines.size() - 1; i++) {
            Vertex mineSource = graphInfo.graph01.get(String.valueOf(mines.get(i)));
            Vertex mineTarget = graphInfo.graph01.get(String.valueOf(mines.get(i + 1)));
            if (mineSource != null && mineTarget != null)
                pathsVertex.add(DejkstraKt.unrollPath(DejkstraKt.shortestPath(graphInfo.graph01, mineSource), mineTarget));
        }

        pathsVertex.sort(Comparator.comparing(List::size));

        for (List<Vertex> path : pathsVertex) {
            if (path.size() > 2) {
                Vertex mineSource = path.get(0);
                Vertex mineTarget = path.get(1);
                if (mineSource != null && mineTarget != null) {
                    wayBetweenMinesVertex.add(mineSource);
                    wayBetweenMinesVertex.add(mineTarget);
                    path.remove(mineSource);
                }
            }
            Vertex mineSource1 = graphInfo.graph01.get(String.valueOf(path.get(path.size() - 1)));
            Vertex mineTarget1 = graphInfo.graph01.get(String.valueOf(path.get(path.size() - 2)));
            if (mineSource1 != null && mineTarget1 != null) {
                wayBetweenMinesVertex.add(mineSource1);
                wayBetweenMinesVertex.add(mineTarget1);
                path.remove(mineSource1);
            }
            if (path.size() == 0) pathsVertex.remove(path);
        }
        for (List<Vertex> path : pathsVertex) {
            wayBetweenMinesVertex.addAll(path);
        }
        for (int i = 0; i < wayBetweenMinesVertex.size() - 1; i++) {
            River river = new River(Integer.parseInt(wayBetweenMinesVertex.get(i).getName()), Integer.parseInt(wayBetweenMinesVertex.get(i + 1).getName()));
            if (rivers.get(river) != null) wayBetweenMinesRivers.add(river);
        }

        int count = 0;
        for (River river : wayBetweenMinesRivers) {
            if (rivers.get(river) == RiverState.Our) count++;
        }
        if (wayBetweenMinesRivers.size() <= count) connectMines = true;
    }


    private void optimalPath() {
        List<Map<Vertex, VertexInfo>> minesDejkstraWeight = new ArrayList<>();
        Set<Vertex> connectedVertex = new HashSet<>();
        for (River river : rivers.keySet()) {
            if (rivers.get(river) == RiverState.Our) {
                Vertex source = graphInfo.graphWeight.get(String.valueOf(river.getSource()));
                Vertex target = graphInfo.graphWeight.get(String.valueOf(river.getTarget()));
                if (source != null && target != null) {
                    connectedVertex.add(source);
                    connectedVertex.add(target);
                }
            }
        }
        for (Integer mine : mines) {
            Vertex mineVertexWeight = graphInfo.graphWeight.get(String.valueOf(mine));
            if (mineVertexWeight != null)
                minesDejkstraWeight.add(DejkstraKt.shortestPath(graphInfo.graphWeight, mineVertexWeight));
        }
        Vertex maxPriorityVertex = null;
        int maxPointsPriorityDistance = 0;

        for (Vertex vertex : graphInfo.graphWeight.getVertices()) {
            if (!connectedVertex.contains(vertex)) {
                int maxPriorityDistance = 0;
                for (Map<Vertex, VertexInfo> aMinesDejkstraWeight : minesDejkstraWeight) {
                    int distance = aMinesDejkstraWeight.get(vertex).getDistance();
                    maxPriorityDistance += distance * distance;
                }
                if (maxPriorityDistance > maxPointsPriorityDistance) {
                    maxPointsPriorityDistance = maxPriorityDistance;
                    maxPriorityVertex = vertex;
                }
            }
        }
        if (maxPriorityVertex != null) {
            Map<Vertex, VertexInfo> resultMap = DejkstraKt.shortestPath(graphInfo.graph01, maxPriorityVertex);
            int minDistance = Integer.MAX_VALUE;
            Vertex minDistanceFromVertex = null;
            for (Vertex vertex : connectedVertex) {
                VertexInfo vertexInfo = resultMap.get(vertex);
                if (vertexInfo != null && vertexInfo.getDistance() < minDistance) {
                    minDistanceFromVertex = vertex;
                    minDistance = resultMap.get(vertex).getDistance();
                }
            }
            List<Vertex> result = new ArrayList<>();
            List<River> resultRiverPath = new ArrayList<>();
            if (minDistanceFromVertex != null) result = DejkstraKt.unrollPath(resultMap, minDistanceFromVertex);

            for (int i = result.size() - 1; i > 0; i--) {
                resultRiverPath.add(new River(Integer.valueOf(result.get(i).getName()), Integer.valueOf(result.get(i - 1).getName())));
            }
            optimalPath = resultRiverPath;
        }
    }

    List<River> getWayBetweenMines() {
        return wayBetweenMinesRivers;
    }

    List<River> getOptimalPath() {
        return optimalPath;
    }


    public List<Integer> getMines() {
        return mines;
    }

    void setConnectMines() {
        this.connectMines = true;
    }
}