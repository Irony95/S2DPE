package physicsComponents;

import org.apache.commons.math4.legacy.linear.RealVector;

import application.EngineProperties;
import javafx.scene.paint.Color;

import java.io.Serializable;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;

public abstract class EntityUnit implements Serializable {
	private static final long serialVersionUID = -1485555859996298506L;
	public RealVector position;
	public RealVector velocity;
	public double mass;
	public double invMass;
	protected RealVector accumulatedForce;
	Material material;
	public transient Color color;
	
	public double inertia = 100;
	public double invInertia = 0;
	public double orientation = 0;
	public double angularVelocity = 0;
	public double torque = 0;
	
	protected EntityUnit(double x, double y, double vx, double vy, Material mat) {
		position = new ArrayRealVector(new double[] {x, y});
		velocity = new ArrayRealVector(new double[] {vx, vy});
		accumulatedForce = new ArrayRealVector(new double[] {0, 0});
		this.material = mat;
		//mass should be updated based on the inheriting object
		this.mass = 1;
		this.invMass = 1;
		
	}
	
	public void update(double deltaTime) {		
		
		RealVector acceleration = accumulatedForce.mapMultiply(invMass);
		double deltaVX = ODESolver.RK4(velocity.getEntry(0), 0,
				deltaTime,(y, t) -> (acceleration.getEntry(0)));
		velocity.addToEntry(0, deltaVX); 
		double deltaVY = ODESolver.RK4(velocity.getEntry(1), 0,
				deltaTime,(y, t) -> (acceleration.getEntry(1)));
		velocity.addToEntry(1, deltaVY);
		
		double deltaX = ODESolver.RK4(position.getEntry(0), 0,
				deltaTime,(y, t) -> (velocity.getEntry(0)));
		position.addToEntry(0, deltaX); 
		double deltaY = ODESolver.RK4(position.getEntry(1), 0,
				deltaTime,(y, t) -> (velocity.getEntry(1)));
		position.addToEntry(1, deltaY); 
		resetForce();
	}
	
	public void addForce(RealVector addedForce) {	
		accumulatedForce = accumulatedForce.add(addedForce);
	}
	
	public void applyAirResistance() {
		//no movement, no unit vector aka no air resistance
		if (velocity.getL1Norm() == 0) { return; } 
		RealVector dragNormal = velocity.mapMultiply(-1).unitVector();
		double j = (1.0/2) * EngineProperties.airDensity
				* Math.pow(velocity.getL1Norm(), 2);
		addForce(dragNormal.mapMultiply(j));
	}
	
	public void addForce(double fx, double fy) {
		accumulatedForce.addToEntry(0, fx);
		accumulatedForce.addToEntry(1, fy);
	}
	
	public void resetForce() {
		accumulatedForce.set(0);
	}
}
