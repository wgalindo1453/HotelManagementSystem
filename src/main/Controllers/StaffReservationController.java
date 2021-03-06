import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

/*****************************************************************
 *                     StaffReservationController Class
 * - populates a ListView of all reservations in the DB
 * - clicking a reservation triggers a detailed view pop up window or scene
 * - can filter through reservations by various search criteria
 *****************************************************************/

public class StaffReservationController extends DBConnection implements Initializable {

    @FXML private TableView<Reservation> resTable;
    @FXML private Text mainmenuTV;
    @FXML private Text logoutTV;
    @FXML private TableColumn<Reservation, String> userIDColumn;
    @FXML private TableColumn<Reservation, String> hotelColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> checkOutColumn;
    @FXML private TableColumn<Reservation, String> costColumn;
    @FXML private TableColumn<Reservation, String> resNumColumn;
    @FXML private TableColumn<Reservation, String> image;
    @FXML private TextField hotelTF;
    @FXML private TextField resNumTF;
    @FXML private TextField nameTF;
    @FXML private DatePicker checkInDP;
    @FXML private DatePicker checkOutDP;
    @FXML private Button applyRBtn;
    @FXML private Button resetBtn;
    @FXML private Button modifyBtn;
    @FXML private Button deleteBtn;
    @FXML private ObservableList<Reservation> resList;
    private Connection conn;
    private StringBuilder query;
    LocalDate today = LocalDate.now();
    LocalDate future = LocalDate.now().plusMonths(1);
    int userID;
    /*****************************************************************
     *                     sceneChange Function
     * @param event
     * - Handles all button pressing input to various scenes
     *****************************************************************/
    @FXML void sceneChange(MouseEvent event) {
        AnchorPane newScene = null;

        // try block attempts to load a new scene
        try {
            if (event.getSource() == mainmenuTV) {
                newScene = FXMLLoader.load(getClass().getResource("StaffMainMenu.fxml"));
                System.out.println("Log: StaffRes -> MainMenuBtn");
            } else if(event.getSource() == logoutTV) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                LoginController controller = new LoginController();
                loader.setController(controller);
                newScene = loader.load();
                System.out.println("Log: StaffRes -> LoginBtn");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(newScene);
        Stage window = (Stage)((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    // handles the modify button when pressed
    @FXML void modifyHandler(ActionEvent event) {
        AnchorPane newScene = null;
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow(); // for displaying Toast error messages

        // try block attempts to load a new scene
        try {
            if(resTable.getSelectionModel().getSelectedItem() == null) {
                Toast.makeText(stage, "Error: no reservation selected", 2000, 500, 500);
                return;
            } else {
                System.out.println("Log: StaffRes -> ModifyBtn");
                Reservation reservation = resTable.getSelectionModel().getSelectedItem();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("StaffResModify.fxml"));
                StaffResModify controller = new StaffResModify(reservation, "staff");
                loader.setController(controller);
                newScene = loader.load();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(newScene);
        Stage window = (Stage)((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    /*****************************************************************
     *                     initialize Function
     * - initializes global variables
     * - sets up SQL DB connection
     * - calls populateList() to populate ListView
     *****************************************************************/
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkInDP.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);


                setDisable(empty || date.compareTo(today) < 0 );
            }
        });
        restrictDatePicker(checkInDP);
        restrictDatePicker(checkOutDP);
        // set cell values
        image.setCellValueFactory(new PropertyValueFactory<>("photo"));
        userIDColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        hotelColumn.setCellValueFactory(new PropertyValueFactory<>("hotelName"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        resNumColumn.setCellValueFactory(new PropertyValueFactory<>("resID"));

        // get list of reservations
        resList = FXCollections.observableArrayList();
        query = new StringBuilder();
        try{
            // get connection
            conn = getConnection();
            populateListView();
        } catch(SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    /*****************************************************************
     *                     populateListView Function
     * - populates the list view with reservation data
     * - makes a query to DB for all reservations for a specific user
     *****************************************************************/
    public void populateListView() throws SQLException{
        Connection con = null;
        try {
            con = getConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        CallableStatement callableStatement = con.prepareCall("{call hotel.getID(?,?)}");
        callableStatement.setString(1,LoadedUser.getInstance().getUser().getFirstName().toString());
        callableStatement.setString(2,"CUST");
        ResultSet rs = callableStatement.executeQuery();
        while(rs.next()){
            userID = rs.getInt("ID");
        }
        System.out.println(userID+"<-USER ID");

        CallableStatement cs = con.prepareCall("{call hotel.ReservationFilter(?, ?, ?, ?)}");
        cs.setString(1, "");
        cs.setString(2, "");
        cs.setDate(3, null);
       cs.setDate(4, null);
        ResultSet rs2 = cs.executeQuery();

        if(rs2.next()){
            addReservations(rs2);
        }

        resTable.setItems(resList);
    }

    /*****************************************************************
     *                     handleFilter Method
     * @param event - holds data on the UI element clicked either resetBtn or applyBtn
     * - if resetBtn then clear the ListView and repopulate it will all reservation data
     * - else it is applyBtn so repopulate listview with filters applied
     *****************************************************************/
    @FXML private void handleFilter(ActionEvent event) throws SQLException {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow(); // for displaying Toast error messages
        if(event.getSource() == resetBtn) {
            resList.clear();
            populateListView();
        } else {

            LocalDate checkin = checkInDP.getValue();
            LocalDate checkout = checkOutDP.getValue();
            String hotel = hotelTF.getText().toString();
            String name = nameTF.getText().toString();
            System.out.print(name);
            System.out.println(hotel);
            
            System.out.println("TESTING");
            if(hotel.equals("") && name.equals("") && checkin == null && checkout == null) {
                Toast.makeText(stage, "Error: no filter information given", 2000, 500, 500);
            } else if(checkin != null && checkout != null) {
                if(checkin.isAfter(checkout)){
                    Toast.makeText(stage, "Error: Check in date cannot be after checkout", 2000, 500, 500);
                }
            }
            System.out.println("TESTING");

            Connection con = null;
            try {
                con = getConnection();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            CallableStatement callableStatement = con.prepareCall("{call hotel.ReservationFilter(?, ?, ?, ?)}");
            callableStatement.setString(1, name);
            callableStatement.setString(2, hotel);
            if(checkin != null)
                callableStatement.setDate(3, Date.valueOf(checkInDP.getValue()));
            else
                callableStatement.setDate(3, null);
            if(checkout != null)
                callableStatement.setDate(4, Date.valueOf(checkOutDP.getValue()));
            else
                callableStatement.setDate(4, null);
            ResultSet rs = callableStatement.executeQuery();

            resList.clear();



            while(rs.next()) {
               Reservation r = new Reservation();
                File file = new File("Res/images/hotels/" + rs.getString("hotel_image"));
                Image image = new Image(file.toURI().toString());
                ImageView im = new ImageView();
                im.setImage(image);
                im.setPreserveRatio(true);
                im.setFitHeight(150);
                im.setFitWidth(200);
                r.setPhoto(im);
               r.setName(rs.getString("fname"));
               r.setHotelName(rs.getString("hotel_name"));
               r.setCheckIn(rs.getDate("check_in").toLocalDate());
               r.setCheckOut(rs.getDate("check_out").toLocalDate());
               r.setCost(rs.getDouble("total_price"));
               r.setResID(rs.getInt("reservationId"));
               resList.add(r);
            }
            resTable.refresh();
        }
    }

    // method clears text all filter TextFields & Date Pickers
    private void resetSearchFields(){
        hotelTF.clear();
        checkInDP.setValue(null);
        checkOutDP.setValue(null);
    }
    EventHandler<MouseEvent> mouseClickedEventHandler = clickEvent -> {
        if (clickEvent.getButton() == MouseButton.PRIMARY) {

        }
        clickEvent.consume();
    };
    public void restrictDatePicker(DatePicker datePicker) {
        datePicker.setDayCellFactory((DatePicker param) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    //...
                    addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
                } else {
                    //...
                    removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
                }
                if (item.isBefore(today)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #FF4500;");
                }
            }
        });
    }

    /*****************************************************************
     *                     addReservations Method
     * @param rs - holds reservations data from DB
     * - uses a ResultSet iterable to parse the res data into an ObservableList
     *****************************************************************/
    private void addReservations(ResultSet rs) throws SQLException {
        do{
            Reservation res = new Reservation();
            File file = new File("Res/images/hotels/" + rs.getString("hotel_image"));
            Image image = new Image(file.toURI().toString());
            ImageView im = new ImageView();
            im.setImage(image);
            im.setPreserveRatio(true);
            im.setFitHeight(150);
            im.setFitWidth(200);
            res.setPhoto(im);
            res.setCost(rs.getDouble("total_price"));
            res.setCheckIn(rs.getDate("check_in").toLocalDate());
            res.setCheckOut(rs.getDate("check_out").toLocalDate());
            res.setHotelName(rs.getString("hotel_name"));
            res.setName(rs.getString("fname"));
            res.setResID(rs.getInt("reservationId"));
            res.setAdults(rs.getInt("adults"));
            res.setChildren(rs.getInt("children"));
            resList.add(res);
            System.out.println(res);
        } while(rs.next());
    }

    // pops up alert dialog window to confirm deletion of reservation and then queries DB to do so
    @FXML private void deleteRes() throws SQLException, ClassNotFoundException {
        Reservation reservation2Delete = resTable.getSelectionModel().getSelectedItem();
        System.out.println("Log: StaffResManagey -> deleteBtn");
        ButtonType deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.NONE,"Please confirm reservation deletion", deleteBtn, cancelBtn);
        alert.setTitle("Deletion Confirmation");
        //alert.setContentText("Please confirm reservation deletion");
        Optional<ButtonType> result = alert.showAndWait();

        // if user confirmed reservation deletion
        if(result.orElse(cancelBtn) == deleteBtn){
            System.out.println("Delete");
            PreparedStatement ps = null;


           conn= getConnection();
                    ps = conn.prepareStatement("DELETE FROM reservation where reservationId = ?");
                    ps.setInt(1, reservation2Delete.getResID());
                    ps.execute();
                    System.out.println("Deleted");
        }

        resList.clear();
        populateListView();
    }
}