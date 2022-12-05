package exceptions;

/**
 * An exception thrown when the robot tries to deliver more items than its tube capacity without refilling.
 */
public class ExcessiveDeliveryException extends Throwable {
	public ExcessiveDeliveryException(){
		super("Attempting to deliver more than 3 items in a single trip!!");
	}
}
