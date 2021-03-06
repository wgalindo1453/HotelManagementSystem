import com.mysql.cj.x.protobuf.MysqlxPrepare;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.xml.transform.Result;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.time.LocalDate;

public class StaffAccountsController extends DBConnection implements Initializable {

    @FXML private AnchorPane accountsScreen;
    @FXML private Text mainmenuTV;
    @FXML private Text logoutTV;
    @FXML private Text modifyTV;
    @FXML private Text createTV;
    @FXML private Text deleteTV;
    @FXML private TextField firstNameSearchField;
    @FXML private TextField lastNameSearchField;
    @FXML private TextField emailSearchField;
    @FXML private TextField phoneNumberSearchField;
    @FXML private DatePicker dobSearchPicker;
    @FXML private ComboBox<String> typeSearchPicker;
    @FXML private Button searchBtn;
    @FXML private Button resetBtn;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> dobColumn;
    @FXML private TableColumn<User, String> typeColumn;

    @FXML private ObservableList<User> usersList;

    private Connection conn;
    private StringBuilder query;

    @FXML void sceneChange(MouseEvent event) {
        AnchorPane newScene = null;
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow(); // for displaying Toast error messages

        try {
            if (event.getSource() == mainmenuTV)
                newScene = FXMLLoader.load(getClass().getResource("StaffMainMenu.fxml"));
            else if(event.getSource() == logoutTV){
                LoadedUser.getInstance().clearUser();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                LoginController controller = new LoginController();
                loader.setController(controller);
                newScene = loader.load();
            }else if(event.getSource() == modifyTV){
                if(usersTable.getSelectionModel().getSelectedItem() == null) {
                    Toast.makeText(stage, "Error: no account selected please select one to modify", 2000, 500, 500);
                    return;
                }
                User selectedUser = usersTable.getSelectionModel().getSelectedItem();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("StaffModify.fxml"));
                StaffModifyController controller = new StaffModifyController(selectedUser);
                loader.setController(controller);
                newScene = loader.load();
            }else if(event.getSource() == createTV){
                newScene = FXMLLoader.load(getClass().getResource("StaffCreateUser.fxml"));
            } else if (event.getSource() == deleteTV) {
                if(usersTable.getSelectionModel().getSelectedItem() == null) {
                    Toast.makeText(stage, "Error: no account selected please select one to delete", 2000, 500, 500);
                    return;
                }
                handleUserDelete();
                return;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(newScene);
        Stage window = (Stage)((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        System.out.println("STAFF ACCOUNTS CONTROLLER . JAVA ");
        usersList = FXCollections.observableArrayList();
        query = new StringBuilder();
        try {
            conn = getConnection();
            populateListView();
        } catch (SQLException | ClassNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        typeSearchPicker.getItems().add("CUST");
        typeSearchPicker.getItems().add("EMP");
    }

    private void populateListView() throws SQLException, NoSuchAlgorithmException {
        query.setLength(0);
        query.append("SELECT * FROM users;");
        ResultSet rs = conn.createStatement().executeQuery(query.toString());

        if(rs.next())
            addUsers(rs);
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        usersTable.setItems(usersList);
    }

    private void handleUserDelete() throws SQLException {
        User user = usersTable.getSelectionModel().getSelectedItem();

        ButtonType deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.NONE,"Please confirm the deletion of the user: " + user.getFirstName() + " " + user.getLastName(), deleteBtn, cancelBtn);
        alert.setTitle("Deletion Confirmation");
        //alert.setContentText("Please confirm reservation deletion");
        Optional<ButtonType> result = alert.showAndWait();

        // if user confirmed reservation deletion
        if(result.orElse(cancelBtn) == deleteBtn){
            System.out.println("Delete");
            usersList.remove(user);
            String query = "delete from users where email = ?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, user.getEmail());

            // execute the preparedstatement
            preparedStmt.execute();
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException {
        if(event.getSource() == resetBtn){
            usersList.clear();
            populateListView();
        }else{
            HashMap<Integer, Object> statementValues = new HashMap<>();
            query.setLength(0);
            query.append("SELECT * FROM users");

            if(buildQuery(statementValues)){
                PreparedStatement ps = conn.prepareStatement(query.toString());
                for(Integer key : statementValues.keySet()){
                    Object obj = statementValues.get(key);
                    if(obj instanceof String) {
                        ps.setString(key, (String)obj);
                    }
                    else if(obj instanceof LocalDate) {
                        ps.setDate(key, Date.valueOf((LocalDate) obj));
                    }
                }
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    usersList.clear();
                    addUsers(rs);
                }
            }else{
                System.out.println("no query built");
            }
        }
        resetSearchFields();
    }

    private void resetSearchFields(){
        firstNameSearchField.setText("");
        lastNameSearchField.setText("");
        phoneNumberSearchField.setText("");
        emailSearchField.setText("");
        dobSearchPicker.setValue(null);
        typeSearchPicker.setValue(null);
    }

    private boolean buildQuery(HashMap<Integer, Object> statementValues){
        int pos = 1;
        boolean appended = false;
        query.append(" WHERE");
        String email = emailSearchField.getText();
        String phoneNumber = phoneNumberSearchField.getText();
        String firstName = firstNameSearchField.getText();
        String lastName = lastNameSearchField.getText();
        LocalDate dob = dobSearchPicker.getValue();
        String type = typeSearchPicker.getValue();

        if(Validators.isValidEmailAddress(email)){
            if(appended){
                query.append(" AND");
            }
            query.append(" email=?");
            appended = true;
            statementValues.put(pos, email);
            pos++;
        }
        if(Validators.isValidPhoneNumber(phoneNumber)){
            if(appended){
                query.append(" AND");
            }
            query.append(" phone=?");
            appended = true;
            statementValues.put(pos, phoneNumber);
            pos++;
        }
        if(!firstName.equals("")){
            if(appended){
                query.append(" AND");
            }
            query.append(" fname=?");
            appended=true;
            statementValues.put(pos, firstName);
            pos++;
        }
        if(!lastName.equals("")){
            if(appended){
                query.append(" AND");
            }
            query.append(" lname=?");
            appended = true;
            statementValues.put(pos, lastName);
            pos++;
        }
        if(dob != null){
            if (appended) {
                query.append(" AND");
            }
            query.append(" dob=?");
            appended = true;
            statementValues.put(pos, dob);
            pos++;
        }
        if(type != null && !type.equals("")){
            if (appended) {
                query.append(" AND");
            }
            query.append(" usertype=?");
            appended = true;
            statementValues.put(pos, type);
        }
        query.append(";");

        return appended;
    }

    private void addUsers(ResultSet rs) throws SQLException, NoSuchAlgorithmException {
        do{
            User user = new User();
            user.setFirstName(rs.getString("fname"));
            user.setLastName(rs.getString("lname"));
            user.setEmail(rs.getString("email"));
            user.setPhoneNumber(rs.getString("phone"));
            user.setType(rs.getString("usertype"));
            user.setDob(rs.getDate("dob").toLocalDate());
            user.setPassword(rs.getString("password"));
            usersList.add(user);
        } while(rs.next());
    }
}
