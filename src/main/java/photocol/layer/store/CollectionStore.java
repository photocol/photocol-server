package photocol.layer.store;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.response.StatusResponse;
import static photocol.definitions.response.StatusResponse.Status.*;
import photocol.layer.DataBase.Method.InitDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CollectionStore {

    private Connection conn;
    public CollectionStore() {
        this.conn = new InitDB().initialDB("photocol");
    }

    // get all collections that user has permissions to
    public StatusResponse<List<PhotoCollection>> getUserCollections(int uid) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT pub, name, uri, role FROM collection " +
                    "INNER JOIN acl ON collection.cid=acl.cid " +
                    "WHERE acl.uid=?");
            stmt.setInt(1, uid);

            ResultSet rs = stmt.executeQuery();
            List<PhotoCollection> photoCollections = new ArrayList<>();
            while(rs.next()) {
                // here, aclList just indicates the current user's role
                List<ACLEntry> aclList = new ArrayList<>();
                aclList.add(new ACLEntry("current user", rs.getInt("role")));
                photoCollections.add(new PhotoCollection(rs.getBoolean("pub"), rs.getString("name"), aclList));
            }
            return new StatusResponse<>(STATUS_OK, photoCollections);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse<>(STATUS_MISC);
        }
    }

    // check if collection exists and return cid if it does; actually checks uri, not name
    public StatusResponse<Integer> checkIfCollectionExists(int uid, String collectionUri) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT collection.cid FROM collection " +
                    "INNER JOIN acl ON collection.cid=acl.cid WHERE acl.uid=? AND collection.uri=?");
            stmt.setInt(1, uid);
            stmt.setString(2, collectionUri);

            System.err.println(stmt.toString());

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                return new StatusResponse<>(STATUS_COLLECTION_NOT_FOUND);

            return new StatusResponse<>(STATUS_OK, rs.getInt("cid"));
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_COLLECTION_NOT_FOUND);
        }
    }

    // insert collection into db
    public StatusResponse createCollection(int uid, PhotoCollection collection) {
        try {
            PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO collection (name, pub, uri) VALUES (?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            stmt1.setString(1, collection.name);
            stmt1.setBoolean(2, collection.isPublic);
            stmt1.setString(3, collection.uri);
            stmt1.executeUpdate();

            ResultSet keyResultSet = stmt1.getGeneratedKeys();
            if(!keyResultSet.next())
                return new StatusResponse(STATUS_COLLECTION_NAME_INVALID);
            int cid = keyResultSet.getInt("cid");

            PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO acl (cid, uid, role) VALUES (?, ?, ?);");
            stmt2.setInt(1, cid);
            stmt2.setInt(2, uid);
            stmt2.setInt(3, ACLEntry.Role.ROLE_OWNER.toInt());
            stmt2.executeUpdate();

            return new StatusResponse(STATUS_OK);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_COLLECTION_NAME_INVALID);
        }
    }

    // get photos in collection
    public StatusResponse<List<Photo>> getCollectionPhotos(int cid) {
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
