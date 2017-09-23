import com.orbischallenge.firefly.client.objects.models.EnemyUnit;
import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.World;
import com.orbischallenge.firefly.client.objects.models.Tile;

import com.orbischallenge.game.engine.Point;
import com.orbischallenge.firefly.objects.enums.Direction;

import com.orbischallenge.logging.Log;

import java.util.*;

public class PlayerAI {
    // Any field declarations go here
	int turn = 0;
	Point position;
	EnemyUnit enemy;
	int distance;
//	ArrayList<EnemyUnit> empty = new ArrayList<EnemyUnit>();
//	Map<String, ArrayList<Point>> unitIDs = new HashMap<string, ArrayList<Point>>();
	ArrayList<Point> avoidPoint = null;
	
    public PlayerAI() {
        // Any instantiation code goes here

    }

    /**
     * This method will get called every turn.
     *
     * @param world The latest state of the world.
     * @param friendlyUnits An array containing all remaining firefly units in your team
     * @param enemyUnits An array containing all remaining enemy firefly units
     *
     */
    public void doMove(World world, FriendlyUnit[] friendlyUnits, EnemyUnit[] enemyUnits) {
        /* Fly away to freedom, daring fireflies
        Build thou nests
        Grow, become stronger
        Take over the world */
    		// Tries to create a map of unclustered nests
    		if (turn == 0) {
    			Point mainNest = world.getFriendlyNestPositions()[0];
    			int path_one, path_two;
    			path_one = path_two = 0;
    			path_one = world.getShortestPathDistance(mainNest, mainNest.add(new Point(2, 1)));
    			path_one = world.getShortestPathDistance(mainNest, mainNest.add(new Point(-1, 2)));
    			path_one = world.getShortestPathDistance(mainNest, mainNest.add(new Point(1, -2)));
    			path_one = world.getShortestPathDistance(mainNest, mainNest.add(new Point(-2, -1)));
    			
    			path_two = world.getShortestPathDistance(mainNest, mainNest.add(new Point(2, -1)));
    			path_two = world.getShortestPathDistance(mainNest, mainNest.add(new Point(1, 2)));
    			path_two = world.getShortestPathDistance(mainNest, mainNest.add(new Point(-2, 1)));
    			path_two = world.getShortestPathDistance(mainNest, mainNest.add(new Point(-1, -2)));
    			
    			avoidPoint = new ArrayList<Point>();

    			if (path_one > path_two) {
    				for (int i=1; i<6; i++) {
    					avoidPoint.add(new Point(2*i, i));
    					avoidPoint.add(new Point(-i, 2*i));
    					avoidPoint.add(new Point(i, -2*i));
    					avoidPoint.add(new Point(-2*i, -i));
    				}
    			} else {
    				for (int i=1; i<6; i++) {
    					avoidPoint.add(new Point(2*i, -i));
    					avoidPoint.add(new Point(i, 2*i));
    					avoidPoint.add(new Point(-i, -2*i));
    					avoidPoint.add(new Point(-2*i, i));
    				}
    			}
    		} elif (turn == 20) {
    			avoidPoint = null;
    		}
    		//incrementing turns
    		turn++;
    		// Already in a winning position. Ensuring we have enough score before taking over
    		// the last nest
    		if (world.getavoidPointPositions().length == 1) {
    			if (turn < 90 && world.getFriendlyTiles().length <= world.getEnemyTiles().length) {
    				avoidPoint = new ArrayList<Point>();
    				Point theNest = world.getavoidPointPositions()[0];
    				avoidPoint.add(theNest.add(new Point(1,0)));
    				avoidPoint.add(theNest.add(new Point(-1,0)));
    				avoidPoint.add(theNest.add(new Point(0,1)));
    				avoidPoint.add(theNest.add(new Point(0,-1)));
    			} else {
    				avoidPoint = null;
    			}
    		};
    		
        for (FriendlyUnit unit: friendlyUnits) {
//        		if (turn < 20) { // CONQUER!
//        			//add if does not contain
//        			String uid = unit.getUuid();
//        			if (!unitIDs.containsKey(uid)) {
//        				unitIDs[uid] = 
//        			}
//        				
//        		} else { // DESTROY!!
            		position = unit.getPosition();
            		// Can optimize by choosing the "biggest enemy" or "killable" enemy later on
            		enemy = world.getClosestEnemyFrom(position, null);
            		distance = world.getShortestPathDistance(position, enemy.getPosition());
            		if (distance == 1) {
            			world.move(unit, enemy.getPosition());
            		} else if (distance < 4) {
            			// Tactic 1
	    				if ((enemy.getHealth()+3) > unit.getHealth()) {
	    					world.move(unit, position);
	    				} else {
	    					conquer (world, unit);
	    				}
            			// Tactic 2
//	    				if (!((enemy.getHealth()-4) > unit.getHealth() || (enemy.getHealth()+4) < unit.getHealth())) {
//	    					world.move(unit, position);
//	    				} else {
//	    					conquer (world, unit);
//	    				}
	    			} else {
	    				conquer (world, unit);
	    			}
//        		}

        }
    }
    
    public void conquer (World world, FriendlyUnit unit) {
        List<Point> path = world.getShortestPath(unit.getPosition(),
                world.getClosestCapturableTileFrom(unit.getPosition(), null).getPosition(),
                avoidPoint);
        	if (path == null) {
        		path = world.getShortestPath(unit.getPosition(),
        				world.getClosestCapturableTileFrom(unit.getPosition(), null).getPosition(), 
        				null);
        	}
        	if (path != null) world.move(unit, path[0])

        	
    }
    

}