package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;


import static automail.Simulation.STATISTICS_ENABLED;
import static strategies.Automail.robots;

public class CautionRobot extends Robot{

    private static final int REQUIRED_WRAP_TIME = 2;      //time required to wrap fragile item
    private static final int REQUIRED_UNWRAP_TIME = 1;    //time required to unwrap fragile item
    private int wrapTime;
    private int unwrapTime; 

    public CautionRobot(IMailDelivery delivery, IMailPool mailPool) {
        super(delivery, mailPool);
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException {

        switch(current_state) {
            case WRAPPING:
                wrapTime--;
                if(wrapTime == 0){
                    changeState(RobotState.DELIVERING);
                }
                break;
            /** This state is triggered when the robot is unwrapping a fragile item */
            case UNWRAPPING:
                cautionPackageDelivered++;
                cautionPackageDeliveredWeight+=fragileItem.getWeight();
                delivery.deliver(fragileItem);
                fragileItem = null;
                deliveryCounter++;
                /** see if there is mail left to deliver and change state accordingly */
                if(deliveryItem == null){
                    changeState(RobotState.RETURNING);
                } else {
                    setRoute();
                    changeState(RobotState.DELIVERING);
                }
                break;

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
                }
                else if(waitForUnwrap()){
                    /** Freeze in the same level if any robot is delivering fragile item around */
                    break;
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
                    if(fragileItem != null){
                        /** Reset wrapping time if receive a fragile item to deliver */
                        wrapTime = REQUIRED_WRAP_TIME;
                        unwrapTime = REQUIRED_UNWRAP_TIME;
                        wrapUnwrapTime+=wrapTime;
                        wrapUnwrapTime+=unwrapTime;
                        changeState(RobotState.WRAPPING);
                    } else {
                        changeState(RobotState.DELIVERING);
                    }
                }
                break;
            case DELIVERING:
                if(current_floor == destination_floor){ // If already here check whether drop off or unwrap
                    /** Delivery complete, report this to the simulator! */
                    if(fragileItem != null){
                        changeState(RobotState.UNWRAPPING);
                    } else {
                        if (STATISTICS_ENABLED){
                            normalPackageDelivered++;
                            normalPackageDeliveredWeight+=deliveryItem.getWeight();
                        }
                        delivery.deliver(deliveryItem);
                        deliveryItem = null;
                        deliveryCounter++;
                        if (deliveryCounter > 3) {  // Implies a simulation bug
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
                    }
                } else if(waitForUnwrap()){
                    /** Freeze in the same level if any robot is delivering fragile item around */
                    break;
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

    /**
     * Check if the robot has sent all mails with it
     */
    @Override
    public boolean isEmpty() {
        return (deliveryItem == null && tube == null && fragileItem == null);
    }

    /**
     * Decide whether to wait for unwrapping fragile item
     */
    public boolean waitForUnwrap(){
        for(Robot r: robots){
            if(r.current_state.equals("UNWRAPPING") || (r.fragileItem!=null && r.current_floor == r.destination_floor)){
                if((r.current_floor == current_floor+1) || (r.current_floor == current_floor-1)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addToSpecialHand(MailItem mailItem) throws ItemTooHeavyException {
        assert(fragileItem == null);
        fragileItem = mailItem;
        if (fragileItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
    }
}
