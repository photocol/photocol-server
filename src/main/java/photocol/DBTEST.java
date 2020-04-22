package photocol;
import photocol.definitions.User;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.UserStore;
//
import java.sql.*;
public class DBTEST {
    public static void main(String[] Args) throws SQLException, HttpMessageException {
        UserStore udb = new UserStore();
        User Af = new User("victorgggggzh716@gmail.com", "victooorra", "pw123");
        udb.createUser(Af);
        udb.checkCredentials(Af);


    }
}
