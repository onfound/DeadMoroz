package game;

import org.jetbrains.annotations.NotNull;
import protocol.data.River;

import java.util.*;

import game.Graph.*;

class SubGraphs {

    private Map<River, RiverState> rivers;
    private List<Integer> mines;
    private Queue<SubGraphInfo> graphs;

    class SubGraphInfo implements Comparable {
        Integer countMines;
        List<Integer> mines;
        Map<River, RiverState> rivers;
        Graph graph01;
        Graph graphWeight;

        SubGraphInfo(Graph graph01, Integer countMines, List<Integer> mines, Map<River, RiverState> rivers, Graph graphWeight) {
            this.graph01 = graph01;
            this.countMines = countMines;
            this.mines = mines;
            this.rivers = rivers;
            this.graphWeight = graphWeight;
        }

        @Override
        public int compareTo(@NotNull Object o) {
            SubGraphInfo oInfo = (SubGraphInfo) o;
            return Integer.compare(graph01.getVertices().size(), oInfo.graph01.getVertices().size());
        }
    }

    SubGraphs(Map<River, RiverState> rivers, List<Integer> mines) {
        this.rivers = rivers;
        this.mines = mines;
        graphs = new PriorityQueue<>();
        buildSubGraphs();
    }

    private void buildSubGraphs() {
        GraphBuilder graphBuilder = new GraphBuilder();
        for (River river : rivers.keySet()) {
            if (rivers.get(river) != RiverState.Enemy1 && rivers.get(river) != RiverState.Enemy2 && rivers.get(river) != RiverState.Enemy3) {
                graphBuilder.addConnection(graphBuilder.addVertex(String.valueOf(river.getSource())), graphBuilder.addVertex(String.valueOf(river.getTarget())), 1);
            }
        }
        Graph graph = graphBuilder.build();
        splitIntoSubGraphs(graph);
    }

    private void splitIntoSubGraphs(Graph graph) {
        Set<Vertex> vertices = graph.getVertices();

        while (!vertices.isEmpty()) {
            Set<Vertex> subGraphVertices = findSubGraphVertices(graph, vertices.iterator().next());
            vertices.removeAll(subGraphVertices);
            int countMineInSubGraph = 0;
            List<Integer> newMines = new ArrayList<>();
            for (Vertex vertex : subGraphVertices) {
                int mine = Integer.parseInt(vertex.getName());
                if (mines.contains(mine)) {
                    countMineInSubGraph++;
                    newMines.add(mine);
                }
            }
            if (countMineInSubGraph == 0) continue;
            GraphBuilder graphBuilder01 = new GraphBuilder();
            GraphBuilder graphBuilderWeight1 = new GraphBuilder();
            Map<River, RiverState> newRivers = new HashMap<>();
            for (River river : rivers.keySet()) {
                Vertex source = graph.get(String.valueOf(river.getSource()));
                Vertex target = graph.get(String.valueOf(river.getTarget()));
                if (source != null && target != null && graph.getNeighbors(source).contains(target)) {
                    if (rivers.get(river) == RiverState.Our)
                        graphBuilder01.addConnection(graphBuilder01.addVertex(source.getName()), graphBuilder01.addVertex(target.getName()), 0);
                    else
                        graphBuilder01.addConnection(graphBuilder01.addVertex(source.getName()), graphBuilder01.addVertex(target.getName()), 1);
                    graphBuilderWeight1.addConnection(graphBuilderWeight1.addVertex(source.getName()), graphBuilderWeight1.addVertex(target.getName()), 1);
                    newRivers.put(river, rivers.get(river));
                }
            }
            graphs.add(new SubGraphInfo(graphBuilder01.build(), countMineInSubGraph, newMines, newRivers, graphBuilderWeight1.build()));
        }
    }

    private Set<Vertex> findSubGraphVertices(Graph graph, Vertex from) {
        Queue<Vertex> queue = new LinkedList<>();
        Set<Vertex> visited = new HashSet<>();
        queue.add(from);
        visited.add(from);
        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            for (Vertex vertex : graph.getNeighbors(current)) {
                if (!visited.contains(vertex)) {
                    queue.add(vertex);
                    visited.add(vertex);
                }
            }
        }
        return visited;
    }

    Queue<SubGraphInfo> getGraphsInfo() {
        return graphs;
    }
}
