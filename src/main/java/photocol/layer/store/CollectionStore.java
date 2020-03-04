package photocol.layer.store;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.response.StatusResponse;
import static photocol.definitions.response.StatusResponse.Status.*;
import photocol.layer.DataBase.Method.InitDB;

import java.sql.*;
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

    // check if collection exists and is viewable by uid; return cid if it does; actually checks uri, not name
    public StatusResponse<Integer> checkIfCollectionExists(int uid, int collectionOwnerUid, String collectionUri) {
        try {
            // OLD QUERY: remove later
//            PreparedStatement stmt = conn.prepareStatement("SELECT collection.cid FROM collection " +
//                    "INNER JOIN acl ON collection.cid=acl.cid WHERE acl.uid=? AND acl.role=? AND collection.uri=?");
//            stmt.setInt(1, collectionOwnerUid);
//            stmt.setInt(2, 0);
//            stmt.setString(3, collectionUri);

            PreparedStatement stmt = conn.prepareStatement("SELECT collection.cid, uid FROM collection " +
                    "INNER JOIN acl ON collection.cid=acl.cid " +
                    "WHERE ((uid=? and role=?) or uid=?) AND collection.uri=?");
            stmt.setInt(1, collectionOwnerUid);
            stmt.setInt(2, 0);
            stmt.setInt(3, uid);
            stmt.setString(4, collectionUri);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                return new StatusResponse<>(STATUS_COLLECTION_NOT_FOUND);

            // check that both uid and collectionOwnerUid are in the returned set
            // TODO: very janky but no time -- fix this and maybe query later
            boolean uidFound = false, collectionOwnerUidFound = false;
            int cid = rs.getInt("cid");
            if(rs.getInt("uid")==uid) uidFound = true;
            if(rs.getInt("uid")==collectionOwnerUid) collectionOwnerUidFound = true;
            if(rs.next()) {
                if(rs.getInt("uid")==uid) uidFound = true;
                if(rs.getInt("uid")==collectionOwnerUid) collectionOwnerUidFound = true;
            }

            System.out.println("checking 1 2 3 " + uidFound + " " + collectionOwnerUidFound);

            return new StatusResponse<>(STATUS_OK, cid);
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

    // add photo to collection
    public StatusResponse addImage(int cid, int pid) {
        try {
            // try to insert image; will fail if not unique
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO icj (cid, pid) VALUES (?, ?);");
            stmt.setInt(1, cid);
            stmt.setInt(2, pid);

            stmt.executeUpdate();
            return new StatusResponse(STATUS_OK);
        } catch(SQLIntegrityConstraintViolationException err) {
            // image already exists in collection
            return new StatusResponse(STATUS_IMAGE_EXISTS_IN_COLLECTION);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_MISC);
        }
    }

    // update collection
    public StatusResponse update(int cid, PhotoCollection photoCollection) {
        try {
            // update name and uri
            if (photoCollection.name != null && photoCollection.name.length() > 0) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE collection SET name=?, uri=? WHERE cid=?");
                stmt.setString(1, photoCollection.name);
                stmt.setString(2, photoCollection.uri);
                stmt.setInt(3, cid);
                stmt.executeUpdate();
            }

            // update acl list; do all as one callback
            if (photoCollection.aclList.size() > 0) {
                conn.setAutoCommit(false);

                for (ACLEntry entry : photoCollection.aclList) {
                    PreparedStatement stmt;
                    if (entry.role != ACLEntry.Role.ROLE_NONE) {
                        stmt = conn.prepareStatement("INSERT INTO acl (cid, uid, role) VALUES (?, ?, ?)");
                        stmt.setInt(1, cid);
                        stmt.setInt(2, entry.uid);
                        stmt.setInt(3, entry.role.toInt());
                    } else {
                        stmt = conn.prepareStatement("REMOVE FROM acl WHERE cid=? AND uid=?");
                        stmt.setInt(1, cid);
                        stmt.setInt(2, entry.uid);
                    }
                    stmt.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(true);
            }

            return new StatusResponse(STATUS_OK);
        } catch(SQLIntegrityConstraintViolationException err) {
            // if try to insert duplicate acl records for same db; this will be eliminated when stricter checks
            // on ACL list on service layer are implemented
            return new StatusResponse(STATUS_MISC);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_MISC);
        }
    }
}
