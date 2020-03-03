package photocol.layer.store;

import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.response.StatusResponse;
import static photocol.definitions.response.StatusResponse.Status.*;
import photocol.layer.DataBase.Method.InitDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CollectionStore {

    private Connection conn;
    public CollectionStore() {
        this.conn = new InitDB().initialDB("photocol");
    }

    // check if collection exists and return cid if it does
    public StatusResponse<Integer> checkIfCollectionExists(int uid, String collectionName) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT collection.cid FROM collection " +
                    "INNER JOIN acl ON collection.uid=acl.uid WHERE acl.uid=? AND collection.name=?");
            stmt.setInt(1, uid);
            stmt.setString(2, collectionName);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                return new StatusResponse<>(STATUS_COLLECTION_NOT_FOUND);

            return new StatusResponse<>(STATUS_OK, rs.getInt("cid"));
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_COLLECTION_NOT_FOUND);
        }
    }

    public StatusResponse createCollection(int uid, PhotoCollection collection) {
        return new StatusResponse(STATUS_COLLECTION_NAME_INVALID);
    }

    public StatusResponse<List<Photo>> getCollectionPhotos(int uid, int cid) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uri,description,upload_date FROM photo " +
                    "INNER JOIN icj ON icj.pid=photo.pid WHERE icj.cid=?");
            stmt.setInt(1, cid);

            ResultSet rs = stmt.executeQuery();
            List<Photo> photoList = new ArrayList<>();
            while(rs.next())
                photoList.add(new Photo(rs.getString("uri"), rs.getString("description"), rs.getDate("upload_date")));

            return new StatusResponse(STATUS_OK, photoList);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_COLLECTION_NOT_FOUND);
        }
    }

    // get user's access role in a collection
    public StatusResponse<Integer> getUserCollectionRole(int uid, int cid) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM acl WHERE uid=? AND cid=?");
            stmt.setInt(1, uid);
            stmt.setInt(2, cid);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                return new StatusResponse<>(STATUS_INSUFFICIENT_COLLECTION_PERMISSIONS);

            return new StatusResponse<>(STATUS_OK, rs.getInt("role"));
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse<>(STATUS_INSUFFICIENT_COLLECTION_PERMISSIONS);
        }
    }
}
