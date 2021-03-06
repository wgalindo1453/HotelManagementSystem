import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class StaffModifyController extends DBConnection implements Initializable {

    @FXML private Text mainmenuTV;
    @FXML private Text logoutTV;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private DatePicker dobPicker;
    @FXML private Text modifyTV;
    @FXML private Text backTV;
    @FXML private PasswordField passwordField;
    @FXML private Label modifyMsgLabel;

    private final User toModify;

    public StaffModifyController(User user){
        toModify = user;
    }

    @FXML
    void sceneChange(MouseEvent event) {
        AnchorPane newScene = null;
        try {
            if (event.getSource() == mainmenuTV) {
                System.out.println("StaffModify mainmenu pressed");
                newScene = FXMLLoader.load(getClass().getResource("StaffMainMenu.fxml"));
            }
            else if(event.getSource() == logoutTV){
                System.out.println("StaffModify logout pressed");
                LoadedUser.getInstance().clearUser();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                LoginController controller = new LoginController();
                loader.setController(controller);
                newScene = loader.load();
            }else if(event.getSource() == modifyTV){
                if (modifyUser()){
                    modifyMsgLabel.setText("Successfully modified user information");
                    modifyMsgLabel.setTextFill(Color.web("#00FF00"));
                    String query = "UPDATE users SET fname=?, lname=?, phone=?, password=?, dob=? WHERE email=?";
                    Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setString(1, toModify.getFirstName());
                    ps.setString(2, toModify.getLastName());
                    ps.setString(3, toModify.getPhoneNumber());
                    ps.setString(4, Hasher.getInstance("SHA-256").hash(toModify.getPassword()));
                    ps.setDate(5, Date.valueOf(toModify.getDob()));
                    ps.setString(6, toModify.getEmail());
                    ps.execute();
                }
                else {
                    modifyMsgLabel.setText("No changes made to user information");
                    modifyMsgLabel.setTextFill(Color.web("#FF0000"));
                }
                return;
            }else if(event.getSource() == backTV){
                newScene = FXMLLoader.load(getClass().getResource("StaffAccounts.fxml"));
            }
        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(newScene);
        Stage window = (Stage)((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    private boolean modifyUser() throws NoSuchAlgorithmException {
        boolean modified = false;

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();
        String hashed = (password == null) ? toModify.getPassword() : Hasher.getInstance("SHA-256").hash(password);
        LocalDate dob = dobPicker.getValue();

        if(!Validators.isValidPhoneNumber(phoneNumber)){
            modifyMsgLabel.setText("Invalid phone number format");
            modifyMsgLabel.setTextFill(Color.web("#FF0000"));
        }else{
            if(!toModify.getFirstName().equals(firstNameField.getText())) {
                toModify.setFirstName(firstName);
                modified = true;
            }
            if(!toModify.getLastName().equals(lastName)){
                toModify.setLastName(lastName);
                modified = true;
            }
            if(hashed != null && !toModify.getPassword().equals(hashed)){
                toModify.setPassword(password);
                modified = true;
            }
            if(!toModify.getPhoneNumber().equals(phoneNumber)){
                toModify.setPhoneNumber(phoneNumber);
                modified = true;
            }
            if(toModify.getDob().compareTo(dob) != 0){
                toModify.setDob(dob);
                modified = true;

            }
        }

        return modified;
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("STAFF MODIFY CONTROLLER . JAVA ");
        firstNameField.setText(toModify.getFirstName());
        lastNameField.setText(toModify.getLastName());
        passwordField.setText(toModify.getPassword());
        passwordField.setSkin(passwordField.getSkin());
        emailField.setText(toModify.getEmail());
        phoneNumberField.setText(toModify.getPhoneNumber());
        dobPicker.setValue(toModify.getDob());
        System.out.println("PASSWORD TO USER WE SELECTED "+toModify.getPassword().toString());
        emailField.setEditable(false);
    }
}
