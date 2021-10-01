import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class StaffReservationController implements Initializable {

    @FXML private TableView<Reservation> resTable;

    @FXML private Text mainmenuTV;

    @FXML private Text logoutTV;

    @FXML private TableColumn<Reservation, String> userIDColumn;

    @FXML private TableColumn<Reservation, String> hotelColumn;

    @FXML private TableColumn<Reservation, String> datesColumn;

    @FXML private TableColumn<Reservation, String> roomsColumn;

    @FXML private TableColumn<Reservation, String> costColumn;

    @FXML private TableColumn<Reservation, String> resNumColumn;

    @FXML private TextField useridTF;

    @FXML private TextField hotelTF;

    @FXML private TextField resNumTF;

    @FXML private ObservableList<User> resList;

    private Connection conn;
    
    // changes the scene when mainmenuTV or logoutTV are clicked
    @FXML void sceneChange(MouseEvent event) {
        AnchorPane newScene = null;
        
        // try block attempts to load a new scene
        try {
            if (event.getSource() == mainmenuTV)
                newScene = FXMLLoader.load(getClass().getResource("StaffMainMenu.fxml"));
            else if(event.getSource() == logoutTV){
                newScene = FXMLLoader.load(getClass().getResource("login.fxml"));
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

    }

    public void populateListView(){
        // get list of reservations

        // get connection
        // conn = getConnection();
        // query the database
    }
}
