import jbotsim.Message;
import jbotsim.Node;
import java.awt.*;

public class Sensor extends Node {
    private Node parent = null;
    int battery = 255;
    Boolean batteryZero = false;
    private int time = 0;

    private Boolean send = true;

    @Override
    public void onMessage(Message message) {
        // "INIT" flag : construction of the spanning tree
        // "SENSING" flag : transmission of the sensed values
        // You can use other flags for your algorithms
        if (message.getFlag().equals("INIT")) {
            // if not yet in the tree
            if (parent == null) {
                // enter the tree
                parent = message.getSender();
                getCommonLinkWith(parent).setWidth(4);
                // propagate further
                sendAll(message);
                //send(parent, new Message(node, "BAT"));
            }
        } else if (message.getFlag().equals("SENSING")) {
            // retransmit up the tree
            send(parent, message);
        }
        else if(message.getFlag().equals("PARENT")){
            time = 0;
            send(parent, message);
        }
    }

    @Override
    public void send(Node destination, Message message) {
        if (battery > 0) {
            super.send(destination, message);
            battery--;
            if(battery == 0 && !batteryZero){
                System.out.println("Battery " + getTime()+" "+this.getID());
                batteryZero = true;
                System.exit(0);
            }
            updateColor();
        }
    }

    @Override
    public void onClock() {
        if (parent != null) { // if already in the tree
            if (Math.random() < 0.02) { // from time to time...
                double sensedValue = Math.random(); // sense a value
                send(parent, new Message(sensedValue, "SENSING")); // send it to parent
            }
            if(this.time > 0 && send){
                this.send = false;
                Node node = new Node();
                node.setLocation(this.getX(), this.getY());
                send(parent, new Message(node, "PARENT"));
                time = 0;
            }
            else {
                time++;
            }
        }
    }

    protected void updateColor() {
        setColor(battery == 0 ? Color.red : new Color(255 - battery, 255 - battery, 255));
    }
}