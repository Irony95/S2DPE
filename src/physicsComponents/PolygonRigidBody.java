package physicsComponents;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import org.apache.commons.math4.legacy.linear.RealVector;

import application.EngineCanvas;
import application.EngineProperties;
import application.SceneObjects;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.MatrixUtils;
import org.apache.commons.math4.legacy.linear.RealMatrix;

public class PolygonRigidBody extends EntityUnit implements Drawable, PolygonCollider {
	private static final long serialVersionUID = -7042455836093065583L;

	public static class Builder {
		
		ArrayList<RealVector> points = new ArrayList<RealVector>();
		Color color;	
		double vx = 0, vy = 0;
		double rot = 0, av = 0;
		public static Builder newInstance() {
			return new Builder();
		}
		
		
		public Builder addPoint(double x, double y) {
			points.add(new ArrayRealVector(new double[] {x, y}));
			return this;
		}
		
		public Builder setColor(Color c) {
			this.color = c;
			return this;
		}
		public Builder setVelocity(double x, double y) {
			this.vx = x;
			this.vy = y;
			return this;
		}
		public Builder setRotations(double velocity, double rotation) {
			this.rot = rotation;
			this.av = velocity;
			return this;
		}
		
		public PolygonRigidBody build(double x, double y, Material mat) {
			this.points.add(0, new ArrayRealVector(new double[] {0, 0}));
			return new PolygonRigidBody(x, y, mat, this);
		}
	}
	
	RealVector[] points;
	RealVector[] translatedPoints;
	private ArrayList<ParticleEntity> functionalPoints = new ArrayList<ParticleEntity>();
	private ArrayList<Integer> functionalPointsIndex = new ArrayList<Integer>();

	private PolygonRigidBody(double x, double y, Material mat, Builder builder) {
		super(x, y, builder.vx, builder.vy, mat); 	
		this.orientation = builder.rot;
		this.angularVelocity = builder.av;
		
		points = new RealVector[builder.points.size()];
		points = builder.points.toArray(points);
		translatedPoints = new RealVector[points.length];
	
		double avgX = 0;
		double avgY = 0;
		for (int i = 0;i < points.length;i ++) {
			avgX += points[i].getEntry(0);
			avgY += points[i].getEntry(1);
		}
		avgX /= points.length;
		avgY /= points.length;
		for (int i = 0;i < points.length;i ++) {
			points[i].addToEntry(0, -avgX);
			points[i].addToEntry(1, -avgY);
		}
		color = builder.color;
		updateTranslatedPoints();
		
		if (material.customMass >= 0) {
			this.mass = material.customMass;
			this.invMass = material.customMass == 0 ? 0 : 1.0/material.customMass;
		}
		else {
			this.mass = 0;
			this.inertia = 0;
			double area = 0;
			double k_inv3 = 1f/3;
			for (int i = 0;i < points.length;i++) {
				RealVector p1 = points[i];
				RealVector p2 = points[(i + 1) % points.length];
				
				double D = cross(p1, p2);
				double triangleArea = 0.5f * D;
				area += triangleArea;
				
				double intx2 = p1.getEntry(0) * p1.getEntry(0) + p2.getEntry(0) * p1.getEntry(0) + p2.getEntry(0) * p2.getEntry(0);
				double inty2 = p1.getEntry(1) * p1.getEntry(1) + p2.getEntry(1) * p1.getEntry(1) + p2.getEntry(1) * p2.getEntry(1);
				inertia += (0.25f * k_inv3 * D) * (intx2 + inty2);
			}
			this.mass = area * material.density;
			this.invMass = 1.0/this.mass;
			this.inertia *= material.density;
			this.invInertia = 1.0/this.inertia;
			
			if (material.customInertia != -1) {
				this.inertia = material.customInertia;
				this.invInertia = 1.0/this.inertia;
			}
			
		}
	}

	@Override
	public void vsCircle(EntityUnit object, CircleCollider collider) {
		
		double bestSeperation = Double.NEGATIVE_INFINITY;
		int bestIndex = -1;
		RealVector bestNormal = null;
		
		for (int i =0;i < translatedPoints.length; i++) {
			RealVector point = translatedPoints[i].add(position);
			RealVector faceVector = i == translatedPoints.length-1 ?
					translatedPoints[0].subtract(translatedPoints[i]) : 
				translatedPoints[i+1].subtract(translatedPoints[i]);
			RealVector normal = new ArrayRealVector(new double[] {faceVector.getEntry(1), -faceVector.getEntry(0)}).unitVector();
			if (normal.dotProduct(translatedPoints[i]) < 0){ normal = normal.mapMultiply(-1); }
			
			double seperation = normal.dotProduct(object.position.subtract(point));
			if (seperation > collider.getRadius()) { return; }
			if (seperation > bestSeperation) {
				bestIndex = i;
				bestSeperation = seperation;
				bestNormal = normal;
			}
		}
		if (bestIndex == -1) { return; }
		RealVector[] refPoints = new RealVector[2];
		refPoints[0] = translatedPoints[bestIndex].add(position);
		int refIndex = bestIndex == translatedPoints.length-1 ? 0 : bestIndex+1;
		refPoints[1] = translatedPoints[refIndex].add(position);

		double penetration = 0;
		RealVector contactPoint = null;
		RealVector contactNormal = null;
		if (bestSeperation < 0) {
			penetration = collider.getRadius();
			contactPoint = bestNormal.mapMultiply(collider.getRadius()).add(object.position);
			contactNormal = bestNormal.mapMultiply(-1);
		}
		else {
			penetration = collider.getRadius() - bestSeperation;
			double dot1 = (object.position.subtract(refPoints[0])).dotProduct(refPoints[1].subtract(refPoints[0]));
			double dot2 = (object.position.subtract(refPoints[1])).dotProduct(refPoints[0].subtract(refPoints[1]));
			
			if (dot1 <= 0.0) {
				double distSq = Math.pow(object.position.getEntry(0) - refPoints[0].getEntry(0), 2)
						+ Math.pow(object.position.getEntry(1) - refPoints[0].getEntry(1), 2);
				if (distSq > Math.pow(collider.getRadius(), 2)) { return; }
				contactNormal = object.position.subtract(refPoints[0]).unitVector();
				contactPoint = refPoints[0];
				
			}
			else if (dot2 <= 0) {
				double distSq = Math.pow(object.position.getEntry(0) - refPoints[1].getEntry(0), 2)
						+ Math.pow(object.position.getEntry(1) - refPoints[1].getEntry(1), 2);
				if (distSq > Math.pow(collider.getRadius(), 2)) { return; }
				contactNormal = object.position.subtract(refPoints[1]).unitVector();
				contactPoint = refPoints[1];
			}
			else {
				contactNormal = bestNormal;
				if (object.position.subtract(refPoints[0]).dotProduct(contactNormal) > collider.getRadius()) { return; }
				contactPoint = contactNormal.mapMultiply(collider.getRadius()).add(object.position);
			}
		}
		RealVector rA = contactPoint.subtract(position);
		RealVector[] impulses = ImpulseSolver.solveImpulse(this, object, contactNormal, this,  rA);

		if (impulses == null || impulses[0] == null) { return; }
		velocity = velocity.subtract(impulses[0].mapMultiply(invMass));
		addAngularVelocity(-getInvInertia() * cross(rA, impulses[0]));
		object.velocity = object.velocity.add(impulses[0].mapMultiply(object.invMass));
		
		if (EngineProperties.applyFriction && impulses[1] != null) {				

			velocity = velocity.subtract(impulses[1].mapMultiply(invMass));
			addAngularVelocity(-getInvInertia() * cross(rA, impulses[1]));

			object.velocity = object.velocity.add(impulses[1].mapMultiply(object.invMass));
		}
		
		//positional correction
		ImpulseSolver.positionalCorrection(this, object, penetration, contactNormal);
	}

	@Override
	public void vsCanvas(double width, double height) {
		RealVector contactPoint = new ArrayRealVector(new double[] {0,0});
		int contactCount = 0;
		RealVector normal = null;
		//On the x axis
		for (int i = 0;i < translatedPoints.length;i++) {
			RealVector point = translatedPoints[i].add(position);

			if (point.getEntry(0) < 0) {
				normal = new ArrayRealVector(new double[] {1, 0});
				contactPoint = contactPoint.add(point);
				contactCount++;
				
				double penetrationDepth = Math.abs(point.getEntry(0)) ;
				position.addToEntry(0, penetrationDepth * 0.5);
			}
			else if (point.getEntry(0) > width) {
				normal = new ArrayRealVector(new double[] {-1, 0});
				contactPoint = contactPoint.add(point);
				contactCount++;
				
				double penetrationDepth = -Math.abs(point.getEntry(0)-width);
				position.addToEntry(0, penetrationDepth * 0.5);
			}
		}
		if (contactCount > 0) {
			contactPoint = contactPoint.mapDivide(contactCount);
			
			RealVector rA = contactPoint.subtract(position);
			RealVector[] impulses = ImpulseSolver.solveImpulse(this, this, normal, rA);
			
			if (impulses != null && impulses[0] != null) { 			
				velocity = velocity.subtract(impulses[0].mapMultiply(invMass));
				addAngularVelocity(-invInertia * cross(rA, impulses[0]));
				
				if (EngineProperties.applyFriction && impulses[1] != null) {	
					velocity = velocity.add(impulses[1].mapMultiply(invMass));
					addAngularVelocity(invInertia * cross(rA, impulses[1]));
				}
			}
		}
		//on the Y axis
		for (int i = 0;i < translatedPoints.length;i++) {
			RealVector point = translatedPoints[i].add(position);
			if (point.getEntry(1) < 0) {
				normal = new ArrayRealVector(new double[] {0, 1});
				contactPoint = contactPoint.add(point);
				contactCount++;
				
				double penetrationDepth = Math.abs(point.getEntry(1)) ;
				position.addToEntry(1, penetrationDepth * 0.5);
			}
			else if (point.getEntry(1) > height) {
				normal = new ArrayRealVector(new double[] {0, -1});
				contactPoint = contactPoint.add(point);
				contactCount++;
				
				double penetrationDepth = -Math.abs(point.getEntry(1)-height);
				position.addToEntry(1, penetrationDepth * 0.5);
			}
		}
		if (contactCount > 0) {
			contactPoint = contactPoint.mapDivide(contactCount);
			
			RealVector rA = contactPoint.subtract(position);
			RealVector[] impulses = ImpulseSolver.solveImpulse(this, this, normal, rA);
			
			if (impulses != null && impulses[0] != null) { 
				velocity = velocity.subtract(impulses[0].mapMultiply(invMass));
				addAngularVelocity(-invInertia * cross(rA, impulses[0]));
				
				if (EngineProperties.applyFriction && impulses[1] != null) {
					velocity = velocity.add(impulses[1].mapMultiply(invMass));
					addAngularVelocity(invInertia * cross(rA, impulses[1]));
				}
			}
		}
	}
	
	@Override
	public void vsPolygon(EntityUnit object, PolygonCollider collider) {
		double[] penetration = findAxisOfLeastPenetration(object, collider);
		if (penetration[1] >= 0) { return; }
		double[] colliderPenetration = collider.findAxisOfLeastPenetration(this, this);
		if (colliderPenetration[1] >= 0) { return; }
		
		boolean flip = false;
		RealVector[] refPoints = new RealVector[2];
		EntityUnit referenceUnit = null;
		PolygonCollider referenceCollider = null;
		RealVector[] incPoints = new RealVector[2];
		EntityUnit incidentUnit = null;
		PolygonCollider incidentCollider = null;
		//if greater, this object is reference
		if (penetration[1] > colliderPenetration[1]) {
			referenceUnit = this;
			referenceCollider = this;
			incidentUnit = object;
			incidentCollider = collider;
			refPoints[1] = translatedPoints[(int) penetration[0]].add(position);
			int index = (int) (penetration[0] == translatedPoints.length-1 ? 0 : penetration[0] + 1);
			refPoints[0] = translatedPoints[index].add(position);
			
			incPoints[0] = collider.getPoints()[(int) colliderPenetration[0]];
			index = (int) (colliderPenetration[0] == collider.getPoints().length-1 ? 0 : colliderPenetration[0] + 1);
			incPoints[1] = collider.getPoints()[index];
			
			flip = true;
			//System.out.println("reference color is " + (color.toString()));
		}
		else {
			referenceUnit = object;
			referenceCollider = collider;
			incidentUnit = this;
			incidentCollider = this;
			refPoints[0] = collider.getPoints()[(int) colliderPenetration[0]];
			int index = (int) (colliderPenetration[0] == collider.getPoints().length-1 ? 0 : colliderPenetration[0] + 1);
			refPoints[1] = collider.getPoints()[index];
			
			incPoints[0] = translatedPoints[(int) penetration[0]].add(position);
			index = (int) (penetration[0] == translatedPoints.length-1 ? 0 : penetration[0] + 1);
			incPoints[1] = translatedPoints[index].add(position);
			
			//System.out.println("reference color is " + ((PolygonRigidBody)object).color.toString());

		}
		RealVector refVector = refPoints[1].subtract(refPoints[0]).unitVector();
		
		double offset = refVector.dotProduct(refPoints[0]);
		incPoints = ImpulseSolver.clipPoints(incPoints[0], incPoints[1], refVector, offset);
		if (incPoints[0] == null || incPoints[1] == null) { return; }

		offset = refVector.dotProduct(refPoints[1]);
		incPoints = ImpulseSolver.clipPoints(incPoints[0], incPoints[1], refVector.mapMultiply(-1), -offset);
		if (incPoints[0] == null || incPoints[1] == null) { return; }
		
		RealVector refNormal = new ArrayRealVector(new double[] {refVector.getEntry(1), -refVector.getEntry(0)});
		if (flip) { refNormal = refNormal.mapMultiply(-1); }
		//System.out.println(refNormal.toString());
		
		double penetrationDistance = 0;
		RealVector contactPoint = new ArrayRealVector(new double[] {0, 0});
		int contactCount = 0;
		double seperation = refNormal.dotProduct(incPoints[0]) - refNormal.dotProduct(refPoints[0]);
		if (seperation <= 0) {
			contactPoint = contactPoint.add(incPoints[0]);
			contactCount++;
			penetrationDistance += -seperation;
		}
		seperation = refNormal.dotProduct(incPoints[1]) - refNormal.dotProduct(refPoints[0]);
		if (seperation <= 0) {
			contactPoint = contactPoint.add(incPoints[1]);
			contactCount++;
			penetrationDistance += -seperation;
		}
		if (contactCount == 0) { return; }
		contactPoint = contactPoint.mapDivide(contactCount);
		penetrationDistance /= contactCount;
		
		RealVector rA = contactPoint.subtract(referenceUnit.position);
		RealVector rB = contactPoint.subtract(incidentUnit.position);
		
		RealVector[] impulses = ImpulseSolver.solveImpulse(referenceUnit, incidentUnit, refNormal,
				referenceCollider, incidentCollider,
				rA, rB);
		
		if (impulses == null || impulses[0] == null) { return; }
		referenceUnit.velocity = referenceUnit.velocity.subtract(impulses[0].mapMultiply(referenceUnit.invMass));
		referenceCollider.addAngularVelocity(-referenceCollider.getInvInertia() * cross(rA, impulses[0]));
		incidentUnit.velocity = incidentUnit.velocity.add(impulses[0].mapMultiply(incidentUnit.invMass));
		incidentCollider.addAngularVelocity(incidentCollider.getInvInertia() * cross(rB, impulses[0]));
		
		if (EngineProperties.applyFriction && impulses[1] != null) {					
			referenceUnit.velocity = referenceUnit.velocity.subtract(impulses[1].mapMultiply(referenceUnit.invMass));
			referenceCollider.addAngularVelocity(-referenceCollider.getInvInertia() * cross(rA, impulses[1]));

			incidentUnit.velocity = incidentUnit.velocity.add(impulses[1].mapMultiply(incidentUnit.invMass));
			incidentCollider.addAngularVelocity(incidentCollider.getInvInertia() * cross(rB, impulses[1]));
		}
		
		ImpulseSolver.positionalCorrection(referenceUnit, incidentUnit, penetrationDistance, refNormal);
	}
	
	@Override
	public double[] findAxisOfLeastPenetration(EntityUnit object, PolygonCollider collider) {
		double bestPenetration = Double.NEGATIVE_INFINITY;
		int bestIndex = -1;
		
		for (int i =0;i < translatedPoints.length; i++) {
			RealVector faceVector = i == translatedPoints.length-1 ?
					translatedPoints[0].subtract(translatedPoints[i]) : 
				translatedPoints[i+1].subtract(translatedPoints[i]);
			RealVector normal = new ArrayRealVector(new double[] {faceVector.getEntry(1), -faceVector.getEntry(0)}).unitVector();
			if (normal.dotProduct(translatedPoints[i]) < 0)
			{				
				normal = normal.mapMultiply(-1);
			}
			RealVector supportVector = collider.getSupport(normal.mapMultiply(-1));
			RealVector point = translatedPoints[i].add(position);
			double pointPenetration = normal.dotProduct(supportVector.subtract(point));
			if (pointPenetration > bestPenetration) {
				bestIndex = i;
				bestPenetration = pointPenetration;
			}
		}
		return new double[] {bestIndex, bestPenetration};
	}
	
	@Override
	public RealVector getSupport(RealVector dir) {
		double bestProjection = Double.NEGATIVE_INFINITY;
		RealVector bestVertex =  null;
		for (int i = 0;i < translatedPoints.length;i++) {
			RealVector point = translatedPoints[i].add(position);
			double projection = point.dotProduct(dir);
			if (projection > bestProjection) {
				bestVertex = point;
				bestProjection = projection;
			}
		}
		return bestVertex;
	}
	
	public double cross(RealVector a, RealVector b) {
		return a.getEntry(0) * b.getEntry(1) - a.getEntry(1) * b.getEntry(0);
	}
	
	public RealVector cross(RealVector a, double b) {
		return new ArrayRealVector(new double[] {
				a.getEntry(1) * -b,
				a.getEntry(0) * b
		});
	}
	
	@Override
	public void applyRotationalAirResistance() {
		if (angularVelocity == 0) { return; } 
		double j = (1.0/2) * EngineProperties.airDensity
				* Math.pow(angularVelocity, 2);
		addAngularVelocity(j * -Math.signum(angularVelocity));
	}

	@Override
	public void draw(GraphicsContext gc) {
		double[] xPoints = new double[points.length];
		double[] yPoints = new double[points.length];
		for (int i = 0; i < points.length;i++) {
			xPoints[i] = translatedPoints[i].getEntry(0) + position.getEntry(0);
			yPoints[i] = translatedPoints[i].getEntry(1) + position.getEntry(1);
		}
		gc.setFill(this.color);
		gc.fillPolygon(xPoints, yPoints, points.length);
		
	}
	
	private void updateTranslatedPoints() {
		double c = Math.cos(orientation);
		double s = Math.sin(orientation);
		double[][] matrixData = { {c,-s}, {s, c}};
		RealMatrix rot = MatrixUtils.createRealMatrix(matrixData);
		
		for (int i = 0;i < translatedPoints.length; i++) {
			translatedPoints[i] = rot.operate(points[i]);
		}
		
		for (int i = 0;i < functionalPoints.size();i++) {
			functionalPoints.get(0).position = translatedPoints[functionalPointsIndex.get(i)].add(position);
		}
	}
	
	@Override
	public void update(double deltaTime) {
		super.update(deltaTime);
		for (int i = 0;i < functionalPoints.size();i++) {
			RealVector radius = translatedPoints[functionalPointsIndex.get(i)];
			// torque = radius X force
			double addedTorque = cross(radius, functionalPoints.get(i).accumulatedForce);
			torque += addedTorque;
			functionalPoints.get(i).resetForce();
		}
		//solve for angular rotations
		angularVelocity += ODESolver.RK4(angularVelocity, 0, deltaTime, (y, t) -> (torque * invInertia));
		orientation += ODESolver.RK4(orientation, 0, deltaTime, (y, t) -> (angularVelocity));
		if (orientation > 2*Math.PI) {orientation -= 2*Math.PI; }
		if (orientation < 0) {orientation += 2*Math.PI; }
		
		torque = 0;
		updateTranslatedPoints();
	}

	@Override
	public void drawWireframe(GraphicsContext gc, int index) {
		double[] xPoints = new double[points.length+1];
		double[] yPoints = new double[points.length+1];
		for (int i = 0; i < points.length;i++) {
			xPoints[i] = translatedPoints[i].getEntry(0) + position.getEntry(0);
			yPoints[i] = translatedPoints[i].getEntry(1) + position.getEntry(1);
		}
		xPoints[points.length] = translatedPoints[0].getEntry(0) + position.getEntry(0);
		yPoints[points.length] = translatedPoints[0].getEntry(1) + position.getEntry(1);
		
		if (velocity.getL1Norm() > 10) {
			EngineCanvas.DrawUtils.drawArrow(gc, position, velocity.unitVector(), 30);			
		}
		gc.setFill(Color.WHITE);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText(SceneObjects.getInstance().getEntityName(index), position.getEntry(0), position.getEntry(1));
		
		
		gc.setStroke(this.color);
		gc.setLineWidth(2);
		gc.strokePolyline(xPoints, yPoints, xPoints.length);
		
		for (int i =0; i < functionalPoints.size();i++) {
			functionalPoints.get(i).draw(gc);
		}
	}
	
	public ParticleEntity getPointAsEntity(int i) {
		Material fakeMat = new Material("", 0, 0, 0, 0, 0);
		RealVector location = translatedPoints[i].add(position);
		ParticleEntity point = new ParticleEntity(location.getEntry(0), location.getEntry(1), 0, 0, fakeMat, Color.FORESTGREEN);
		functionalPoints.add(point);
		functionalPointsIndex.add(i);
		return point;
	}

	@Override
	public RealVector[] getPoints() {
		RealVector[] absPoints = new RealVector[translatedPoints.length];
		for (int i =0; i < translatedPoints.length;i++) {
			absPoints[i] = translatedPoints[i].add(position);
		}
		return absPoints;
	}

	@Override
	public double getInvInertia() {
		return invInertia;
	}

	@Override
	public void addAngularVelocity(double updated) {
		angularVelocity += updated;
	}
	
	@Override
	public double getAngularVelocity() { return angularVelocity; }
}