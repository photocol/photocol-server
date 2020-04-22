package photocol.layer.handler;

import photocol.definitions.exception.HttpMessageException;
import photocol.layer.store.UserStore;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;

public class SearchHandler {

    UserStore userStore;
    public SearchHandler(UserStore userStore) {
        this.userStore = userStore;
    }

    /**
     * Search users
     * @param req   spark request object
     * @param res   spark response object
     * @return      list of usernames (strings) matching query
     * @throws HttpMessageException on failure
     */
    public List<String> searchUsers(Request req, Response res) throws HttpMessageException {
        String userQuery = req.params("userquery");
        if(userQuery==null || userQuery.length()==0)
            return new ArrayList<>();

        return this.userStore.searchUsers(userQuery);
    }

}
