package application;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import physicsComponents.*;

import java.util.ArrayList;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ObjectInputStream;

public class EngineMasterController {
	private SceneObjects sceneObjects = SceneObjects.getInstance();
	private EngineCanvas canvas;
	AnimationTimer timer;
	private long previousTime = 0;
	
	@FXML
    private ListView<String> lvEntities;

    @FXML
    private ListView<String> lvForces;
	
	@FXML
    private Button btnPausePlay;

    @FXML
    private Button btnStep;

    @FXML
    private TextField tfTimeStepSize;

    @FXML
    private TextField tfAirDensity;
    
    @FXML
    private TextField tfSaveSceneName;
    
    @FXML
    private CheckBox cbApplyFriction;
    
    @FXML
    private ComboBox<String> cobMaterial;    
    
    @FXML
    private ComboBox<String> cobObject;
    
    @FXML
    private ToggleButton tbShowWireframe;
    
    @FXML
    private TabPane tpSceneLists;
    
    @FXML
    private Pane paneSettings;
	
	public void initialize() {
		Random random = new Random();
		sceneObjects.setEntityListView(lvEntities);
		sceneObjects.setForceListView(lvForces);
		GlobalGravity gravity = new GlobalGravity(0 * EngineProperties.getMetersPerPixel(),
				9.8 * EngineProperties.getMetersPerPixel(), sceneObjects.entities);
		sceneObjects.addForce("gravity", gravity);
		
		
		Material simpleMat = new Material("Simple", 0.001, 1, 0.15f, 0.05f, 0.72);
		sceneObjects.materials.add(simpleMat);
		
		Material stationaryMat = new Material("Stationary", 0, 0, 0, 0, 0);
		stationaryMat.customMass = 0;
		sceneObjects.materials.add(stationaryMat);
		
		Material losslessMat = new Material("Lossless", 0.001, 0, 0, 0, 1);
		sceneObjects.materials.add(losslessMat);
		
		Material basketballMat = new Material("Basketball", 0.001, 0.54, 0.5f, 0.2f, 0.6);
		sceneObjects.materials.add(basketballMat);
		
		Material heavyMat = new Material("Heavy", 0.001, 0.54, 0.5f, 0.2f, 0.60);
		heavyMat.customMass = 999999;
		sceneObjects.materials.add(heavyMat);
		updateMaterialComboBox();
		
		//comboBox
		cobObject.getItems().clear();
		cobObject.getItems().add("Circle RigidBody");
		cobObject.getItems().add("Polygon RigidBody");
		cobObject.getItems().add("Particle Entity");
		cobObject.getItems().add("Spring");
		cobObject.getItems().add("Global Gravity");
		cobObject.getItems().add("Joint");
		cobObject.getSelectionModel().select(0);
		setToObjectSettings(null);	 
		
//		//double pendulum
//		ParticleEntity particle1 = new ParticleEntity(300, 100, 0, 0, stationaryMat, Color.BLUE);
//		ParticleEntity particle2 = new ParticleEntity(500, 100, 0, 0, simpleMat, Color.RED);
//		ParticleEntity particle3 = new ParticleEntity(500, 300, 0, 0, simpleMat, Color.RED);
//		//System.out.println(particle1.mass);
//
//		Spring spring = new Spring((EntityUnit)particle1, (EntityUnit)particle2, 99999, 200.0);
//		Spring spring2 = new Spring((EntityUnit)particle2, (EntityUnit)particle3, 99999, 200.0);
//
//		sceneObjects.forceActors.add(spring);
//		sceneObjects.forceActors.add(spring2);
//		sceneObjects.entities.add(particle1);
//		sceneObjects.entities.add(particle2);
//		sceneObjects.entities.add(particle3);
		
		//random particles
//		for (int i = 0;i < 200;i ++) {
//				CircleRigidBody ball = new CircleRigidBody(random.nextInt(600) + 50, random.nextInt(200)+ 50,
//						0, 0, simpleMat, random.nextInt(15) + 5, 
//						Color.rgb(random.nextInt(200) + 55 , random.nextInt(200) + 55, random.nextInt(200) + 55));
//			sceneObjects.addEntity("ball" + String.valueOf(i), ball);
//		}
//		//demolition
//		for (int i = 0;i < 2;i ++) {
//			for (int j = 0;j < 10;j ++) {
//				
//				PolygonRigidBody polyBody = PolygonRigidBody.Builder.newInstance()
//						.setColor(Color.rgb(random.nextInt(200) + 55 , random.nextInt(200) + 55, random.nextInt(200) + 55))
//						.setRotations(0, 0.0)
//						.setVelocity(0, 00)
//						.addPoint(50, 0)			
//						.addPoint(50, 50)
//						.addPoint(0, 50)
//						.build(200 + i*50, 580-j*50, simpleMat);
//				sceneObjects.addEntity("cube " + String.valueOf(i+j), polyBody);
//				
//			}
//		}		

//		CircleRigidBody ball = new CircleRigidBody(650, 200,
//				-1000, 00, simpleMat, 50, 
//				Color.RED);
//		sceneObjects.addEntity("DeathBall", ball);
//		//random triangles
//		for (int i = 0;i < 40;i ++) {
//			PolygonRigidBody polyBody = PolygonRigidBody.Builder.newInstance()
//					.setColor(Color.rgb(random.nextInt(200) + 55 , random.nextInt(200) + 55, random.nextInt(200) + 55))
//					.setRotations(0, random.nextDouble(2*Math.PI))
//					.setVelocity(0, 00)
//					.addPoint(50, 0)			
//					.addPoint(50, 50)
//					.build(random.nextInt(900), random.nextInt(700), simpleMat);
//			sceneObjects.addEntity("triangle " + String.valueOf(i), polyBody);
//		}
		
//		//newtons Cradle
//		ParticleEntity anchor = new ParticleEntity(300, 100, 0, 0, stationaryMat, Color.RED);
//		CircleRigidBody newtonsBall = new CircleRigidBody(100, 100, 0, 0, losslessMat, 50, Color.BLUE);
//		Spring wire = new Spring(anchor, newtonsBall, 99999, 200);
//		sceneObjects.addEntity("Anchor", anchor);
//		sceneObjects.addEntity("Ball", newtonsBall);
//		sceneObjects.addForce("Wiree", wire);
//		
//		for (int i = 0;i < 5;i ++) {
//			anchor = new ParticleEntity(400 + i * 100, 100, 0, 0, stationaryMat, Color.RED);
//			newtonsBall = new CircleRigidBody(400 + i * 100, 300, 0, 0, losslessMat, 50, Color.BLUE);
//			wire = new Spring(anchor, newtonsBall, 99999, 200);
//			sceneObjects.addEntity("Anchor " + i, anchor);
//			sceneObjects.addEntity("Ball " + i, newtonsBall);
//			
//			sceneObjects.addForce("wire " + i, wire);
//			}
		
		
		timer = new AnimationTimer() {

			//now is defined in nanoseconds
			@Override
			public void handle(long now) {
				if (previousTime == 0) {previousTime = now; return; }
				double deltaSecond = (now-previousTime)/1_000_000_000.0;
				runSimulation(deltaSecond);
				previousTime = now;
			}	
		};
		
		
	}
	
	public void setCanvas(EngineCanvas controller) {
		canvas = controller;
		canvas.drawFrame((float) (1f/60));
	}
	
	public void runSimulation(double deltaSecond) {
		for (int step =0;step < EngineProperties.getStepCount();step++) {
			
			for (int i =0;i < sceneObjects.forceActors.size();i++) {
				sceneObjects.forceActors.get(i).applyForce();
			}
			
			for (int i = 0;i < sceneObjects.entities.size();i++) {
				EntityUnit object = sceneObjects.entities.get(i);
				//weight is 9.8N/kg downwards
				
				if (object instanceof CircleCollider) {
					CircleCollider collider = ((CircleCollider) object); 
					collider.vsCanvas(canvas.canvas.getWidth(), canvas.canvas.getHeight());	

					for (int j = i + 1;j < sceneObjects.entities.size();j++) {
						EntityUnit checked = sceneObjects.entities.get(j);		
						if (checked instanceof CircleCollider) {
							CircleCollider colliderB = (CircleCollider) checked;
							collider.vsCircle(checked, colliderB);
						}
						else if (checked instanceof PolygonCollider) {
							PolygonCollider colliderB = (PolygonCollider) checked;
							collider.vsPolygon(checked, colliderB);
						}
					}
				}
				if (object instanceof PolygonCollider) {
					PolygonCollider collider = (PolygonCollider) object;
					collider.vsCanvas(canvas.canvas.getWidth(), canvas.canvas.getHeight());
					for (int j = i + 1;j < sceneObjects.entities.size();j++) {
						EntityUnit checked = sceneObjects.entities.get(j);		
						if (checked instanceof PolygonCollider) {
							PolygonCollider colliderB = (PolygonCollider) checked;
							collider.vsPolygon(checked, colliderB);
							
						}
						else if (checked instanceof CircleCollider) {
							CircleCollider colliderB = (CircleCollider) checked;
							collider.vsCircle(checked, colliderB);
						}
					}
					collider.applyRotationalAirResistance();
				}

				object.applyAirResistance();
				object.update(deltaSecond/EngineProperties.getStepCount());
			}
						
		}
		canvas.drawFrame((float) (1f/deltaSecond));
	}
    
    @FXML
    void onStepClicked(ActionEvent event) {
    	if (EngineProperties.isPlaying) { return; }
    	runSimulation(Double.parseDouble(tfTimeStepSize.getText()));
    }
    
    @FXML
    void onAirDensityChanged(ActionEvent event) {
    	if (Double.parseDouble(tfAirDensity.getText()) == Double.NaN) {return; }
    	EngineProperties.airDensity = Double.parseDouble(tfAirDensity.getText())
    			/ Math.pow(EngineProperties.METERS_PER_PIXEL, 2);
    }
    
    @FXML
    void onPlayClicked(ActionEvent event) {
    	if (!EngineProperties.isPlaying) { previousTime = 0; timer.start(); btnPausePlay.setText("Stop"); }
    	else { timer.stop(); btnPausePlay.setText("Play"); }
    	EngineProperties.isPlaying = !EngineProperties.isPlaying;
    }
    
    @FXML
    void onApplyFrictionInteracted(ActionEvent event) {
		EngineProperties.applyFriction = cbApplyFriction.isSelected();
		System.out.println(EngineProperties.applyFriction );
    }
    
    @FXML
    void showWireframeClicked(ActionEvent event) {
    	EngineProperties.showWireframe = tbShowWireframe.isSelected();
    	canvas.drawFrame(0);
    }
    
    @FXML
    void onDeleteClicked(ActionEvent event) {
    	int index;
    	switch (tpSceneLists.getSelectionModel().getSelectedIndex()) {
    	case 0:
    		index = lvEntities.getSelectionModel().getSelectedIndex();
    		sceneObjects.deleteEntity(index);
    		canvas.drawFrame(0);
    		break;
    		
    	case 1:
    		index = lvForces.getSelectionModel().getSelectedIndex();
    		sceneObjects.deleteForce(index);
    		canvas.drawFrame(0);
    		break;
    	}
    }
    
    void updateMaterialComboBox() { 
    	cobMaterial.getItems().clear();
    	for (int i = 0;i < sceneObjects.materials.size();i++) { 
    		cobMaterial.getItems().add(sceneObjects.materials.get(i).name);
    	}
    	cobMaterial.setValue(cobMaterial.getItems().get(0));
    }
    
    @FXML
    void startMaterialWindow(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("MaterialSettings.fxml"));
        	AnchorPane root =(AnchorPane)loader.load();
        	Scene scene2 = new Scene(root);          
        	scene2.getStylesheets().add(getClass().getResource("application.css").toExternalForm());        
        	Stage Window2 = new Stage();  
        	Window2.setScene(scene2);
        	Window2.show();
        	
        	MaterialSettingsController c = loader.getController();
        	c.setMasterController(this);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
	}
    
    @FXML
    private void setToObjectSettings(ActionEvent event) {
    	paneSettings.getChildren().clear();
    	String filename = null;
    	String object = cobObject.getSelectionModel().getSelectedItem();
    	if (object == null) { return; }
    	switch (object) {
	    	case "Circle RigidBody":
	    		filename = "CircleRigidBodySettings.fxml";
	    		break;
	    	case "Polygon RigidBody":
	    		filename = "PolygonRigidBodySettings.fxml";
	    		break;
	    	case "Particle Entity":
	    		filename = "ParticleEntitySettings.fxml";
	    		break;
	    	case "Spring":
	    		filename = "SpringSettings.fxml";
	    		break;
	    	case "Global Gravity":
	    		filename = "GlobalGravitySettings.fxml";
	    		break;
	    	case "Joint":
	    		filename = "JointSettings.fxml";
	    		break;
    	}
    	if (filename == null) { return; }
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource(filename));
        	AnchorPane root =(AnchorPane)loader.load();
        	paneSettings.getChildren().add(root);       	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @FXML
    void onSaveScene(ActionEvent event) {
    	String filename = tfSaveSceneName.getText();
    	if (filename == "") { return; }
    	File tmpDir = new File(filename);
    	if (tmpDir.exists()) { return; }
    	try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);			
			SerializableObjects obj = new SerializableObjects(sceneObjects.entities, sceneObjects.forceActors);		
			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    void onLoadScene(ActionEvent event) {
    	String filename = tfSaveSceneName.getText();
    	if (filename == "") { return; }
    	File tmpDir = new File(filename);
    	if (!tmpDir.exists()) { return; }
    	try {
			FileInputStream fileInputStream
			= new FileInputStream(filename);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			SerializableObjects objects = (SerializableObjects) objectInputStream.readObject();
			sceneObjects.entities.clear();
			sceneObjects.forceActors.clear();
			lvEntities.getItems().clear();
			lvForces.getItems().clear();
			for (int i =0; i < objects.entities.size();i++) {
				sceneObjects.addEntity("entity:" + i, objects.entities.get(i));
			}
			for (int i = 0;i < objects.forceActors.size();i++) {
				sceneObjects.addForce("Force:" + i, objects.forceActors.get(i));
			}			
			Random random = new Random();
			for (int i = 0;i < sceneObjects.entities.size();i++) {
				sceneObjects.entities.get(i).color = 
						Color.rgb(random.nextInt(200) + 55 , random.nextInt(200) + 55, random.nextInt(200) + 55);
			}
			objectInputStream.close();
			canvas.drawFrame(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
    }
}
