package strategies;

import automail.CautionRobot;
import automail.IMailDelivery;
import automail.NormalRobot;
import automail.Robot;

import static automail.Simulation.CAUTION_ENABLED;

public class Automail {
	      
    public static Robot[] robots;
    public IMailPool mailPool;
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	this.mailPool = mailPool;
    	
    	/** Initialize robots according to the mode */
    	robots = new Robot[numRobots];
    	if(CAUTION_ENABLED) {
            for (int i = 0; i < numRobots; i++) robots[i] = new CautionRobot(delivery, mailPool);
        } else {
            for (int i = 0; i < numRobots; i++) robots[i] = new NormalRobot(delivery, mailPool);
        }
    }

    /**
     * calculate the required statistics and print out
     * @param robot_num is the number of robot assigned
     * */
    public void statistics(int robot_num){
        int totalNormalPackageDelivered=0;
        int totalWrapUnwrapTime=0;
        int totalCautionPackageDelivered=0;
        int totalNormalPackageDeliveredWeight=0;
        int totalCautionPackageDeliveredWeight=0;
        for (int i=0; i<robot_num; i++) {
            totalNormalPackageDelivered+=robots[i].getNormalPackageDelivered();
            totalWrapUnwrapTime+=robots[i].getWrapUnwrapTime();
            totalCautionPackageDelivered+=robots[i].getCautionPackageDelivered();
            totalNormalPackageDeliveredWeight+=robots[i].getNormalPackageDeliveredWeight();
            totalCautionPackageDeliveredWeight+=robots[i].getCautionPackageDeliveredWeight();
        }
        System.out.println("The number of packages delivered normally: "+totalNormalPackageDelivered);
        System.out.println("The number of packages delivered using caution: "+totalCautionPackageDelivered);
        System.out.println("The total weight of the packages delivered normally: "+totalNormalPackageDeliveredWeight);
        System.out.println("The total weight of the packages delivered using caution: "+totalCautionPackageDeliveredWeight);
        System.out.printf("The total amount of time spent by the special arms wrapping & unwrapping items: "+totalWrapUnwrapTime);
    }
    
}
