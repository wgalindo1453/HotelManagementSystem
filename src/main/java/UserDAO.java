

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;



public interface UserDAO {

    public int add(User user)
            throws SQLException, ClassNotFoundException;
    public void delete(String email)
            throws SQLException;
    public User getUser(String EMAIL)
            throws SQLException, NoSuchAlgorithmException;
    public List<User> getUsers()
            throws SQLException, NoSuchAlgorithmException;
    public void update(User user)
            throws SQLException;
}