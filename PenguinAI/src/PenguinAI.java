import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class PenguinAI extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    private static PenguinAI penguinAI;
    
    /*
     * Die Phase in der sich der Spieler befindet, wird benötigt um herauszufinden was als nächstes getan werden muss
     */
    private static Phase phase;

    /*
     * Überprüft die aktuelle Phase ruft die entsprechende Phase auf und läutet nächste Phasen ein
     */
    private PhaseHandler phaseHandler;
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    /*
     * Gibt die aktuelle Phase zurück
     * 
     * @return Die aktuelle Phase
     */
    public static Phase getPhase(){
    	return phase;
    }
    
    /*
     * Setzt die aktuelle Phase auf eine neue Phase
     * 
     * @param givenPhase Die Phase die eingeläutet werden soll
     */
    public static void setPhase(Phase givenPhase){
    	phase = givenPhase;
    }
    
    @Override
    public void onUnitCreate(Unit unit) {
    }


    @Override
    public void onUnitDiscover(Unit arg0) {
    	super.onUnitDiscover(arg0);
    }
    
    @Override
    public void onUnitShow(Unit arg0) {
    	super.onUnitShow(arg0);
    }
    
    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        //phase = Phase.FIRST_BUILDORDER;
        phaseHandler = new PhaseHandler();

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

    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        StringBuilder units = new StringBuilder("My units:\n");

        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
    }

    public static void main(String[] args) {
        penguinAI = new PenguinAI();
        penguinAI.run();
    }
}