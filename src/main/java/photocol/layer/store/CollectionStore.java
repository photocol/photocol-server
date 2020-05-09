package photocol.layer.store;

import photocol.definitions.ACLEntry;
import photocol.definitions.Photo;
import photocol.definitions.PhotoCollection;
import photocol.definitions.exception.HttpMessageException;
import photocol.util.DBConnectionClient;

import javax.sql.DataSource;

import static photocol.definitions.ACLEntry.ACLOperation.*;
import static photocol.definitions.exception.HttpMessageException.Error.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionStore {

    private DataSource dbcp;
    public CollectionStore(DBConnectionClient dbClient) {
        dbcp = dbClient.getDataSource();
    }

    /**
     * Get all collections that user has permissions to access
     * @param uid       uid of accessor
     * @param username  username of user
     * @return          list of collections accessible by user
     * @throws HttpMessageException on failure
     */
    public List<PhotoCollection> getUserCollections(int uid, String username) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }try {
            PreparedStatement stmt = conn.prepareStatement("SELECT username as owner, pub, name, uri, description, " +
                    "cover_photo, acl1.role " +
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

                photoCollections.add(new PhotoCollection(rs.getBoolean("pub"), rs.getString("name"), aclList, "", ""));
            }
            conn.close();
            return photoCollections;
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
    public int checkIfCollectionExists(int uid, int collectionOwnerUid, String collectionUri) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
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
            {
                conn.close();
                return -1;
            }
            conn.close();
            return rs.getInt("cid");
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
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
            {
                conn.close();
                throw new HttpMessageException(401, COLLECTION_NAME_INVALID);
            }
            int cid = keyResultSet.getInt("cid");

            PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO acl (cid, uid, role) VALUES (?, ?, ?);");
            stmt2.setInt(1, cid);
            stmt2.setInt(2, uid);
            stmt2.setInt(3, ACLEntry.Role.ROLE_OWNER.toInt());
            stmt2.executeUpdate();
            conn.close();
            return true;
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            // get collection details
            PreparedStatement stmt = conn.prepareStatement("SELECT name, pub, description, photo.uri as cover_photo " +
                    "FROM collection " +
                    "LEFT JOIN photo ON cover_photo=pid " +
                    "WHERE cid=?");
            stmt.setInt(1, cid);
            ResultSet rs = stmt.executeQuery();

            if(!rs.next())
            {
                conn.close();
                throw new HttpMessageException(401, COLLECTION_NOT_FOUND);
            }
            String collectionName = rs.getString("name");
            boolean collectionIsPublic = rs.getBoolean("pub");
            String description = rs.getString("description");
            String coverPhotoUri = rs.getString("cover_photo");

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
            stmt = conn.prepareStatement("SELECT uri, caption, filename, upload_date, width, height FROM photo " +
                    "INNER JOIN icj ON icj.pid=photo.pid WHERE icj.cid=?");
            stmt.setInt(1, cid);
            rs = stmt.executeQuery();
            List<Photo> photoList = new ArrayList<>();
            while(rs.next()) {
                Photo.PhotoMetadata photoMetadata = new Photo.PhotoMetadata();
                photoMetadata.width = rs.getInt("width");
                photoMetadata.height = rs.getInt("height");
                photoList.add(new Photo(rs.getString("uri"),
                                        rs.getString("filename"),
                                        rs.getString("caption"),
                                        rs.getDate("upload_date"),
                                        photoMetadata));
            }

            PhotoCollection photoCollection = new PhotoCollection(collectionIsPublic, collectionName,
                    aclList, coverPhotoUri, description);
            photoCollection.setPhotos(photoList);
            conn.close();
            return photoCollection;
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT role FROM acl WHERE uid=? AND cid=?");
            stmt.setInt(1, uid);
            stmt.setInt(2, cid);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
            {
                conn.close();
                throw new HttpMessageException(401, INSUFFICIENT_COLLECTION_PERMISSIONS);
            }
            conn.close();
            return rs.getInt("role");
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            // try to insert image (will fail if not unique) or remove image
            PreparedStatement stmt;
            stmt = conn.prepareStatement(isAdd
                    ? "INSERT INTO icj (cid, pid) VALUES (?, ?);"
                    : "DELETE FROM icj WHERE cid=? AND pid=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, pid);

            stmt.executeUpdate();
            conn.close();
            return true;
        } catch(SQLIntegrityConstraintViolationException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            // image already exists in collection
            throw new HttpMessageException(401, IMAGE_EXISTS_IN_COLLECTION);
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            // create a map of current acl list to perform checks against
            PreparedStatement stmt = conn.prepareStatement("SELECT uid, role FROM acl WHERE cid=?");
            stmt.setInt(1, cid);
            ResultSet rs = stmt.executeQuery();

            List<ACLEntry> aclList = new ArrayList<>();
            while(rs.next())
                aclList.add(new ACLEntry(rs.getInt("uid"), rs.getInt("role")));
            conn.close();
            return aclList;
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Update collection attributes or acl. Assume validation already completed
     * @param cid               collection cid
     * @param photoCollection   attributes to change
     * @param ownerUid          owner uid
     * @return                  true on success
     * @throws HttpMessageException on failure
     */
    public boolean update(int cid, PhotoCollection photoCollection, int ownerUid) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            // update name and uri, if specified
            if (photoCollection.name != null && photoCollection.name.length() > 0) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE collection SET name=?, uri=? WHERE cid=?");
                stmt.setString(1, photoCollection.name);
                stmt.setString(2, photoCollection.uri);
                stmt.setInt(3, cid);
                stmt.executeUpdate();
            }

            // update description, if specified
            if(photoCollection.description != null) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE collection SET description=? WHERE cid=?");
                stmt.setString(1, photoCollection.description);
                stmt.setInt(2, cid);
                stmt.executeUpdate();
            }

            // update cover photo, if specified
            if(photoCollection.coverPhotoUri != null) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE collection " +
                        "SET cover_photo=(SELECT pid FROM photo WHERE uri=?) " +
                        "WHERE cid=?");
                stmt.setString(1, photoCollection.coverPhotoUri);
                stmt.setInt(2, cid);
                stmt.executeUpdate();
            }

            // update acl list; do all as one transaction
            if (photoCollection.aclList.size() > 0) {
                conn.setAutoCommit(false);

                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO acl (cid, uid, role) VALUES (?, ?, ?)");
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE acl SET role=? WHERE cid=? AND uid=?");
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM acl WHERE cid=? and uid=?");

                for (ACLEntry entry : photoCollection.aclList) {
                    // shouldn't happen, just an extra check to prevent NullPointerException
                    if(entry.operation==null)
                    {
                        conn.close();
                        throw new HttpMessageException(400, ILLEGAL_ACL_ACTION, "UNRECOGNIZED ACL ACTION");
                    }

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
            conn.close();
            return true;
        } catch(SQLIntegrityConstraintViolationException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            // if try to insert duplicate acl records for same db; this will be eliminated when stricter checks
            // on ACL list on service layer are implemented
            throw new HttpMessageException(401, COLLECTION_NAME_INVALID);
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
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
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            // delete collection; should gracefully cascade into deleting icj and acl entries
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM collection WHERE collection.cid=?");
            stmt.setInt(1, cid);
            stmt.executeUpdate();
            conn.close();
            return true;
        } catch(SQLException err) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                System.err.println("Error connecting to database.");
                throwables.printStackTrace();
            }
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }
}
