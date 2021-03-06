import com.mysql.jdbc.log.Log;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StaffMainMenuController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button propertyBtn;
    @FXML private Button reservationBtn;
    @FXML private Button accountBtn;
    @FXML private Button logoutButton;
    @FXML private Button createResBtn;
    @FXML private Label welcomeMessage;

    // launches scenes using a set of if statements to determine btn pressed and launches new scene accordingly
    @FXML void launchScene(ActionEvent event) {
        AnchorPane newScene = null;

        try {
            if(event.getSource() == profileBtn){
                newScene = FXMLLoader.load(getClass().getResource("UserProfile.fxml"));
                System.out.println("Log: MainMenu -> profileBtn");
            }
            if (event.getSource() == propertyBtn){
                newScene = FXMLLoader.load(getClass().getResource("StaffProperty.fxml"));
                System.out.println("Log: MainMenu -> propertyBtn");
            }
            else if (event.getSource() == reservationBtn) {
                newScene = FXMLLoader.load(getClass().getResource("StaffReservation.fxml"));
                System.out.println("Log: MainMenu -> reservationBtn");
            }
            else if (event.getSource() == logoutButton) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                LoginController controller = new LoginController();
                loader.setController(controller);
                newScene = loader.load();
                System.out.println("Log: MainMenu -> logout");
            }
            else if (event.getSource() == accountBtn) {
                newScene = FXMLLoader.load(getClass().getResource("StaffAccounts.fxml"));
                System.out.println("Log: MainMenu -> accountsBtn");
            } else if (event.getSource() == createResBtn) {
                newScene = FXMLLoader.load(getClass().getResource("UserCreate.fxml"));
                System.out.println("Log: MainMenu -> Create Res");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(newScene);
        Stage window = (Stage)((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeMessage.setText("Welcome " + LoadedUser.getInstance().getUser().getFirstName());
    }
}
