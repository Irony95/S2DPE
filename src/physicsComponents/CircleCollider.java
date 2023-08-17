package physicsComponents;

public interface CircleCollider {
	public double getRadius();
	public void vsCircle(EntityUnit object, CircleCollider collider, double dt);
	public void vsCanvas(double width, double height, double dt);
	public void vsPolygon(EntityUnit object, PolygonCollider collider, double dt);
}
