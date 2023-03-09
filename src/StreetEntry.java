import java.util.HashMap;
import java.util.Map;

public class StreetEntry {
        Node from;
        Node to;
        String streetId;
        // key = coordinates separated with _ Value = Node
        private Map<String, Node> nodes = new HashMap<>();

        public StreetEntry(String streetId, Point from, Point to) {

            Node fromNode = getNode(from);

            if (fromNode == null) {
                throw new IllegalArgumentException();
            }
            Node toNode = getNode(to);
            if (toNode == null) {
                throw new IllegalArgumentException();
            }

            this.from = fromNode;
            this.to = toNode;
            this.streetId = streetId;
        }

		private Node getNode(Point position) {
			Node node = nodes.get(position.getX() + "_" + position.getY());
	        if (node == null) {
	            throw new IllegalArgumentException();
	        }
	        return node;
		}

    }