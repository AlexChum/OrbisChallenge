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
	// Game Data
	int turn = 0;
	int tilesNum;
	// To avoid reinitialization of the variable through everyloop
	boolean dead;
	String uid;
	Point position;
	EnemyUnit enemy;
	int distance;
	Map<Direction, Point> neighbours;
	// ArrayLists of Points that shortestPath will try to avoid
	ArrayList<Point> winningTiles = new ArrayList<Point>(); // The enemy's LAST nest to be avoided
	ArrayList<Point> avoidPoint = new ArrayList<Point>(); // My own nests position + winningTiles
	ArrayList<Point> toConquer = new ArrayList<Point>(); // Priority Nests
	Map<String, Point> ConquerMap = new HashMap<String, Point>(); // Assigned priority Nests
	
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
    	
    		// Initializes a map of unclustered nests
    		// Assigns the number of tiles on this map
    		if (turn == 0) {
    			tilesNum = world.getTiles().length;
    			
    			noClusters (world);
    			findEasyNests (world);
    		} else if (turn == 20) { // Clears position of all nests
    			avoidPoint.clear();
    		}
    		
    		//incrementing turns
    		turn++;
    		
    		//TODO: REMOVE BEFORE SUBMITTING
    		System.out.println(turn);
    		
    		// Already in a winning position (Opponent only has 1 nest after 13 turns). 
    		// Ensuring we have enough every score available before taking over the last nest
    		if (turn > 13 && world.getEnemyNestPositions().length == 1) {
	    		lastNest (world);
    		} else {
    			winningTiles.clear();
    		}
    		
    		// Looping through "directed" fireflies
    		for (Map.Entry<String, Point> entry: ConquerMap.entrySet()) {
    			dead = true;
    			for (FriendlyUnit unit: friendlyUnits) {
    				if (entry.getKey().equals(unit.getUuid())) {
    					dead = false;
    				}
    			}
    			if (dead) {
    				toConquer.add(ConquerMap.remove(entry.getKey()));
    			}
    		}
    		
    		//Looping through every unit
        for (FriendlyUnit unit: friendlyUnits) {
        		position = unit.getPosition();
        		uid = unit.getUuid();
        		// Can optimize by choosing the "biggest enemy" or "killable" enemy later on
        		enemy = world.getClosestEnemyFrom(position, null);
        		distance = world.getShortestPathDistance(position, enemy.getPosition());
        		if (distance == 1 && !winningTiles.contains(enemy.getPosition())) {
        			world.move(unit, enemy.getPosition());
        		} else if (distance < 4) {
        			// Tactic 1
    				if ((enemy.getHealth()+3) > unit.getHealth()) {
    					world.move(unit, position);
    				} else {
    					conquer (world, unit);
    				}
    				// Implementation not working
    			} else if (ConquerMap.containsKey(uid)) {
    				conquer (world, unit, ConquerMap.get(uid));
    			} else if (!toConquer.isEmpty()) {
    				int shortestIndex = 1000;
    				for (Point p: toConquer) {
    					if (world.getShortestPathDistance(position, p)< shortestIndex) {
    						shortestIndex = toConquer.indexOf(p);
    					}
    				}
    				ConquerMap.put(uid, toConquer.remove(0));
    				conquer (world, unit, ConquerMap.get(uid));
    			} else {
    				conquer (world, unit);
    			}
        			// Tactic 2
//	    				if (!((enemy.getHealth()-4) > unit.getHealth() || (enemy.getHealth()+4) < unit.getHealth())) {
//	    					world.move(unit, position);
//	    				} else {
//	    					conquer (world, unit);
//	    				}
        }
    }
    
    /**
     * This method is the original AI pattern with a some points to avoid in its Path
     * 
     * @param world The latest state of the world.
     * @param unit The unit currently undergoing <doMove>
     */
    public void conquer (World world, FriendlyUnit unit) {
    		List<Point> path = null;
    		if (turn > 15 && winningTiles.isEmpty()) {
	        path = world.getShortestPath(unit.getPosition(),
	                world.getClosestEnemyNestFrom(unit.getPosition(), avoidPoint),
	                avoidPoint);
    		}
        	if (path == null) {
        		path = world.getShortestPath(unit.getPosition(),
        				world.getClosestCapturableTileFrom(unit.getPosition(), avoidPoint).getPosition(), 
        				avoidPoint);
        	}
        	if (path == null) {
        		path = world.getShortestPath(unit.getPosition(),
        				world.getClosestCapturableTileFrom(unit.getPosition(), avoidPoint).getPosition(), 
        				null);
        	}
        	if (path != null) world.move(unit, path.get(0));
    }
    
    
    public void conquer (World world, FriendlyUnit unit, Point destination) {
        	List<Point> path = world.getShortestPath(unit.getPosition(), destination, avoidPoint);
        	if (path == null) {
        		if (ConquerMap.containsKey(unit.getUuid())) {
            		ConquerMap.remove(unit.getUuid());
        		}
        		conquer (world, unit);
        	} else {
        		world.move(unit, path.get(0));
        	}
    }

    		// Unimplemented Feature
//    public void chooseEnemy (World world, FriendlyUnit unit, EnemyUnit[] enemy) {
//		neighbours = world.getNeighbours(unit.getPosition());
//		for (Map.Entry<Direction, Point> entry: neighbours.entrySet()) {
//		}
//    }
    
    public void findEasyNests (World world) {
    		Point startNest = world.getFriendlyNestPositions()[0];
    		int walls;
    		List<Point> entries = new ArrayList<Point>();
    		Point tile;
    		for (int i=0; i<19; i++) {
    			for (int j=0; j<19; j++) {
    				tile = new Point(i,j);
	    			neighbours = world.getNeighbours(tile);
	    			walls = 0;
	    			entries.clear();
	    			for (Map.Entry<Direction, Point> entry: neighbours.entrySet()) {
	    				if (world.isWall(entry.getValue()))	{
	    					walls++;
	    				} else {
	    					entries.add(entry.getValue());
	    				}
	    			}
	    			if (walls > 1) {
	    				if (world.getShortestPathDistance(startNest, tile) < 20) {
	    					avoidPoint.add(tile);
	    					for (Point p: entries) {
	    						toConquer.add(p);
	    					}
	    				}
	    			}
    			}
    	    	}
    }
    
    /**
     * This method adds to ArrayList<Point> the position of the last nest 
     * such that the fireflies will avoid those Points.
     * 
     * @param world The latest state of the world.
     */
    public void lastNest (World world) {
    	
		int permanentTiles = 1;
		for (Tile tile: world.getEnemyTiles()) {
			if (tile.isPermanentlyOwned())	permanentTiles++;
		}
		permanentTiles += world.getEnemyTilesAround(world.getEnemyNestPositions()[0]).length;
		
		if (world.getFriendlyTiles().length == (tilesNum - permanentTiles) || turn > 90) {
			avoidPoint.clear();
			winningTiles.clear();
		} else {
			Point theNest = world.getEnemyNestPositions()[0];
			winningTiles.add(theNest);
			winningTiles.add(theNest.add(new Point(1,0)));
			winningTiles.add(theNest.add(new Point(-1,0)));
			winningTiles.add(theNest.add(new Point(0,1)));
			winningTiles.add(theNest.add(new Point(0,-1)));
			avoidPoint.add(theNest);
			avoidPoint.add(theNest.add(new Point(1,0)));
			avoidPoint.add(theNest.add(new Point(-1,0)));
			avoidPoint.add(theNest.add(new Point(0,1)));
			avoidPoint.add(theNest.add(new Point(0,-1)));
		}
    }
    
    /**
     * This method adds to ArrayList<Point> the position of the nearest *defined* 
     * unclustered nest positions such that the fireflies will avoid those Points.
     * 
     * There are the predefined paths for unclustered nests
     * 
     * @param world The latest state of the world.
     */
    public void noClusters (World world) {		
		Point mainNest = world.getFriendlyNestPositions()[0];
		int path_one, path_two, x, y;
		path_one = path_two = 0;
		// Brute force computation
//		// Looped Computation does not seem to work because of world.getShortestPathDistance
//		// It does not like references as arguments
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(2, 1)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-1, 2)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(1, -2)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-2, -1)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(3, -1)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-3, 1)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(1, 3)));
		path_one += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-1, -3)));
		    			
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(2, -1)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(1, 2)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-2, 1)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-1, -2)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(3, 1)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-3, -1)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(1, -3)));
		path_two += world.getShortestPathDistance(mainNest, mainNest.add(new Point(-1, 3)));
		
//		// Looped Computation
//		mainNest = mainNest.add(new Point(1, -3));
//		x = mainNest.getX();
//		y = mainNest.getY();
//		for (int i=0; i<3; i++) {
//			for (int j=0; j<3; j++) {
//				path_one += world.getShortestPathDistance(mainNest, new Point(x+j, y+2*j));
//			}
//			x -= 2;
//			y++;
//		}
//		System.out.println(path_one);
//		//Path two 3x3
//		mainNest = mainNest.add(new Point(-1, -3));
//		x = mainNest.getX();
//		y = mainNest.getY();
//		for (int i=0; i<3; i++) {
//			for (int j=0; j<3; j++) {
//				path_two += world.getShortestPathDistance(mainNest, new Point(x+2*j, y+j));
//			}
//			x--;
//			y += 2;
//		}
		
		if (path_one > path_two) {
			//Starting for 5x5 (2, -6)
			//Starting for 7x7 (3, -9)
			mainNest = mainNest.add(new Point(3, -9));
			x = mainNest.getX();
			y = mainNest.getY();
			for (int i=0; i<7; i++) {
				for (int j=0; j<7; j++) {
					avoidPoint.add(new Point(x+j, y+2*j));
				}
				x -= 2;
				y++;
			}
		} else {
			//Starting for 5x5 (-2, -6)
			//Starting for 7x7 (-3, -9)
			mainNest = mainNest.add(new Point(-3, -9));
			x = mainNest.getX();
			y = mainNest.getY();
			for (int i=0; i<7; i++) {
				for (int j=0; j<7; j++) {
					avoidPoint.add(new Point(x+2*j, y+j));
				}
				x--;
				y += 2;
			}
		}
    }

}
