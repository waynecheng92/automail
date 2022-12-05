package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;

public class NormalRobot extends Robot{
    public NormalRobot(IMailDelivery delivery, IMailPool mailPool) {
        super(delivery, mailPool);
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException {
        switch(current_state) {
            /** This state is triggered when the robot is returning to the mailroom after a delivery */
            case RETURNING:
                /** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == Building.MAILROOM_LOCATION){
                    if (tube != null) {
                        mailPool.addToPool(tube);
                        System.out.printf("T: %3d >  +addToPool [%s]%n", Clock.Time(), tube.toString());
                        tube = null;
                    }
                    /** Tell the sorter the robot is ready */
                    mailPool.registerWaiting(this);
                    changeState(RobotState.WAITING);
                } else {
                    /** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(Building.MAILROOM_LOCATION);
                    break;
                }
            case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch){
                    receivedDispatch = false;
                    deliveryCounter = 0; // reset delivery counter
                    setRoute();
                    changeState(RobotState.DELIVERING);
                }
                break;
            case DELIVERING:
                if(current_floor == destination_floor){ // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
                    normalPackageDelivered++; //finish delivering one more item
                    normalPackageDeliveredWeight+=deliveryItem.getWeight(); //add to the total delivery weight
                    delivery.deliver(deliveryItem);
                    deliveryItem = null;
                    deliveryCounter++;
                    if (deliveryCounter > 2) {  // Implies a simulation bug
                        throw new ExcessiveDeliveryException();
                    }
                    /** Check if want to return, i.e. if there is no item in the tube*/
                    if (tube == null && fragileItem == null) {
                        changeState(RobotState.RETURNING);
                    } else {
                        /** If there is another item, set the robot's route to the location to deliver the item */
                        deliveryItem = tube;
                        tube = null;
                        setRoute();
                        changeState(RobotState.DELIVERING);
                    }

                } else {
                    /** The robot is not at the destination yet, move towards it! */
                    moveTowards(destination_floor);

                }
                break;
        }
    }

    /**
     * Sets the route for the robot
     */
    @Override
    protected void setRoute() {
        /** Set the destination floor */
        if (fragileItem == null) {
            destination_floor = deliveryItem.getDestFloor();
        } else {
            destination_floor = fragileItem.getDestFloor();
        }
    }

    @Override
    public void addToSpecialHand(MailItem mailItem) {
        assert(fragileItem == null);
    }
}

