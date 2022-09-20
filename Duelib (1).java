package IS;
import robocode.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Duelist - a robot by Solomon Berry, Tanner Glazier, and Brennan Williams
 */

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.geom.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;

import java.awt.*;



public class Duelib extends RadiansRobot //Radians robot is a class that I implemented that allows me to use radians in my linear targeting algorithm, which was a lot easier to do the math with

{
	boolean peek; // Variable to control whether or not to turn based on if there's a robot there
	double moveAmount; // Max move amount, that will be set to the max between the battleground height and width
	static int corner = 0; // Which corner we are currently using
	// static so that it keeps it between rounds.
	
	boolean stopWhenSeeRobot = false; // To make sure we don't hit a robot on our way to a corner in the goCorner() algorithm

	
	/**
	 * run: Duelist's default behavior
	 */
	public void run() {


		setColors(new Color(240, 128, 240),new Color(150, 150, 255),Color.pink); // body,gun,radar are all set to a variety of pink
		

		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		// Initialize peek to false
		peek = false;

		// turnLeft to face a wall.
		turnLeft(getHeading() % 90);
		ahead(moveAmount);
		// Turn the gun to turn right 90 degrees.
		peek = true;
		turnGunRight(90);
		turnRight(90);

		//Start out moving around the corners while there is still more than one other opponent
		while (getOthers() > 1) { 
			// Look before we turn when ahead() completes
			peek = true;
			// Move up the wall
			ahead(moveAmount);
			// Don't look now
			peek = false;
			// Turn to the next wall
			turnRight(90);
		}



		// Move to a corner, was going to see if I could improve the corner algorithm, but didn't have time as I focused on targeting for the most part
		goCorner();
		
		//Once we get to the final 1v1 we switch to a complex corner movement, and scan at every point
		while(true)
		{
			//30, 30
			turnGunRight(360);
			ahead(180); //(30,210)
			turnGunRight(360);
			turnRight(161.565);
			ahead(189.737); //(90, 30)
			turnGunRight(360);
			turnRight(171.87); 
			ahead(134.164); //(30, 150)
			turnGunRight(360);
			turnRight(116.565);
			ahead(134.164); //(150, 150)
			turnGunRight(360);
			turnRight(90);
			ahead(120); //(150, 30)
			turnGunRight(360);
			turnRight(116.565);
			ahead(134.164); //(30, 90)
			turnGunRight(360);
			turnRight(171.87);
			ahead(189.737); //(210, 30)	
			turnGunRight(360);
			turnRight(161.565);
			ahead(180);
			turnRight(90);
			
		}
		
	}
	
	//Called to get to the top left corner
	public void goCorner() {
		// We don't want to stop when we're just turning...using the variable we initialized at the top
		stopWhenSeeRobot = false;
		
		// turn to face the wall to the "right" of our desired corner.
		turnRight(normalRelativeAngleDegrees(corner - getHeading()));
		stopWhenSeeRobot = true;
		// Move to that wall
		ahead(moveAmount); //Reuse the max move amount from earlier
		// Turn to face the corner
		turnLeft(90);
		// Move to the corner
		ahead(moveAmount);
		
		// Turn gun to starting point, and also the bot oriented in the right direction to get started on our corner algorithm
		turnGunRight(180);
		turnRight(180);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */



public void onHitWall(HitWallEvent e){
if(getOthers() < 2){ //Only call this once we get down to 1v1
back(30);}
}

	public void onScannedRobot(ScannedRobotEvent event) {
	if(getOthers() > 2) //start out with a head on targeting shot, as that works best in melee. I found that by switching to linear targeting after it got down to two opponents resulted in the best results for me
	{
		fire(2);
		if (peek) {
			scan();
		}
	}else{ // Precise linear targeting algorithm that I got from a tutorial walkthrough, and adapted to work with the regular robot class.
    // ... Radar code ..
    final double FIREPOWER = 2;
    final double ROBOT_WIDTH = 16,ROBOT_HEIGHT = 16;
    // Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to robot
    final double eAbsBearing = getHeadingRadians() + event.getBearingRadians();
    final double rX = getX(), rY = getY(),
        bV = Rules.getBulletSpeed(FIREPOWER);
    final double eX = rX + event.getDistance()*Math.sin(eAbsBearing),
        eY = rY + event.getDistance()*Math.cos(eAbsBearing),
        eV = event.getVelocity(),
        eHd = event.getHeadingRadians();
    // These constants make calculating the quadratic coefficients below easier
    final double A = (eX - rX)/bV;
    final double B = eV/bV*Math.sin(eHd);
    final double C = (eY - rY)/bV;
    final double D = eV/bV*Math.cos(eHd);
    // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
    final double a = A*A + C*C;
    final double b = 2*(A*B + C*D);
    final double c = (B*B + D*D - 1);
    final double discrim = b*b - 4*a*c;
    if (discrim >= 0) {
        // Reciprocal of quadratic formula
        final double t1 = 2*a/(-b - Math.sqrt(discrim));
        final double t2 = 2*a/(-b + Math.sqrt(discrim));
        final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
        // Assume enemy stops at walls
        final double endX = limit(
            eX + eV*t*Math.sin(eHd),
            ROBOT_WIDTH/2, getBattleFieldWidth() - ROBOT_WIDTH/2);
        final double endY = limit(
            eY + eV*t*Math.cos(eHd),
            ROBOT_HEIGHT/2, getBattleFieldHeight() - ROBOT_HEIGHT/2);
        turnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
            Math.atan2(endX - rX, endY - rY)
            - getGunHeadingRadians()));
        fire(FIREPOWER);
    }}
}

//Function that is called in the linear targeting algorithm 
private double limit(double value, double min, double max) {
    return Math.min(max, Math.max(min, value));
}

/**
 * onHitRobot:  Move away a bit.
 */
public void onHitRobot(HitRobotEvent e) {
	back(20);
	ahead(20);
}

//Moves in a square after it wins
public void onWin()
{
	ahead(100); 
	turnRight(90); 
	ahead(100); 	
	turnRight(90); 
	ahead(100); 
	turnRight(90); 
	ahead(100); 
	turnRight(90);  
}

}


