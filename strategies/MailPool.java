package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.Robot;
import exceptions.BreakingFragileItemException;
import exceptions.ItemTooHeavyException;

import static automail.Simulation.CAUTION_ENABLED;
import static automail.Simulation.FRAGILE_ENABLED;

public class MailPool implements IMailPool {

	private class Item {
		int destination;
		MailItem mailItem;
		
		public Item(MailItem mailItem) {
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.destination > i2.destination) {  // Further before closer
				order = 1;
			} else if (i1.destination < i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;

	public MailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
	}

	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		pool.add(item);
		pool.sort(new ItemComparator());
	}
	
	@Override
	public void step() throws ItemTooHeavyException, BreakingFragileItemException {
		try{
			ListIterator<Robot> i = robots.listIterator();
			while (i.hasNext()) loadRobot(i);
		} catch (Exception e) { 
            throw e; 
        } 
	}

	/**
	 * Initialize the MailPool
	 * */
	private void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException, BreakingFragileItemException{
		Robot robot = i.next();
		assert(robot.isEmpty());
		// System.out.printf("P: %3d%n", pool.size());
		ListIterator<Item> j = pool.listIterator();
		MailItem item;
		if ((pool.size() > 0 && !FRAGILE_ENABLED) || (pool.size()>0 && FRAGILE_ENABLED && !CAUTION_ENABLED)) {
			try {
				robot.addToHand(j.next().mailItem); // hand first as we want higher priority delivered first
				j.remove();
				if (pool.size() > 0) {
					robot.addToTube(j.next().mailItem);
					j.remove();
				}
				robot.dispatch(); // send the robot off if it has any items to deliver
				i.remove();       // remove from mailPool queue
			} catch (Exception e) {
				throw e;
			}

		} else if (pool.size() > 0 && FRAGILE_ENABLED && CAUTION_ENABLED){
			/** When caution mode is on and fragile items are involved */
			try {
				item = j.next().mailItem;
				for (int k = 0; k < 3 ; k++) {
					/** stop the robot receiving mail if getting two fragile items continuously */
					if (robot.getSpecialArms() != null && item.getFragile() && pool.size() > 0) {
						break;
					}

					/** add fragile mail to special arm of the robot */
					if (item.getFragile() && robot.getSpecialArms() == null) {
						robot.addToSpecialHand(item);
						j.remove();
						if(pool.size() > 0 ) {
							item = j.next().mailItem;
							continue;
						} else {
							break;
						}
					}

					/** add normal mail to normal arm of the robot */
					if (pool.size() > 0 && robot.getArms()== null) {
						robot.addToHand(item);  // hand first as we want higher priority delivered first
						j.remove();
						if(pool.size() > 0 ) {
							item = j.next().mailItem;
							continue;
						} else {
							break;
						}
					}

					/** add normal mail to the tube of the robot if normal has item */
					if (pool.size() > 0 && robot.getArms() != null && robot.getTube() == null) {
						robot.addToTube(item);
						j.remove();
						if(pool.size() > 0 ) {
							item = j.next().mailItem;
							continue;
						} else {
							break;
						}
					}

				}
				robot.dispatch(); // send the robot off if it has any items to deliver
				i.remove();       // remove from mailPool queue
			} catch (Exception e) {
				throw e;
			}
		}
	}

	@Override
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

}
