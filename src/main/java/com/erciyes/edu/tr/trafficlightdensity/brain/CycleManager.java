package com.erciyes.edu.tr.trafficlightdensity.brain;

import com.erciyes.edu.tr.trafficlightdensity.road_objects.Direction;

import java.util.List;

//Bir döngüyü (cycle) sırasıyla başlatır, faz geçişlerini yönetir.
public class CycleManager {
    int currentIndex;
    List<Direction> directionOrder;
    Direction activeDirection;
    TrafficController trafficController = new TrafficController();

    public CycleManager(TrafficController trafficController) {
    }

    public void startCycle()
    {
        directionOrder = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        currentIndex = 0;
        activeDirection = directionOrder.get(currentIndex);

        //Log ekleyelim görelim:

        System.out.println("Cycle started. First direction: " + activeDirection);
    }

    public void switchToNextDirection()
    {
        currentIndex = (currentIndex + 1) % directionOrder.size();

        this.activeDirection = directionOrder.get(currentIndex);
    }

    public Direction getCurrentDirection()
    {
        return activeDirection;
    }

    public int getCurrentDuration()
    {
        return trafficController.getGreenDuration(activeDirection);
    }

    public boolean hasRemainingVehicle()
    {
        if (trafficController.vehicleCount.get(activeDirection) > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


}