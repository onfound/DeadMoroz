package game;

import protocol.Protocol;
import protocol.data.River;

import java.util.*;
import java.util.Map.*;

public class Brain {
    private State state;
    private Protocol protocol;
    private GraphController graphController;
    private Trick trick;

    public Brain(State state, Protocol protocol) {
        this.state = state;
        this.protocol = protocol;
        graphController = new GraphController();
        trick = new Trick();
    }


    public void makeMove() {

        graphController.update(state.getMines(), state.getRivers());
        if (state.getMines().size() < 30 && graphController.getMines().size() > 1 && state.getRivers().size() > 100) {
            trick.update(state.getRivers(), state.getMines());
        }

        //записываем все наши реки в set ourRivers
        HashSet<Integer> ourRiversInt = new HashSet<>();
        for (Entry<River, RiverState> riverEntry : state.getRivers().entrySet()) {
            if (riverEntry.getValue() == RiverState.Our) {
                ourRiversInt.add(riverEntry.getKey().getSource());
                ourRiversInt.add(riverEntry.getKey().getTarget());
            }
        }
        // смотрим если есть чем помешать - мешаем
        if (trick.getDirtyTrick() != null) {
            for (River river : trick.getDirtyTrick()) {
                if (state.getRivers().get(new River(river.getSource(), river.getTarget())) == RiverState.Neutral) {
                    protocol.claimMove(river.getSource(), river.getTarget());
                    System.out.println("┌∩┐(◣_◢)┌∩┐ " + "(" + river.getSource() + ":" + river.getTarget() + ")");
                    return;
                }
            }
        }

        // далее наш алгоритм...
        if (graphController.getWayBetweenMines() != null) {
            for (River river : graphController.getWayBetweenMines()) {
                if (state.getRivers().get(new River(river.getSource(), river.getTarget())) == RiverState.Neutral) {
                    protocol.claimMove(river.getSource(), river.getTarget());
                    System.out.println("Соединяю шахты " + "(" + river.getSource() + ":" + river.getTarget() + ")");
                    return;
                }
            }
        }

        if (graphController.getOptimalPath() != null) {
            for (River river : graphController.getOptimalPath()) {
                if (state.getRivers().get(new River(river.getSource(), river.getTarget())) == RiverState.Neutral) {
                    protocol.claimMove(river.getSource(), river.getTarget());
                    System.out.println("ЗАХВАТЫВАЮ САМУЮ ДАЛЬНЮЮ ТОЧКУ" + "(" + river.getSource() + ":" + river.getTarget() + ")");
                    return;
                }
            }
        }
        graphController.setConnectMines();
        for (Entry<River, RiverState> riverEntry : state.getRivers().entrySet()) {
            Integer source = riverEntry.getKey().getSource();
            Integer target = riverEntry.getKey().getTarget();
            if (riverEntry.getValue() == RiverState.Neutral &&
                    (state.getMines().contains(source) || state.getMines().contains(target))) {
                protocol.claimMove(source, target);
                System.out.println("******Захватил реку у Шахты" + "(" + source + ":" + target + ")");
                return;
            }
        }

        // ищем реки так чтобы концы реки уже принадлежали нашим другим рекам
        for (Entry<River, RiverState> riverEntry : state.getRivers().entrySet()) {
            Integer source = riverEntry.getKey().getSource();
            Integer target = riverEntry.getKey().getTarget();
            if (riverEntry.getValue() == RiverState.Neutral &&
                    (ourRiversInt.contains(source) && ourRiversInt.contains(target))) {
                protocol.claimMove(source, target);
                System.out.println("******Захватил реку у двух наших рек" + "(" + source + ":" + target + ")");
                return;
            }
        }
        // то же самое что и раньше только не обязательно чтобы 2 конца принадлежали
        for (Entry<River, RiverState> riverEntry : state.getRivers().entrySet()) {
            Integer source = riverEntry.getKey().getSource();
            Integer target = riverEntry.getKey().getTarget();
            if (riverEntry.getValue() == RiverState.Neutral &&
                    (ourRiversInt.contains(source) || ourRiversInt.contains(target))) {
                protocol.claimMove(source, target);
                System.out.println("******Захватил реку у нашей реки" + "(" + source + ":" + target + ")");
                return;
            }
        }
        // наугад захватываем
        for (Entry<River, RiverState> riverEntry : state.getRivers().entrySet()) {
            Integer source = riverEntry.getKey().getSource();
            Integer target = riverEntry.getKey().getTarget();
            if (riverEntry.getValue() == RiverState.Neutral) {
                protocol.claimMove(source, target);
                System.out.println("******захватил рандомную пустую реку" + "(" + source + ":" + target + ")");
                return;
            }
        }
        protocol.passMove();
    }
}