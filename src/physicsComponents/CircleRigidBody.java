package physicsComponents;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import Solvers.ImpulseSolver;
import application.EngineWorld;
import application.EngineProperties;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import world.SceneObjects;

public class CircleRigidBody extends EntityUnit implements Drawable, CircleCollider {
	
	private static final long serialVersionUID = -617798588778560711L;
	public double radius;

	public CircleRigidBody(double x, double y, double vx, double vy, Material mat,
			double radius, Color color) {
		super(x, y, vx, vy, mat);
		this.radius = radius;
		this.color = color;

		if (material.customMass >= 0 ) {
			this.mass = material.customMass;
			this.invMass = material.customMass == 0 ? 0 : 1.0/material.customMass;
		}
		else {
			this.mass = (Math.PI * radius * radius) * material.density;
			this.invMass = 1.0/mass;
		}
	}

	@Override
	public void vsCanvas(double width, double height, double dt) {
		//on the X axis
		if (position.getEntry(0) + radius > width || position.getEntry(0) - radius < 0) {
			RealVector normal = new ArrayRealVector(new double[] {0, 0});
			if (position.getEntry(0) + radius > width) { normal.setEntry(0, -1); }
			else { normal.setEntry(0, 1); }
			RealVector[] impulses = ImpulseSolver.solveImpulse(this, null, normal, null);
			if (impulses != null && impulses[0] != null) {
				addForce(impulses[0].mapDivide(dt).mapMultiply(-1));
				if (EngineProperties.applyFriction && impulses[1] != null) {
					addForce(impulses[1].mapDivide(dt));
				}
			}
			double penetrationDepth = position.getEntry(0) + radius > width ? 
			position.getEntry(0) + radius - width : position.getEntry(0) - radius;
			position.addToEntry(0, -penetrationDepth * 0.5);
		}
		//on the Y axis
		if (position.getEntry(1) + radius > height || position.getEntry(1) - radius < 0) {
			RealVector normal = new ArrayRealVector(new double[] {0, 0});
			if (position.getEntry(1) + radius > height) { normal.setEntry(1, -1); }
			else { normal.setEntry(1, 1); }
			RealVector[] impulses = ImpulseSolver.solveImpulse(this, null, normal, null);
			if (impulses != null && impulses[0] != null) {
				addForce(impulses[0].mapDivide(dt).mapMultiply(-1));
				if (EngineProperties.applyFriction && impulses[1] != null) {
					addForce(impulses[1].mapDivide(dt));
				}
			}
			double penetrationDepth = position.getEntry(1) + radius > height ? 
			position.getEntry(1) + radius - height : position.getEntry(1) - radius;
			position.addToEntry(1, -penetrationDepth * 0.5);
		}
	}

	@Override
	public void vsCircle(EntityUnit object, CircleCollider collider, double dt) {
		double objRadius = collider.getRadius();
		double squaredDist = Math.pow(object.position.getEntry(0) - position.getEntry(0), 2) + 
				Math.pow(object.position.getEntry(1) - position.getEntry(1), 2);

		if (squaredDist != 0 && squaredDist <= Math.pow(radius + objRadius, 2)) {
			RealVector normal = (object.position.subtract(position)).unitVector();
			RealVector[] impulses = ImpulseSolver.solveImpulse(this, object, normal);
			
			if (impulses == null || impulses[0] == null) { return; }
			addForce(impulses[0].mapDivide(dt).mapMultiply(-1));
			object.addForce(impulses[0].mapDivide(dt));
			
			//positional correction
			double penetrationDepth = (radius + objRadius) - position.getDistance(object.position);
			ImpulseSolver.positionalCorrection(this, object, penetrationDepth, normal);
			
			if (EngineProperties.applyFriction && impulses[1] != null) {				
				addForce(impulses[1].mapDivide(dt).mapMultiply(-1));
				object.addForce(impulses[1].mapDivide(dt));
			}
		}
	}

	@Override
	public void draw(GraphicsContext gc) {
		gc.setFill(color);
		gc.fillOval(position.getEntry(0) - radius, position.getEntry(1) - radius, radius*2, radius*2);
		
	}

	@Override
	public void drawWireframe(GraphicsContext gc, int index) {
		gc.setTextAlign(TextAlignment.CENTER);
		if (velocity.getL1Norm() > 20) {
			EngineWorld.DrawUtils.drawArrow(gc, position, velocity.unitVector(), 30);
			EngineWorld.DrawUtils.writeVelocity(gc, position, velocity);			
		}
		gc.setFill(Color.WHITE);
		gc.fillText(SceneObjects.getInstance().getEntityName(index), position.getEntry(0), position.getEntry(1));
		
		gc.setStroke(this.color);
		gc.setLineWidth(2);
		gc.strokeOval(position.getEntry(0) - radius, position.getEntry(1) - radius, radius*2, radius*2);
	}

	@Override
	public double getRadius() {
		return this.radius;
	}

	@Override
	public void vsPolygon(EntityUnit object, PolygonCollider collider, double dt) {
		collider.vsCircle(this, this, dt);
	}
}
