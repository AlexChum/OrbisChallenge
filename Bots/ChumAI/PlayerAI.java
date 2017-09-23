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
	ArrayList<EnemyUnit> empty = new ArrayList<EnemyUnit>();
	Map<String> unitIDs;
	
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
    		turn = 20;
        for (FriendlyUnit unit: friendlyUnits) {
        		if (turn < 20) { // CONQUER!
        			
        			
        		} else { // DESTROY!!
            		position = unit.getPosition();
            		enemy = world.getClosestEnemyFrom(position, empty);
            		distance = world.getShortestPathDistance(position, enemy.getPosition());
            		if (distance == 1) {
            			world.move(unit, enemy.getPosition());
            		} else if (distance < 4) {
    				if (!((enemy.getHealth()-4) > unit.getHealth() || (enemy.getHealth()+4) < unit.getHealth())) {
    					world.move(unit, position);
    				}
	    			} else {
	    	            List<Point> path = world.getShortestPath(unit.getPosition(),
	                            world.getClosestCapturableTileFrom(unit.getPosition(), null).getPosition(),
	                            null);
	    	            	if (path != null) world.move(unit, path.get(0));
	    			}
        		}

        }
    }
    

}