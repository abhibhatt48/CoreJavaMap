import java.util.ArrayList;
import java.util.List;
/**
 * Define a route to travel in the map.  It's a sequence of turns and streets in the city map.
 *
 * The first leg of a route is leg 1.
 */
public class Route {
	private List<Point> points;
    private List<Location> locations;

    public Route() {
        this.points = new ArrayList<>();
        this.locations = new ArrayList<>();
    }

    /**
     * Grow a Route by adding one step (called a "leg") of the route at a time.  This method adds one more
     * leg to an existing route
     * @param turn -- from the current route, what kind of turn do you make onto the next leg
     * @param streetTurnedOnto -- the street id onto which the next leg of the route turns
     * @return -- true if the leg was added to the route.
     */
    public Boolean appendTurn( TurnDirection turn, String streetTurnedOnto ) {
    	if (locations.isEmpty()) {
            locations.add(new Location(streetTurnedOnto, StreetSide.Right));
        } else {
            StreetSide lastSide = locations.get(locations.size() - 1).getStreetSide();
            StreetSide newSide = (lastSide == StreetSide.Left) ? StreetSide.Right : StreetSide.Left;
            locations.add(new Location(streetTurnedOnto, newSide));
        }
        return points.add(new Point(0, 0));  // just adding a dummy point for now
    }

    /**
     * Given a route, report whether the street of the given leg number of the route.
     *
     * Leg numbers begin with 1.
     * @param legNumber -- the leg number for which we want the next street.
     * @return -- the street id of the next leg, or null if there is an error.
     */
    public String turnOnto( int legNumber ) {
    	if (legNumber < 1 || legNumber > this.locations.size()) {
            return null;
        }
        return this.locations.get(legNumber - 1).getStreetId();
    }

    /**
     * Given a route, report whether the type of turn that initiates the given leg number of the route.
     *
     * Leg numbers begin with 1.
     * @param legNumber -- the leg number for which we want the next turn.
     * @return -- the turn direction for the leg, or null if there is an error.
     */
    public TurnDirection turnDirection( int legNumber ) {
    	if (legNumber <= 0 || legNumber > locations.size()) {
            return null;
        }
        if (legNumber == 1) {
            return TurnDirection.Straight;
        }
        Location prevLocation = locations.get(legNumber - 2);
        Location curLocation = locations.get(legNumber - 1);
        if (prevLocation.getStreetId().equals(curLocation.getStreetId())) {
            return TurnDirection.Straight;
        }
        if (prevLocation.getStreetSide() == StreetSide.Left) {
            return (curLocation.getStreetSide() == StreetSide.Right) ? TurnDirection.Left : TurnDirection.Right;
        } else {
            return (curLocation.getStreetSide() == StreetSide.Left) ? TurnDirection.Right : TurnDirection.Left;
        }
    }

    /**
     * Report how many legs exist in the current route
     * @return -- the number of legs in this route.
     */
    public int legs() {
    	return this.locations.size();
    }

    /**
     * Report the length of the current route.  Length is computed in metres by Euclidean distance.
     *
     * By assumption, the route always starts and ends at the middle of a road, so only half of the length
     * of the first and last leg roads contributes to the length of the route
     * @return -- the length of the current route.
     */
    public Double length() {
    	double length = 0;
        for (int i = 1; i < points.size(); i++) {
            Point prevPoint = points.get(i - 1);
            Point curPoint = points.get(i);
            length += prevPoint.distanceTo(curPoint);
        }
        return length / 2;  // assuming first and last legs only contribute half to length
    }

    /**
     * Given a route, return all loops in the route.
     *
     * A loop in a route is a sequence of streets where we start and end at the same intersection.  A typical
     * example of a loop would be driving around the block in a city.  A loop does not need you to start and end
     * the loop going in the same direction.  It's just a point of driving through the same intersection again.
     *
     * A route may contain more than one loop.  Return the loops in order that they start along the route.
     *
     * If one loop is nested inside a larger loop then only report the larger loop.
     * @return -- a list of subroutes (starting and ending legs) of each loop.  The starting leg and the ending leg
     * share a common interesection.
     */
    public List<SubRoute> loops() {
    	List<SubRoute> loops = new ArrayList<>();
        List<Integer> loopEnds = new ArrayList<>();
        for (int i = 1; i <= locations.size(); i++) {
            for (int j = i + 1; j <= locations.size(); j++) {
                if (locations.get(i - 1).getStreetId().equals(locations.get(j - 1).getStreetId())) {
                    List<Location> subLocations = locations.subList(i - 1, j);
                    if (subLocations.size() > 1 && subLocations.get(0).getStreetSide() == subLocations.get(subLocations.size() - 1).getStreetSide()) {
                        loopEnds.add(j);
                    }
                }
            }
        }
        int prevEnd = 0;
        for (int end : loopEnds) {
            loops.add(new SubRoute(this, prevEnd + 1, end));
            prevEnd = end;
        }
        return loops;
    }

    /**
     * Given a route, produce a new route with simplified instructions.  The simplification reports a route
     * that reports the turns in the route but does not report the points where you should keep going straight
     * along your current path.
     * @return -- the simplified route.
     */
    public Route simplify() {
    	List<Location> simplifiedLocations = new ArrayList<>();
        simplifiedLocations.add(locations.get(0));
        for (int i = 1; i < locations.size() - 1; i++) {
            if (!shouldSkip(locations.get(i - 1), locations.get(i), locations.get(i + 1))) {
                simplifiedLocations.add(locations.get(i));
            }
        }
        simplifiedLocations.add(locations.get(locations.size() - 1));

        Route simplifiedRoute = new Route();
        for (Location loc : simplifiedLocations) {
            simplifiedRoute.appendTurn(TurnDirection.Straight, loc.getStreetId());
        }
        return simplifiedRoute;
    }

    private boolean shouldSkip(Location prevLoc, Location curLoc, Location nextLoc) {
        return prevLoc.getStreetId().equals(curLoc.getStreetId()) ||
               curLoc.getStreetId().equals(nextLoc.getStreetId()) ||
               prevLoc.getStreetId().equals(nextLoc.getStreetId());
    }
}
