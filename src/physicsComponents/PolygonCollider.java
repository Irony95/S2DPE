package physicsComponents;

import org.apache.commons.math4.legacy.linear.RealVector;

public interface PolygonCollider {
	public RealVector[] getPoints();
	
	public double getInvInertia();
	public void addAngularVelocity(double updated);
	public double getAngularVelocity();
	public RealVector getSupport(RealVector dir);
	public double[] findAxisOfLeastPenetration(EntityUnit object, PolygonCollider collider);
	
	public void vsCircle(EntityUnit object, CircleCollider collider, double dt);
	public void vsCanvas(double width, double height, double dt);
	public void vsPolygon(EntityUnit object, PolygonCollider collider, double dt);
	
	public void applyRotationalAirResistance();
}
