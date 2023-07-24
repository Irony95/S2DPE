package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import physicsComponents.*;

public class MaterialSettingsController {
	
	EngineMasterController master;

    @FXML
    private Button btnSave;

    @FXML
    private TextField tfDensity;

    @FXML
    private TextField tfDragCoefficient;

    @FXML
    private TextField tfDynamicFriction;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfRestitution;

    @FXML
    private TextField tfStaticFriction;
    
    @FXML
    private TextField tfCustomInertia;

    @FXML
    private TextField tfCustomMass;
    
    void setMasterController(EngineMasterController c) { master = c; }

    @FXML
    void btnOnSave(ActionEvent event) {
    	try {
    		
    		double density = Double.parseDouble(tfDensity.getText());
    		double dragCoeff = Double.parseDouble(tfDragCoefficient.getText());
    		double dynamicFriction = Double.parseDouble(tfDynamicFriction.getText());
    		double staticFriction = Double.parseDouble(tfStaticFriction.getText());
    		double restitution = Double.parseDouble(tfRestitution.getText());
    		
    		String matName = tfName.getText();
    		if (matName == "") { return; }
    		
    		Material newMat = new Material(matName, density, dragCoeff, staticFriction, dynamicFriction, restitution);
    		String customMass = tfCustomMass.getText();
    		if (customMass != "") {
    			newMat.customMass = Double.parseDouble(customMass);
    		}
    		
    		String customInertia = tfCustomInertia.getText();
    		if (customInertia != "") {
    			newMat.customInertia = Double.parseDouble(customInertia);
    		}
    		SceneObjects.getInstance().materials.add(newMat);
    		master.updateMaterialComboBox();
    		
    		Stage window2 = (Stage)btnSave.getScene().getWindow();
    		window2.close();
    	}
    	catch(Exception e) {
    		
    	}
    }

}
