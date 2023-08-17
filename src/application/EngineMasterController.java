package application;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import physicsComponents.*;
import world.ObjectSpawner;
import world.SceneObjects;
import world.ObjectSpawner.ObjectSelector;

import java.util.ArrayList;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
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
	private EngineWorld world;
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
    public ToggleButton tbPlace;
    
    @FXML
    private TabPane tpSceneLists;
    
    @FXML
    private Pane paneSettings;
    
    private ParticleEntity mouseParticle;
    public Spring mouseSpring;
	
	public void initialize() {
		sceneObjects.setEntityListView(lvEntities);
		sceneObjects.setForceListView(lvForces);
		GlobalGravity gravity = new GlobalGravity(0 * EngineProperties.getPixelsPerMeter(),
				9.8 * EngineProperties.getPixelsPerMeter(), sceneObjects.entities);
		sceneObjects.addForce("gravity", gravity);
		
		
		Material simpleMat = new Material("Simple", 0.001, 1, 0.15f, 0.05f, 0.72);
		sceneObjects.materials.add(simpleMat);
		
		Material stationaryMat = new Material("Stationary", 0, 0, 0, 0, 0);
		stationaryMat.customMass = 0;
		stationaryMat.customInertia = 0;
		sceneObjects.materials.add(stationaryMat);
		
		Material losslessMat = new Material("Perfect", 0.001, 0, 0, 0, 1);
		sceneObjects.materials.add(losslessMat);
		
		Material heavyMat = new Material("Heavy", 0.001, 0.54, 0.5f, 0.2f, 0.60);
		heavyMat.customMass = 999999999;
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
		
		mouseParticle = new ParticleEntity(0, 0, 0, 0, stationaryMat, Color.TRANSPARENT);	
		
		timer = new AnimationTimer() {

			//now is defined in nanoseconds
			@Override
			public void handle(long now) {
				if (previousTime == 0) {previousTime = now; return; }
				double deltaSecond = (now-previousTime)/1_000_000_000.0;
				if (EngineProperties.isPlaying) {
					runSimulation(deltaSecond);
				}
				previousTime = now;
				
				world.drawFrame((float) (1f/deltaSecond));
			}	
		};
		timer.start();		
	}
	
	public void setCanvas(EngineWorld controller) {
		world = controller;
		
		world.canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				mouseParticle.position.setEntry(0, arg0.getX());
				mouseParticle.position.setEntry(1, arg0.getY());
				double closestDist = Double.POSITIVE_INFINITY;
				EntityUnit object = null;
				for (int i =0; i< sceneObjects.entities.size();i++) {
					double dist = sceneObjects.entities.get(i).position.getDistance(mouseParticle.position);
					if (dist < closestDist) {
						closestDist = dist;
						object = sceneObjects.entities.get(i);											
					}
				}
				if (object == null) { return; }
				mouseSpring = new Spring(mouseParticle, object, 70, 0, 10);
			}			
		});
		world.canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				mouseSpring = null;				
			}			
		});
		world.canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {	
				boolean runs = world.canvas.getOnMouseClicked() == null;
				if (tbPlace.isSelected() && runs) { 
					mouseSpring = null;
					return;
				}
				mouseParticle.position.setEntry(0, arg0.getX());
				mouseParticle.position.setEntry(1, arg0.getY());
			}			
		});
	}
	
	public void runSimulation(double deltaSecond) {
		for (int step =0;step < EngineProperties.getStepCount();step++) {
			double stepSecondsDelta = deltaSecond/EngineProperties.getStepCount();
			
			for (int i =0;i < sceneObjects.forceActors.size();i++) {
				sceneObjects.forceActors.get(i).applyForce();
			}
			
			boolean runs = world.canvas.getOnMouseClicked() == null;
			if (mouseSpring != null && !tbPlace.isSelected() && runs) {
				mouseSpring.applyForce();
			}
			for (int i = 0;i < sceneObjects.entities.size();i++) {
				EntityUnit object = sceneObjects.entities.get(i);
				//weight is 9.8N/kg downwards
				
				if (object instanceof CircleCollider) {
					CircleCollider collider = ((CircleCollider) object); 
					collider.vsCanvas(world.canvas.getWidth(), world.canvas.getHeight(), stepSecondsDelta);	

					for (int j = i + 1;j < sceneObjects.entities.size();j++) {
						EntityUnit checked = sceneObjects.entities.get(j);		
						if (checked instanceof CircleCollider) {
							CircleCollider colliderB = (CircleCollider) checked;
							collider.vsCircle(checked, colliderB, stepSecondsDelta);
						}
						else if (checked instanceof PolygonCollider) {
							PolygonCollider colliderB = (PolygonCollider) checked;
							collider.vsPolygon(checked, colliderB, stepSecondsDelta);
						}
					}
				}
				if (object instanceof PolygonCollider) {
					PolygonCollider collider = (PolygonCollider) object;
					collider.vsCanvas(world.canvas.getWidth(), world.canvas.getHeight(), stepSecondsDelta);
					for (int j = i + 1;j < sceneObjects.entities.size();j++) {
						EntityUnit checked = sceneObjects.entities.get(j);		
						if (checked instanceof PolygonCollider) {
							PolygonCollider colliderB = (PolygonCollider) checked;
							collider.vsPolygon(checked, colliderB, stepSecondsDelta);
							
						}
						else if (checked instanceof CircleCollider) {
							CircleCollider colliderB = (CircleCollider) checked;
							collider.vsCircle(checked, colliderB, stepSecondsDelta);
						}
					}
					collider.applyRotationalAirResistance();
				}

				object.applyAirResistance();
				object.update(stepSecondsDelta);
			}
						
		}		
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
    			/ Math.pow(EngineProperties.getPixelsPerMeter(), 2);
    }
    
    @FXML
    void onPlayClicked(ActionEvent event) {
    	if (!EngineProperties.isPlaying) {
    		btnPausePlay.setText("Stop");
		}
    	else { btnPausePlay.setText("Play"); }
    	EngineProperties.isPlaying = !EngineProperties.isPlaying;    	
    }
    
    @FXML
    void onApplyFrictionInteracted(ActionEvent event) {
		EngineProperties.applyFriction = cbApplyFriction.isSelected();
    }
    
    @FXML
    void showWireframeClicked(ActionEvent event) {
    	EngineProperties.showWireframe = tbShowWireframe.isSelected();
    }
    
    @FXML
    void onDeleteClicked(ActionEvent event) {
    	int index;
    	switch (tpSceneLists.getSelectionModel().getSelectedIndex()) {
    	case 0:
    		index = lvEntities.getSelectionModel().getSelectedIndex();
    		sceneObjects.deleteEntity(index);
    		break;
    		
    	case 1:
    		index = lvForces.getSelectionModel().getSelectedIndex();
    		sceneObjects.deleteForce(index);
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
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/MaterialSettings.fxml"));
        	AnchorPane root =(AnchorPane)loader.load();
        	Scene scene2 = new Scene(root);          
        	scene2.getStylesheets().add(getClass().getResource("application.css").toExternalForm());        
        	Stage Window2 = new Stage();  
        	Window2.setTitle("New Material");
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
    	ObjectSpawner.polygonPoints.clear();
    	if (object == null) { return; }
    	switch (object) {
	    	case "Circle RigidBody":
	    		filename = "fxml/CircleRigidBodySettings.fxml";
	    		break;
	    	case "Polygon RigidBody":	    		
	    		filename = "fxml/PolygonRigidBodySettings.fxml";
	    		break;
	    	case "Particle Entity":
	    		filename = "fxml/ParticleEntitySettings.fxml";
	    		break;
	    	case "Spring":
	    		filename = "fxml/SpringSettings.fxml";
	    		break;
	    	case "Global Gravity":
	    		filename = "fxml/GlobalGravitySettings.fxml";
	    		break;
	    	case "Joint":
	    		filename = "fxml/JointSettings.fxml";
	    		break;
    	}
    	if (filename == null) { return; }
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource(filename));
        	AnchorPane root =(AnchorPane)loader.load();
        	paneSettings.getChildren().add(root);  
        	if (object == "Polygon RigidBody") {
        		ToggleButton placeVerticesButton = ((ToggleButton)(root.lookup("#btnPlaceVertices")));
	    		placeVerticesButton.setOnAction(actionEvent -> {	    	
	    			if (placeVerticesButton.isSelected()) {
	    				ObjectSpawner.polygonPoints.clear();
	    				world.canvas.setOnMouseClicked(ObjectSpawner.placePolygonPoints(world));
	    			}
	    			else {	    				
	    				world.canvas.setOnMouseClicked(null);
	    			}
	    		});
        	}
        	else if (object == "Spring") {
        		
        		ToggleButton selectObjectA = ((ToggleButton)(root.lookup("#btnObjectA")));
        		ToggleButton selectObjectB = ((ToggleButton)(root.lookup("#btnObjectB")));
        		TextField distanceField = ((TextField)(root.lookup("#tfRestLength")));
        		
        		ObjectSpawner.SelectorCallback callback = new ObjectSpawner.SelectorCallback() {					
        			@Override
        			public void callback() {
        				if (ObjectSpawner.objectA != null && ObjectSpawner.objectB != null) {
        					Double distance = ObjectSpawner.objectA.position
        							.getDistance(ObjectSpawner.objectB.position);
        					distanceField.setText(distance.toString());
        				}
        			}
        		};
        		
        		selectObjectA.setOnAction(actionEvent -> {
        			selectObjectB.setSelected(false);
        			if (selectObjectA.isSelected()) {
        				ObjectSpawner.objectA = null;
        				selectObjectA.setText("Object A");
        				world.canvas.setOnMouseClicked(
        						ObjectSpawner.findAndSetObject(ObjectSpawner.ObjectSelector.A,
								selectObjectA, callback, world.canvas));
        			}
        			else {
        				world.canvas.setOnMouseClicked(null);
        			}
        		});
        		selectObjectB.setOnAction(actionEvent -> {
        			selectObjectA.setSelected(false);
        			if (selectObjectB.isSelected()) {
        				ObjectSpawner.objectB = null;
        				selectObjectB.setText("Object B");
        				world.canvas.setOnMouseClicked(
        						ObjectSpawner.findAndSetObject(ObjectSpawner.ObjectSelector.B,
								selectObjectB, callback, world.canvas));
        			}
        			else {
        				world.canvas.setOnMouseClicked(null);
        			}
        		});
        	}
        	
    		else if (object == "Joint") {        		
        		ToggleButton selectObjectA = ((ToggleButton)(root.lookup("#btnObjectA")));
        		ToggleButton selectObjectB = ((ToggleButton)(root.lookup("#btnObjectB")));        		
        		
        		selectObjectA.setOnAction(actionEvent -> {
        			selectObjectB.setSelected(false);
        			if (selectObjectA.isSelected()) {
        				ObjectSpawner.objectA = null;
        				selectObjectA.setText("Object A");
        				world.canvas.setOnMouseClicked(
        						ObjectSpawner.findAndSetObject(ObjectSpawner.ObjectSelector.A,
								selectObjectA, null, world.canvas));
        			}
        			else {
        				world.canvas.setOnMouseClicked(null);
        			}
        		});
        		selectObjectB.setOnAction(actionEvent -> {
        			selectObjectA.setSelected(false);
        			if (selectObjectB.isSelected()) {
        				ObjectSpawner.objectB = null;
        				selectObjectB.setText("Object B");
        				world.canvas.setOnMouseClicked(
        						ObjectSpawner.findAndSetObject(ObjectSpawner.ObjectSelector.B,
								selectObjectB, null, world.canvas));
        			}
        			else {
        				world.canvas.setOnMouseClicked(null);
        			}
        		});
        	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}  
    }
    
    @FXML
    void onPlaceClicked(ActionEvent event) {
    	
    	switch (cobObject.getSelectionModel().getSelectedItem()) {
    		case "Circle RigidBody":
    			spawningCircle();
    			break;
    		case "Polygon RigidBody":
    			polygonPlaceVertices();
    			break;
    		case "Particle Entity":
    			spawnParticleEntity();
    			break;
    		case "Spring":
    			spawnSpring();
    			break;
    		case "Global Gravity":
    			spawnGravity();
    			break;
    		case "Joint":
    			spawnJoint();
    			break;
    	}
    }
    
    void spawningCircle() {
    	AnchorPane settings = (AnchorPane) paneSettings.lookup("#root");
    	int matIndex = cobMaterial.getSelectionModel().getSelectedIndex();
    	if (tbPlace.isSelected()) {
    		ObjectSpawner.setChildrenDisable(settings, true);
    		cobMaterial.setDisable(true);
    		cobObject.setDisable(true);
    		world.canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {	
					ObjectSpawner.spawnCircle(settings, matIndex, world.mouse.getEntry(0), world.mouse.getEntry(1));
				}    			
    		});
    	}
    	else {
    		ObjectSpawner.setChildrenDisable(settings, false);
    		world.canvas.setOnMouseClicked(null);
    		cobMaterial.setDisable(false);
    		cobObject.setDisable(false);
    	}
    }
    
    void spawnParticleEntity() {
    	AnchorPane settings = (AnchorPane) paneSettings.lookup("#root");
    	int matIndex = cobMaterial.getSelectionModel().getSelectedIndex();
    	if (tbPlace.isSelected()) {    
    		ObjectSpawner.setChildrenDisable(settings, true);
    		cobMaterial.setDisable(true);
    		cobObject.setDisable(true);
    		world.canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
    			@Override 
    			public void handle(MouseEvent arg0) {  
    				ObjectSpawner.spawnParticleEntity(settings, matIndex, world.mouse.getEntry(0), world.mouse.getEntry(1));
    			}	
    		}); 
		}
    	else {
    		ObjectSpawner.setChildrenDisable(settings, false);
    		world.canvas.setOnMouseClicked(null);
    		cobMaterial.setDisable(false);
    		cobObject.setDisable(false);
    	}
    }
    
    void polygonPlaceVertices() {    
    	if (ObjectSpawner.polygonPoints.size() < 3) {
    		tbPlace.setSelected(false);
    		ObjectSpawner.polygonPoints.clear(); 
    		return;
    	}
    	
    	AnchorPane settings = (AnchorPane) paneSettings.lookup("#root");
    	int matIndex = cobMaterial.getSelectionModel().getSelectedIndex();
    	ToggleButton placeVerticesButton = ((ToggleButton)(settings.lookup("#btnPlaceVertices")));
    	if (tbPlace.isSelected()) {     	
    		ObjectSpawner.setChildrenDisable(paneSettings, true);
    		cobMaterial.setDisable(true);
    		cobObject.setDisable(true);
    		placeVerticesButton.setSelected(false);
    		//ObjectSpawner.spawnPolygonRigidBody(settings, matIndex);
    		world.canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
    			@Override
    			public void handle(MouseEvent arg0) {
    				ObjectSpawner.spawnPolygonRigidBody(settings, matIndex, world.mouse.getEntry(0), world.mouse.getEntry(1));
    			}			
    		});
		}
    	else {
    		world.canvas.setOnMouseClicked(null); 
    		ObjectSpawner.setChildrenDisable(paneSettings, false);
    		cobMaterial.setDisable(false);
    		cobObject.setDisable(false);     				    
    	}				
    }
    
    void spawnSpring() {
    	tbPlace.setSelected(false);
    	ObjectSpawner.spawnSpring((AnchorPane)paneSettings.lookup("#root"));
    }    
    
    void spawnGravity() {
    	tbPlace.setSelected(false);
    	ObjectSpawner.spawnGravity((AnchorPane)paneSettings.lookup("#root"));  	
    }
    
    void spawnJoint() {
    	tbPlace.setSelected(false);
    	if (ObjectSpawner.objectA instanceof ParticleEntity && ObjectSpawner.objectB instanceof PolygonRigidBody) {
    		ObjectSpawner.spawnJoint((AnchorPane)paneSettings.lookup("#root"));
    	}
    }
    
    
    @FXML
    void onSaveScene(ActionEvent event) {
    	String filename = tfSaveSceneName.getText();
    	if (filename == "") { return; }
    	try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);	
			ArrayList<String> entityNames = new ArrayList<String>(lvEntities.getItems()); 
			ArrayList<String> forceNames = new ArrayList<String>(lvForces.getItems());
			SerializableObjects obj = new SerializableObjects(sceneObjects.entities,
					sceneObjects.forceActors, entityNames, forceNames);		
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
				sceneObjects.addEntity(objects.entityNames.get(i), objects.entities.get(i));
			}
			for (int i = 0;i < objects.forceActors.size();i++) {
				sceneObjects.addForce(objects.forceNames.get(i), objects.forceActors.get(i));
				if (objects.forceActors.get(i) instanceof GlobalGravity) {
					((GlobalGravity)(objects.forceActors.get(i))).objects = sceneObjects.entities;
				}
			}			
			Random random = new Random();
			for (int i = 0;i < sceneObjects.entities.size();i++) {
				sceneObjects.entities.get(i).color = 
						Color.rgb(random.nextInt(200) + 55 , random.nextInt(200) + 55, random.nextInt(200) + 55);
			}
			objectInputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
