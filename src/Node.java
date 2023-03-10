import java.util.ArrayList;
import java.util.List;

public class Node {
		Point position;
        String label;
        private List<StreetEntry> connectedStreets = new ArrayList<>();

        public List<StreetEntry> getStreets() {
            return connectedStreets;
        }

        Node(int x, int y) {
            this.position = new Point(0, 0);
            this.label = x + "_" + y;
        }
        public boolean addStreet(String streetId, Point from, Point to) {
            try {
                connectedStreets.add(new StreetEntry(streetId, this.position, to));
                return true;
            } catch (Exception e) {
                return false;
            }
        }       

    }