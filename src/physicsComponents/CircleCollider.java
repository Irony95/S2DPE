package physicsComponents;

public interface CircleCollider {
	public double getRadius();
	public void vsCircle(EntityUnit object, CircleCollider collider);
	public void vsCanvas(double width, double height);
	public void vsPolygon(EntityUnit object, PolygonCollider collider);
}
