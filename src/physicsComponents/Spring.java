package physicsComponents;

import org.apache.commons.math4.legacy.linear.RealVector;

import java.io.Serializable;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;

import application.EngineProperties;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Spring extends ForceActor implements Drawable {
	private static final long serialVersionUID = -5810507710185418055L;
	EntityUnit objectA;
	EntityUnit objectB;
	
	private double k;
	private double dampingCoeff;
	private double restLength;
	private int coils;
	public Spring(EntityUnit objectA, EntityUnit objectB, double k, double restLength, double dampingCoeff) {
		this.objectA = objectA;
		this.objectB = objectB;
		if (restLength < 0) { this.restLength = 0; }
		else { this.restLength = restLength; }
		
		if (k < 0) { this.k = 0; }
		else if (k > EngineProperties.maxSpringConstant) {
			this.k = EngineProperties.maxSpringConstant;
		}
		else { this.k = k; }
		
		if (dampingCoeff < 0) { this.dampingCoeff = 0; }
		else if (dampingCoeff > EngineProperties.maxDampingConstant) {
			this.dampingCoeff = EngineProperties.maxDampingConstant;
		}
		else { this.dampingCoeff = dampingCoeff; }
		
		if (restLength == 0) {
			coils = (int) (EngineProperties.kToCoils * 10);
		}
		else {			
			coils = (int) (EngineProperties.kToCoils * Math.log(k * restLength));
		}
	}
	
	@Override
	public void applyForce() {
		double displacement = restLength - objectA.position.getDistance(objectB.position);
		RealVector normal = objectA.position.subtract(objectB.position).unitVector();
		
		objectA.addForce(normal.mapMultiply(displacement * k));
		//points will have inverse Normals
		objectB.addForce(normal.mapMultiply(-displacement * k));
		//RealVector absNormal = new ArrayRealVector(new double[] {Math.abs(normal.getEntry(0)),Math.abs(normal.getEntry(1))});
		if (EngineProperties.applyFriction) {
			objectA.addForce(objectA.velocity.mapMultiply(-dampingCoeff));
			objectB.addForce(objectB.velocity.mapMultiply(-dampingCoeff));
		}
	}
	
	
	@Override
	public void draw(GraphicsContext gc) {
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(3);
		if (k == EngineProperties.maxSpringConstant) {
			gc.strokeLine(objectA.position.getEntry(0), objectA.position.getEntry(1),
					objectB.position.getEntry(0), objectB.position.getEntry(1));
		}
		else {
			RealVector normal = objectB.position.subtract(objectA.position).unitVector();
			//Perpendicular value
			RealVector offset = new ArrayRealVector(
					new double[] {-normal.getEntry(1), normal.getEntry(0)});
			
			//magnitude distance from the normal
			offset = offset.mapMultiply(3);
			double stepDist = objectA.position.getDistance(objectB.position)/coils;
			//including the starting and ending location
			double xPoints[] = new double[coils+2];
			double yPoints[] = new double[coils+2];
			RealVector stepPosition = objectA.position.copy();
			
			//starting location
			xPoints[0] = stepPosition.getEntry(0);
			yPoints[0] = stepPosition.getEntry(1);
			for (int i = 0;i < coils;i++) {
				offset = offset.mapMultiplyToSelf(-1);
				stepPosition = stepPosition.add(normal.mapMultiply(stepDist));
				xPoints[i+1] = stepPosition.getEntry(0) + offset.getEntry(0);
				yPoints[i+1] = stepPosition.getEntry(1) + offset.getEntry(1);
			}
			//ending location
			xPoints[xPoints.length-1] = objectB.position.getEntry(0);
			yPoints[yPoints.length-1] = objectB.position.getEntry(1);
			gc.strokePolyline(xPoints, yPoints, coils);
			
		}
	}
	
	@Override
	public void drawWireframe(GraphicsContext gc, int index) {
		double change = objectA.position.getDistance(objectB.position)/(restLength*2);
		Color strokeColor = Color.LIME;
		strokeColor = strokeColor.interpolate(Color.RED, change);
		gc.setStroke(strokeColor);
		gc.setLineWidth(4);
		gc.strokeLine(objectA.position.getEntry(0), objectA.position.getEntry(1),
				objectB.position.getEntry(0), objectB.position.getEntry(1));
	}
}
