import java.util.Random;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit discovered " + unit.getType());
    }


    @Override
    public void onUnitDiscover(Unit arg0) {
    	System.out.println("New unit discovered, with discover " + arg0.getType());
    	super.onUnitDiscover(arg0);
    }
    
    @Override
    public void onUnitShow(Unit arg0) {
    	System.out.println("A new unit has shown " + arg0.getType());
    	super.onUnitShow(arg0);
    }
    
    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();

        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        	System.out.println();
        }

    }

    //Get the position for a next Supply Position
    public TilePosition getNewSupplyDepotPosition(){
    	int i = 0;
    	Unit closestMineral = null;
    	for(Unit myUnit : self.getUnits()){
    		if(myUnit.getType() == UnitType.Terran_Supply_Depot){
    			i++;
    		}
    		if(myUnit.getType() == UnitType.Terran_Command_Center){
    			closestMineral = getClosestMineral(myUnit);
    		}
    	//closestMineral = getClosestMineral(self.getStartLocation());
        }
    	int x = 2 * self.getStartLocation().getX() - closestMineral.getTilePosition().getX() + 10 + (2 * i);
    	int y = 2 * self.getStartLocation().getY() - closestMineral.getTilePosition().getY();
    	
    	System.out.println( closestMineral.getTilePosition().getX() + "  " +  closestMineral.getTilePosition().getY());
    	
    	return new TilePosition(x,y);
    }
    
    //Find the closest mineral relative to a unit
    public Unit getClosestMineral(Unit myUnit){
    	Unit closestMineral = null;
        for (Unit neutralUnit : game.neutral().getUnits()) {
            if (neutralUnit.getType().isMineralField()) {
                if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                    closestMineral = neutralUnit;
                }
            }
        }
        return closestMineral;
    }
    
    //Find the closest mineral relative to a position
    public Unit getClosestMineral(TilePosition position){
    	Unit closestMineral = null;
        for (Unit neutralUnit : game.neutral().getUnits()) {
            if (neutralUnit.getType().isMineralField()) {

            	double distanceUnitNeutralUnit = position.getDistance(neutralUnit.getTilePosition());
            	double distanceUnitClosestMineral = position.getDistance(closestMineral.getTilePosition());
            	
                if (closestMineral == null || distanceUnitNeutralUnit < distanceUnitClosestMineral) {
                    closestMineral = neutralUnit;
                }
            }
        }
        System.out.println(closestMineral.getTilePosition().getX() + " " + closestMineral.getTilePosition().getY());
        return closestMineral;
    }
    
    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        StringBuilder units = new StringBuilder("My units:\n");

        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

            //if there's enough minerals, train an SCV
            if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50) {
                myUnit.train(UnitType.Terran_SCV);
            }
            
            //if there´s no supplies left, build a new supply depot
            if(self.supplyUsed() >= self.supplyTotal() && myUnit.getType() == UnitType.Terran_SCV && self.minerals() >= 100){
            	//myUnit.build(UnitType.Terran_Supply_Depot, getNewSupplyDepotPosition());
            	Random r = new Random();
            	myUnit.build(UnitType.Terran_Supply_Depot, new TilePosition(r.nextInt(500), r.nextInt(500)));
            }

            //if it's a worker and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle()) {
               Unit closestMineral = getClosestMineral(myUnit);

                //if a mineral patch was found, send the worker to gather it
                if (closestMineral != null) {
                    myUnit.gather(closestMineral, false);
                }
            }
        }

        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}