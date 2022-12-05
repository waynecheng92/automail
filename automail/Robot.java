package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public abstract class Robot {
	
    static public final int INDIVIDUAL_MAX_WEIGHT = 2000;
    public enum RobotState { DELIVERING, WAITING, RETURNING, WRAPPING, UNWRAPPING}
    protected IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public RobotState current_state;
    protected int current_floor;
    protected int destination_floor;
    protected IMailPool mailPool;
    protected boolean receivedDispatch;
    
    protected MailItem deliveryItem = null;
    protected MailItem tube = null;
    protected MailItem fragileItem = null;
    
    protected int deliveryCounter;
    protected int wrapUnwrapTime = 0;
    protected int normalPackageDelivered=0;
    protected int cautionPackageDelivered=0;
    protected int normalPackageDeliveredWeight=0;
    protected int cautionPackageDeliveredWeight=0;

    public int getCautionPackageDelivered() {
        return cautionPackageDelivered;
    }

    public int getWrapUnwrapTime() {
        return wrapUnwrapTime;
    }

    public int getNormalPackageDelivered() {
        return normalPackageDelivered;
    }

    public int getCautionPackageDeliveredWeight() {
        return cautionPackageDeliveredWeight;
    }

    public int getNormalPackageDeliveredWeight() {
        return normalPackageDeliveredWeight;
    }

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param behaviour governs selection of mail items for delivery and behaviour on priority arrivals
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, IMailPool mailPool){
    	id = "R" + hashCode();
        // current_state = RobotState.WAITING;
    	current_state = RobotState.RETURNING;
        current_floor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
    }
    
    public void dispatch() {
    	receivedDispatch = true;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public abstract void step() throws ExcessiveDeliveryException;

    /**
     * Sets the route for the robot
     */
    protected abstract void setRoute();

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    protected void moveTowards(int destination) {
        if(current_floor < destination){
            current_floor++;
        } else {
            current_floor--;
        }
    }
    
    String getIdTube() {
    	return String.format("%s(%1d)", id, (tube == null ? 0 : 1));
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    protected void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (current_state != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    	}
    	current_state = nextState;
    	// if the robot has fragileItem, then print it
    	if(nextState == RobotState.DELIVERING && fragileItem == null){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    	//if the robot does not have the fragileItem, then print the deliveryItem
        if(nextState == RobotState.DELIVERING && fragileItem != null){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), fragileItem.toString());
        }
    }

	public MailItem getTube() {
		return tube;
	}
	public MailItem getSpecialArms() {return fragileItem;}
    public MailItem getArms() {return deliveryItem;}
    
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

    /**
     * Check if robot has delivered all mails
     */
	public boolean isEmpty() {
		return (deliveryItem == null && tube == null );
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {

		assert(deliveryItem == null);

		if(mailItem.fragile) throw new BreakingFragileItemException();

		deliveryItem = mailItem;

		if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();

	}

    public abstract void  addToSpecialHand(MailItem mailItem) throws ItemTooHeavyException;

    public void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(tube == null);
		if(mailItem.fragile) throw new BreakingFragileItemException();
		tube = mailItem;
		if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}
}
