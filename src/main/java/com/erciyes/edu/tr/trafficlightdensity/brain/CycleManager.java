package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;
import java.util.List;
import java.util.Map;

public class CycleManager {
    private int currentIndex;
    private final List<Direction> directionOrder;
    private Direction activeDirection;
    private final TrafficController trafficController;

    public CycleManager(TrafficController trafficController) {
        this.trafficController = trafficController;
        this.directionOrder = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    }

    public List<Direction> getDirectionOrder() { // Added getter
        return directionOrder;
    }

    public void startCycle() {
        currentIndex = 0;
        if (!directionOrder.isEmpty()) {
            activeDirection = directionOrder.get(currentIndex);
        } else {
            activeDirection = null;
            System.err.println("CycleManager Error: Direction order is empty!");
        }
        System.out.println("Cycle started. First direction: " +
                (activeDirection != null ? activeDirection: "NONE") +
                ", Duration: " + getCurrentDuration() + "s");
    }

    public void switchToNextDirection() {
        if (directionOrder.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + 1) % directionOrder.size();
        activeDirection = directionOrder.get(currentIndex);
    }

    public Direction getCurrentDirection() {
        return activeDirection;
    }

    public int getCurrentDuration() {
        if (activeDirection == null) {
            return 0;
        }
        return trafficController.getGreenDuration(activeDirection);
    }

    public boolean isEndOfCycle() {
        if (activeDirection == null || directionOrder == null || directionOrder.isEmpty()) {
            return false;
        }
        return activeDirection == directionOrder.get(directionOrder.size() - 1);
    }
}