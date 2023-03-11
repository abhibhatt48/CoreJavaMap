import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


class StreetEntry {
    private Node from;
    private Node to;
    private final String streetId;
    // key = coordinates separated with _ Value = Node
    private Map<String, Node> nodes = new HashMap<>();
    private Double distance;

    public StreetEntry(String streetId, Point from, Point to) {

        // Get the node corresponding to the "from" point
        Node fromNode = getNode(from);

        // Throw an exception if the node does not exist
        if (fromNode == null) {
            throw new IllegalArgumentException();
        }

        // Get the node corresponding to the "to" point
        Node toNode = getNode(to);

        // Throw an exception if the node does not exist
        if (toNode == null) {
            throw new IllegalArgumentException();
        }

        // Set the instance variables of the class
        this.from = fromNode;
        this.to = toNode;
        this.streetId = streetId;
    }
    // Get the ID of the street
    public String getStreetId() {
        return streetId;
    }

    // Get the node corresponding to the "to" point
    public Node getTo() {
        return to;
    }
 // Get the node corresponding to the given position
    public Node getNode(Point position) {
        // Create a key for the position by concatenating the X and Y coordinates
        String key = position.getX() + "_" + position.getY();

        // Get the node corresponding to the key from the map
        Node node = nodes.get(key);

        // Throw an exception if the node does not exist
        if (node == null) {
            throw new IllegalArgumentException();
        }

        // Return the node
        return node;
    }


public class MapPlanner {
	
	private int degrees;
    private Map<String, StreetEntry> streets = new HashMap<>();
    protected Location currentLocation;
    private Map<String, Node> nodes = new HashMap<>();
	private Map<String, Node> graph;
    
    private StreetEntry getStreetByStreetId(String id) {
        return this.streets.get(id);
    }

    /**
     * Create the Map Planner object.  The degrees provided tell us how much deviation from straight-forward
     * is needed to identify an actual turn in a route rather than a straight-on driving.
     * @param degrees
     */
    public MapPlanner( int degrees ) {
    	this.degrees = degrees;
    }
    public MapPlanner(Map<String, Node> graph) {
        this.graph = graph;
    }

    /**
     * Identify the location of the depot.  That location is used as the starting point of any route request
     * to a destiation
     * @param depot -- the street ID and side of the street (left or right) where we find the depot
     * @return -- true if the depot was set.  False if there was a problem in setting the depot location.
     */
    public Boolean depotLocation(Location depot) {
        if (streets.containsKey(depot.getStreetId())) {
            this.currentLocation = depot;
            return true;
        }
        return false;
    }
    /**
     * Add a street to our map of the city.  The street is identified by the unique street id.
     * Although the parameters indicate a start and an end to the street, the street is bi-directional.
     * The start and end are just relevant when identifying the side of the street for some location.
     *
     * Street coordinates are in metres.
     *
     * Streets that share coordinates of endpoints meet at an intersection and you can drive from one street to the
     * other at that intersection.
     * @param streetId -- unique identifier for the street.
     * @param start -- coordinates of the starting intersection for the street
     * @param end -- coordinates of the ending entersection for the street
     * @return -- true if the street could be added.  False if the street isn't available in the map.
     */
    public Boolean addStreet(String streetId, Point start, Point end) {
        Node fromNode = getNode(start);
        if (fromNode == null) {
            return false;
        }
        Node toNode = getNode(end);
        if (toNode == null) {
            return false;
        }
        fromNode.addStreet(streetId, start, end);
        toNode.addStreet(streetId, end, start);
        this.streets.put(streetId, new StreetEntry(streetId, start, end));
        return true;
    }

    /**
     *  Given a depot location, return the street id of the street that is furthest away from the depot by distance,
     *  allowing for left turns to get to the street.
     */
    private Node getFurtherestNode(Map<Node, Double> distances) {
        Node maxNode = null;
        double maxValue = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Node, Double> entry : distances.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxNode = entry.getKey();
                maxValue = entry.getValue();
            }
        }
        return maxNode;
    }
    private String getStreetId(List<StreetEntry> streets, Node current) {

        String streetId = "";
        Double maxDistance = Double.NEGATIVE_INFINITY;
        for (var se : streets) {
            if (current.position.distanceTo(se.to.position) > maxDistance) {
                streetId = se.getStreetId();
            }
        }
        return streetId;
    }
    
    public String furthestStreet() {
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(
                Comparator.comparingInt(x -> x.priority));
        Map<Node, Double> distances = new HashMap<>();
        Set<Node> visited = new HashSet<>();
        for (var node : nodes.values()) {
            distances.put(node, Double.MAX_VALUE);
        }
        Node currentNode;
        StreetEntry currentStreet = getStreetByStreetId(currentLocation.getStreetId());
        if (currentStreet != null) {
            if (currentLocation.getStreetSide() == StreetSide.Left) {
                currentNode = currentStreet.from;
            } else {
                currentNode = currentStreet.to;
            }
        } else {
            return "";
        }
        distances.replace(currentNode, 0.0);
        queue.add(new NodeEntry(currentNode, 0));

        while (!queue.isEmpty()) {
            var current = queue.remove().node;
            visited.add(current);

            for (var street : current.getStreets()) {
                if (visited.contains(street.to)) {
                    continue;
                }
                Double newDistance = distances.get(current) + street.from.position.distanceTo(street.to.position);
                if (newDistance < distances.get(street.to)) {
                    if (distances.containsKey(street.to)) {
                        distances.replace(street.to, newDistance);
                    }
                    queue.add(new NodeEntry(street.to, newDistance.intValue()));
                }
            }
        }

        Node furtherestNode = getFurtherestNode(distances);
        var streetsOfFurtherestNode = furtherestNode.getStreets();

        return getStreetId(streetsOfFurtherestNode, currentNode);
    }

    /**
     * Compute a route to the given destination from the depot, given the current map and not allowing
     * the route to make any left turns at intersections.
     * @param destination -- the destination for the route
     * 
     * @return -- the route to the destination, or null if no route exists.
     */
        public Route routeNoLeftTurn(Location destination) {
            // initialize data structures for Dijkstra's algorithm
            Map<Node, Double> distance = new HashMap<>();
            Map<Node, Node> previous = new HashMap<>();
            PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingInt(ne -> ne.priority));
            Set<Node> visited = new HashSet<>();
            final Node startNode = graph.get("depot");
            distance.put(startNode, 0.0);
            queue.offer(new NodeEntry(startNode, 0));

            // run Dijkstra's algorithm
            while (!queue.isEmpty()) {
                NodeEntry currentEntry = queue.poll();
                Node currentNode = currentEntry.node;
                if (visited.contains(currentNode)) {
                    continue;
                }
                visited.add(currentNode);
                if (currentNode.label.equals(destination.getStreetId())) {
                    break;  // found shortest path to destination
                }
                for (StreetEntry street : currentNode.getStreets()) {
                    Node neighborNode = graph.get(street.to.label);
                    if (neighborNode == null) {
                        continue;
                    }
                    Double neighborDistance = distance.getOrDefault(neighborNode, Double.POSITIVE_INFINITY);
                    Double newDistance = distance.get(currentNode) + street.distance;
                    if (newDistance < neighborDistance) {
                          // check if left turn is allowed
                            distance.put(neighborNode, newDistance);
                            previous.put(neighborNode, currentNode);
                            queue.offer(new NodeEntry(neighborNode, newDistance.intValue()));
                    }
                }
            }
            // build the route
            Route route = new Route();
            Node currentNode = graph.get(destination.getStreetId());
            Node loopNode = currentNode;
            while (loopNode != null && !loopNode.label.equals("depot")) {
                Node prevNode = previous.get(loopNode);
                StreetEntry street = prevNode.getStreets().stream()
                        .filter(s -> s.to.label.equals(currentNode.label))
                        .findFirst().orElse(null);
                if (street != null) {
                    route.appendTurn(getTurnDirection(street, prevNode, loopNode), street.getStreetId());
                }
                loopNode = prevNode;
            }
            route.appendTurn(TurnDirection.Left, startNode.getStreets().get(0).getStreetId());  // add first leg
            Collections.reverse(route.loops());
            Collections.reverse(route.getStreets());
            return route;
        }
        private TurnDirection getTurnDirection(StreetEntry street, Node prevNode, Node currentNode) {
            Point prevPoint = prevNode.position;
            Point curPoint = currentNode.position;
            Point nextPoint = street.to.position;
            TurnDirection turnDirection = prevPoint.turnType(curPoint, nextPoint, 20);
            if (turnDirection == TurnDirection.Slight_Left || turnDirection == TurnDirection.Sharp_Left) {
                return TurnDirection.Left;
            } else if (turnDirection == TurnDirection.Slight_Right || turnDirection == TurnDirection.Sharp_Right) {
                return TurnDirection.Right;
            } else {
                return TurnDirection.Straight;
            }
        }
    }
}

