package photocol.layer.store;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.DataBase.Method.InitDB;

import static photocol.definitions.ACLEntry.ACLOperation.*;
import static photocol.definitions.exception.HttpMessageException.Error.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionStore {

    private Connection conn;
    public CollectionStore() {
        this.conn = new InitDB().initialDB("photocol");
    }

    /**
     * Get all collections that user has permissions to access
     * @param uid       uid of accessor
     * @param username  username of user
     * @return          list of collections accessible by user
     * @throws HttpMessageException on failure
     */
    public List<PhotoCollection> getUserCollections(int uid, String username) throws HttpMessageException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT username as owner, pub, name, uri, acl1.role " +
                    "FROM collection " +
                    "INNER JOIN acl AS acl1 ON collection.cid=acl1.cid " +
                    "INNER JOIN acl AS acl2 ON collection.cid=acl2.cid " +
                    "INNER JOIN user ON acl2.uid=user.uid " +
                    "WHERE acl1.uid=? AND acl2.role=0");
            stmt.setInt(1, uid);

            ResultSet rs = stmt.executeQuery();
            List<PhotoCollection> photoCollections = new ArrayList<>();
            while(rs.next()) {
                // here, aclList just indicates the current user's role and owner only, not full aclList
                List<ACLEntry> aclList = new ArrayList<>();
                aclList.add(new ACLEntry(username, rs.getInt("role")));
                aclList.add(new ACLEntry(rs.getString("owner"), ACLEntry.Role.ROLE_OWNER));

                photoCollections.add(new PhotoCollection(rs.getBoolean("pub"), rs.getString("name"), aclList));
            }
            return photoCollections;
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Check if collection exists and is viewable by uid; return cid if it does or -1 if not
     * @param uid                   uid of viewer
     * @param collectionOwnerUid    collection owner uid
     * @param collectionUri         collection uri
     * @return                      cid if exists; -1 if not
     * @throws HttpMessageException on failure
     */
    public int checkIfCollectionExists(int uid, int collectionOwnerUid, String collectionUri)
            throws HttpMessageException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT cid FROM acl WHERE uid=? AND cid in " +
                    "(SELECT cid FROM collection WHERE cid IN " +
                    "(SELECT cid FROM acl WHERE role=? AND uid=?) AND uri=?)");
            stmt.setInt(1, uid);
            stmt.setInt(2, ACLEntry.Role.ROLE_OWNER.toInt());
            stmt.setInt(3, collectionOwnerUid);
            stmt.setString(4, collectionUri);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                return -1;

            return rs.getInt("cid");
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Insert collection into db
     * @param uid           uid of owner
     * @param collection    photocollection object
     * @return              true on success
     * @throws HttpMessageException on failure
     */
    public boolean createCollection(int uid, PhotoCollection collection) throws HttpMessageException {
        try {
            PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO collection (name, pub, uri) " +
                            "VALUES (?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            stmt1.setString(1, collection.name);
            stmt1.setBoolean(2, collection.isPublic);
            stmt1.setString(3, collection.uri);
            stmt1.executeUpdate();

            // TODO: check that these validations are correct (i.e., duplicate checks)
            ResultSet keyResultSet = stmt1.getGeneratedKeys();
            if(!keyResultSet.next())
                throw new HttpMessageException(401, COLLECTION_NAME_INVALID);
            int cid = keyResultSet.getInt("cid");

            PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO acl (cid, uid, role) VALUES (?, ?, ?);");
            stmt2.setInt(1, cid);
            stmt2.setInt(2, uid);
            stmt2.setInt(3, ACLEntry.Role.ROLE_OWNER.toInt());
            stmt2.executeUpdate();

            return true;
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Get details and photos in collection
     * @param cid   collection cid
     * @return      list of photo objects from collection
     * @throws HttpMessageException on failure
     */
    public PhotoCollection getCollection(int cid) throws HttpMessageException {
        try {
            // get collection details
            PreparedStatement stmt = conn.prepareStatement("SELECT name, uri, pub FROM collection WHERE cid=?");
            stmt.setInt(1, cid);
            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                throw new HttpMessageException(401, COLLECTION_NOT_FOUND);
            String collectionName = rs.getString("name");
            String collectionUri = rs.getString("uri");
            boolean collectionIsPublic = rs.getBoolean("pub");

            // get acl list
            stmt = conn.prepareStatement("SELECT username, role FROM " +
                    "(SELECT uid, role FROM acl WHERE cid=?) as uidroles " +
                    "INNER JOIN user ON uidroles.uid=user.uid");
            stmt.setInt(1, cid);
            rs = stmt.executeQuery();
            List<ACLEntry> aclList = new ArrayList<>();
            while(rs.next())
                aclList.add(new ACLEntry(rs.getString("username"), rs.getInt("role")));

            // get photo list
            stmt = conn.prepareStatement("SELECT uri,description,upload_date FROM photo " +
                    "INNER JOIN icj ON icj.pid=photo.pid WHERE icj.cid=?");
            stmt.setInt(1, cid);
            rs = stmt.executeQuery();
            List<Photo> photoList = new ArrayList<>();
            while(rs.next())
                photoList.add(new Photo(rs.getString("uri"), rs.getString("description"), rs.getDate("upload_date")));

            PhotoCollection photoCollection = new PhotoCollection(collectionIsPublic, collectionName, collectionUri, aclList);
            photoCollection.setPhotos(photoList);

            return photoCollection;
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Check if user has access to collection; return role if applicable.
     * @param uid   accessor uid
     * @param cid   collection cid
     * @return      role if user has access
     * @throws HttpMessageException on failure or if user doesn't have access
     */
    public int getUserCollectionRole(int uid, int cid) throws HttpMessageException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM acl WHERE uid=? AND cid=?");
            stmt.setInt(1, uid);
            stmt.setInt(2, cid);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS);

            return rs.getInt("role");
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Add photo to collection
     * @param cid   collection cid
     * @param pid   photo pid
     * @param isAdd true if adding photo, false if removing
     * @return      true on success
     * @throws HttpMessageException on failure on failure on failure on failure
     */
    public boolean addRemoveImage(int cid, int pid, boolean isAdd) throws HttpMessageException {
        try {
            // try to insert image (will fail if not unique) or remove image
            PreparedStatement stmt;
            stmt = conn.prepareStatement(isAdd
                    ? "INSERT INTO icj (cid, pid) VALUES (?, ?);"
                    : "DELETE FROM icj WHERE cid=? AND pid=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, pid);

            stmt.executeUpdate();
            return true;
        } catch(SQLIntegrityConstraintViolationException err) {
            // image already exists in collection
            throw new HttpMessageException(401, IMAGE_EXISTS_IN_COLLECTION);
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Get acl list (for checking permissions on update)
     * @param cid   collection cid
     * @return      list of aclentry objects
     * @throws HttpMessageException on error
     */
    public List<ACLEntry> getAclList(int cid) throws HttpMessageException {
        try {
            // create a map of current acl list to perform checks against
            PreparedStatement stmt = conn.prepareStatement("SELECT uid, role FROM acl WHERE cid=?");
            stmt.setInt(1, cid);
            ResultSet rs = stmt.executeQuery();

            List<ACLEntry> aclList = new ArrayList<>();
            while(rs.next())
                aclList.add(new ACLEntry(rs.getInt("uid"), rs.getInt("role")));

            return aclList;
        } catch(SQLException err) {
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Update collection attributes or acl
     * @param cid               collection cid
     * @param photoCollection   attributes to change
     * @param ownerUid          owner uid
     * @return                  true on success
     * @throws HttpMessageException on failure
     */
    public boolean update(int cid, PhotoCollection photoCollection, int ownerUid) throws HttpMessageException {
        try {
            // update name and uri, if specified
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

                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO acl (cid, uid, role) VALUES (?, ?, ?)");
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE acl SET role=? WHERE cid=? AND uid=?");
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM acl WHERE cid=? and uid=?");

                for (ACLEntry entry : photoCollection.aclList) {
                    // shouldn't happen, just an extra check to prevent NullPointerException
                    if(entry.operation==null)
                        throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "UNRECOGNIZED ACL ACTION");

                    if(entry.operation==OP_INSERT || entry.operation==OP_INSERT_OWNER) {
                        insertStmt.setInt(1, cid);
                        insertStmt.setInt(2, entry.uid);
                        insertStmt.setInt(3, entry.role.toInt());
                        insertStmt.addBatch();
                    }

                    if(entry.operation==OP_UPDATE || entry.operation==OP_UPDATE_OWNER) {
                        updateStmt.setInt(2, cid);
                        updateStmt.setInt(3, entry.uid);
                        updateStmt.setInt(1, entry.role.toInt());
                        updateStmt.addBatch();
                    }

                    if(entry.operation==OP_DELETE) {
                        deleteStmt.setInt(1, cid);
                        deleteStmt.setInt(2, entry.uid);
                        deleteStmt.addBatch();

                        // TODO: remove all photos in collection owned by the person?
                    }

                    // make self a viewer if new owner promoted
                    if(entry.operation==OP_UPDATE_OWNER || entry.operation==OP_INSERT_OWNER) {
                        updateStmt.setInt(2, cid);
                        updateStmt.setInt(3, ownerUid);
                        updateStmt.setInt(1, ACLEntry.Role.ROLE_EDITOR.toInt());
                        updateStmt.addBatch();
                    }
                }

                // execute all queries at once; rollback all if any fail
                insertStmt.executeBatch();
                updateStmt.executeBatch();
                deleteStmt.executeBatch();
                conn.commit();
                conn.setAutoCommit(true);
            }

            return true;
        } catch(SQLIntegrityConstraintViolationException err) {
            // if try to insert duplicate acl records for same db; this will be eliminated when stricter checks
            // on ACL list on service layer are implemented
            throw new HttpMessageException(401, COLLECTION_NAME_INVALID);
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Delete collection and all photos associated with it. Assuming permissions of deleter already verified.
     * @param cid   cid of collection to delete
     * @return      true on success
     * @throws HttpMessageException on failure
     */
    public boolean deleteCollection(int cid) throws HttpMessageException {
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE collection, icj, acl FROM collection " +
                    "LEFT JOIN icj ON icj.cid=collection.cid " +
                    "LEFT JOIN acl ON acl.cid=collection.cid " +
                    "WHERE collection.cid=?");
            stmt.setInt(1, cid);
            stmt.executeUpdate();

            return true;
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }
}
