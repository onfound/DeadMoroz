package game;

import protocol.data.River;

import java.util.*;

class Trick {
    private Map<River, RiverState> rivers;
    private List<Integer> mines;
    private List<River> dirtyTrick;

    void update(Map<River, RiverState> rivers, List<Integer> mines) {
        this.rivers = rivers;
        this.mines = mines;
        findDirtyTrick();
    }

    private void findDirtyTrick() {
        dirtyTrick = new ArrayList<>();
        GraphBuilder graphBuilder1 = new GraphBuilder();
        GraphBuilder graphBuilder2 = new GraphBuilder();
        GraphBuilder graphBuilder3 = new GraphBuilder();
        Set<Integer> set1 = new HashSet<>();
        Set<Integer> set2 = new HashSet<>();
        Set<Integer> set3 = new HashSet<>();
        for (River river : rivers.keySet()) {
            String vertexSource = String.valueOf(river.getSource());
            String vertexTarget = String.valueOf(river.getTarget());
            switch (rivers.get(river)) {
                case Neutral:
                    graphBuilder1.addConnection(graphBuilder1.addVertex(vertexSource), graphBuilder1.addVertex(vertexTarget), 1);
                    graphBuilder2.addConnection(graphBuilder2.addVertex(vertexSource), graphBuilder2.addVertex(vertexTarget), 1);
                    graphBuilder3.addConnection(graphBuilder3.addVertex(vertexSource), graphBuilder3.addVertex(vertexTarget), 1);
                    break;
                case Enemy1:
                    graphBuilder1.addConnection(graphBuilder1.addVertex(vertexSource), graphBuilder1.addVertex(vertexTarget), 0);
                    set1.add(river.getSource());
                    set1.add(river.getTarget());
                    break;
                case Enemy2:
                    graphBuilder2.addConnection(graphBuilder2.addVertex(vertexSource), graphBuilder2.addVertex(vertexTarget), 0);
                    set2.add(river.getSource());
                    set2.add(river.getTarget());
                    break;
                case Enemy3:
                    graphBuilder3.addConnection(graphBuilder3.addVertex(vertexSource), graphBuilder3.addVertex(vertexTarget), 0);
                    set3.add(river.getSource());
                    set3.add(river.getTarget());
                    break;
            }
        }
        Graph graph1 = graphBuilder1.build();
        Graph graph2 = graphBuilder2.build();
        Graph graph3 = graphBuilder3.build();
        if (rivers.size() < 9999) {
            for (River river : rivers.keySet()) {
                if (rivers.get(river) == RiverState.Neutral) {
                    if (set1.contains(river.getSource()) && set1.contains(river.getTarget()))
                        checkSimpleRiver(graph1, river);
                    if (set2.contains(river.getSource()) && set2.contains(river.getTarget()))
                        checkSimpleRiver(graph2, river);
                    if (set3.contains(river.getSource()) && set3.contains(river.getTarget()))
                        checkSimpleRiver(graph3, river);
                }
            }
        }

        findPath(graph1);
        findPath(graph2);
        findPath(graph3);
    }

    private void checkSimpleRiver(Graph graph, River river) {
        Graph.Vertex vertexSource = graph.get(String.valueOf(river.getSource()));
        Graph.Vertex vertexTarget = graph.get(String.valueOf(river.getTarget()));
        if (vertexSource != null && vertexTarget != null) {
            int source = graph.getNeighbors(vertexSource).size();
            int target = graph.getNeighbors(vertexTarget).size();
            if (source < 4 && target == 2 || source == 2 && target < 4) {
                dirtyTrick.add(river);
            }
        }
    }

    private void findPath(Graph graph) {
        List<List<Graph.Vertex>> paths = new ArrayList<>();
        for (int i = 0; i < mines.size() - 1; i++) {
            for (int j = i + 1; j < mines.size(); j++) {
                Graph.Vertex source = graph.get(String.valueOf(mines.get(i)));
                Graph.Vertex target = graph.get(String.valueOf(mines.get(j)));
                if (source != null && target != null) {
                    paths.add(DejkstraKt.unrollPath(DejkstraKt.shortestPath(graph, source), target));
                }
            }
        }
        for (List<Graph.Vertex> path : paths) {
            int countNeutralRiver = 0;
            int countEnemyRiver = 0;
            River result = null;
            for (int i = 0; i < path.size() - 1; i++) {
                int source = Integer.valueOf(path.get(i).getName());
                int target = Integer.valueOf(path.get(i + 1).getName());
                River river = new River(source, target);
                if (rivers.get(river) == RiverState.Neutral) {
                    countNeutralRiver++;
                    result = river;
                } else countEnemyRiver++;
            }
            if (countNeutralRiver == 1 && countEnemyRiver > 1) dirtyTrick.add(result);
        }
    }


    List<River> getDirtyTrick() {
        return dirtyTrick;
    }
}
