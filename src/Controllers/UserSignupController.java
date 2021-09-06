import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserSignupController extends User implements Initializable {
    //MACROS
    private static final int INCORRECT_PASSWORD = -1;
    @FXML private Button backToLoginButton;
    @FXML private TextField signupUsernameField;
    @FXML private TextField signupPasswordField;
    @FXML private TextField signupConfirmPasswordField;
    @FXML private TextField signupFirstNameField;
    @FXML private TextField signupLastNameField;
    @FXML private TextField signupEmailField;
    @FXML private TextField signupPhoneField;
    @FXML private DatePicker signupDOBField;
    @FXML private Button signupButton;
    @FXML private Label passwordErrorLabel;
    @FXML private void toLogin(ActionEvent event) throws IOException {
        AnchorPane loginScreen = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(loginScreen);
        Stage window = (Stage)((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
        if (scene.focusOwnerProperty().get() instanceof TextArea) {
            TextArea focusedTextArea = (TextArea) scene.focusOwnerProperty().get();
        }
    }
    /**checks database for username on letter by letter basis , testing this out kinda slow due to constant db connection...
     * will look into faster approach.
    **/
    public void validateUserName() throws SQLException {


/**
        if (validate(signupUsernameField.getText(), signupPasswordField.getText())== ){ //already set userId
            signupUserNameGood.setText("");
            signupUserNameBad.setText("Sorry username already exist");
        }else {
            signupUserNameBad.setText("");
            signupUserNameGood.setText("Great choice!");
        }
 **/
    }
    public void validateConfirmPassword(){
      if(signupPasswordField.getText().equals(signupConfirmPasswordField.getText()))
      {
          passwordErrorLabel.setText("Match");
          passwordErrorLabel.setTextFill(Color.GREEN);
          System.out.println("match");

      }
      else if (!signupPasswordField.getText().equals(signupConfirmPasswordField.getText())){
          System.out.println("does not match");
          passwordErrorLabel.setTextFill(Color.RED);
          passwordErrorLabel.setText("does not Match");
      }

    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backToLoginButton.setOnAction(event -> {
            try {
                toLogin(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        signupConfirmPasswordField.setOnKeyReleased(event -> {
            validateConfirmPassword();
        });

        signupUsernameField.setOnKeyReleased(event -> {
            try {
                validateUserName();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });






















    }
}