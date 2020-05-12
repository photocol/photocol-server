package photocol.layer.store;

import photocol.definitions.Photo;
import photocol.definitions.exception.HttpMessageException;
import photocol.util.DBConnectionClient;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class PhotoStore {
    private static DataSource dbcp;

    public PhotoStore(DBConnectionClient dbClient) {
        dbcp = dbClient.getDataSource();
    }

    /**
     * Check if photo uri is taken; used when generating random photo uris
     * @param uri   uri to check
     * @return      whether photo uri is taken
     * @throws HttpMessageException on error
     */
    public boolean checkIfPhotoExists(String uri) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT pid FROM photocol.photo WHERE uri=?");
            stmt.setString(1, uri);

            ResultSet rs = stmt.executeQuery();
            boolean next = rs.next();
            conn.close();
            return next;
        } catch(Exception err) {
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
     * Get all photos that belong to a user
     * @param uid   user uid
     * @return      list of photos that belong to user
     * @throws HttpMessageException on failure
     */
    public List<Photo> getUserPhotos(int uid) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uri, filename, caption, upload_date, width, height " +
                    "FROM photocol.photo WHERE uid=?");
            stmt.setInt(1, uid);

            ResultSet rs = stmt.executeQuery();
            List<Photo> userPhotos = new ArrayList<>();
            while(rs.next()) {
                Photo.PhotoMetadata photoMetadata = new Photo.PhotoMetadata();
                photoMetadata.width = rs.getInt("width");
                photoMetadata.height = rs.getInt("height");
                userPhotos.add(new Photo(rs.getString("uri"),
                                         rs.getString("filename"),
                                         rs.getString("caption"),
                                         rs.getDate("upload_date"),
                                         photoMetadata));
            }
            conn.close();
            return userPhotos;
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
     * Check to see if user owns photo or has access to a collection that contains the photo, returning pid on success.
     * If checkOwner is set to true, will throw an exception if user is not the owner.
     * @param uri   uri of photo
     * @param uid   uid of user attempting to access photo
     * @return      pid on success
     * @throws HttpMessageException on failure
     */
    public int checkPhotoPermissions(String uri, int uid, boolean checkOwner) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            // check if user owns the photo
            PreparedStatement stmt = conn.prepareStatement("SELECT pid FROM photocol.photo WHERE uid=? AND uri=?");
            stmt.setInt(1, uid);
            stmt.setString(2, uri);

            ResultSet rs = stmt.executeQuery();
            if(rs.next())
            {
                conn.close();
                return rs.getInt("pid");
            }

            if(checkOwner)
            {
                conn.close();
                throw new HttpMessageException(401, NOT_PHOTO_OWNER);
            }

            // check if the photo is a profile picture (always public)
            stmt = conn.prepareStatement("SELECT pid FROM photo INNER JOIN user ON photo.pid=user.profile_photo " +
                    "WHERE photo.uri=?");
            stmt.setString(1, uri);
            rs = stmt.executeQuery();
            if(rs.next()) {
                conn.close();
                return rs.getInt("pid");
            }

            // check if user is in one of the collections that contains the photo
            // TODO: check if this join is actually correct; not sure how to use joins
            // TODO: can probably simplify to one join if duplicate photouri to icj table
            // TODO: check public attribute of collections
            stmt = conn.prepareStatement("SELECT photocol.photo.pid " +
                    "FROM photocol.acl " +
                    "INNER JOIN photocol.icj ON photocol.acl.cid=photocol.icj.cid " +
                    "INNER JOIN photocol.photo ON photocol.photo.pid=photocol.icj.pid " +
                    "WHERE photocol.acl.uid=? AND photocol.photo.uri=?");
            stmt.setInt(1, uid);
            stmt.setString(2, uri);

            rs = stmt.executeQuery();
            if(rs.next()) {
                conn.close();
                return rs.getInt("pid");
            }

            // check if photo is in a public collection
            stmt = conn.prepareStatement("SELECT photo.pid FROM photo " +
                    "INNER JOIN icj ON photo.pid=icj.pid " +
                    "INNER JOIN collection ON collection.cid=icj.cid " +
                    "WHERE collection.pub=? AND photo.uri=?");
            stmt.setInt(1, 1);
            stmt.setString(2, uri);
            rs = stmt.executeQuery();
            if(rs.next()) {
                conn.close();
                return rs.getInt("pid");
            }

            // check if photo is the cover photo of a discoverable collection
            stmt = conn.prepareStatement("SELECT photo.pid FROM photo " +
                    "INNER JOIN collection ON collection.cover_photo=photo.pid " +
                    "WHERE collection.pub=? AND photo.uri=?");
            stmt.setInt(1, 2);
            stmt.setString(2, uri);
            rs = stmt.executeQuery();
            if(rs.next()) {
                conn.close();
                return rs.getInt("pid");
            }

            // not found
            conn.close();
            throw new HttpMessageException(404, IMAGE_NOT_FOUND);
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
     * Create photo in database
     * @param photo     photo object
     * @param uid       uid of owner
     * @return          true on success
     * @throws HttpMessageException on failure
     */
    public boolean createPhoto(Photo photo, int uid) throws HttpMessageException {
        // assume uri is already checked to be unique in service layer
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO photocol.photo " +
                    "(uri, upload_date, mime_type, filename, width, height, uid, orig_uid) VALUES(?,?,?,?,?,?,?,?)");
            stmt.setString(1, photo.uri);
            stmt.setDate(2, new Date(photo.uploadDate.getTime()));
            stmt.setString(3, photo.metadata.mimeType);
            stmt.setString(4, photo.filename);
            stmt.setInt(5, photo.metadata.width);
            stmt.setInt(6, photo.metadata.height);
            stmt.setInt(7, uid);
            stmt.setInt(8, uid);

            // this should never happen
            if(stmt.executeUpdate()<1)
            {
                conn.close();
                throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
            }
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
     * Checks if photo is in specified collection
     * @param photouri  uri of photo to check
     * @param cid       collection to check
     * @return          whether photo is in specified collection
     * @throws HttpMessageException on failure
     */
    public boolean checkIfPhotoInCollection(String photouri, int cid) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT photo.pid FROM photo " +
                    "INNER JOIN icj ON photo.pid=icj.pid " +
                    "WHERE uri=? AND cid=?");
            stmt.setString(1, photouri);
            stmt.setInt(2, cid);

            ResultSet rs = stmt.executeQuery();
            boolean next = rs.next();
            conn.close();
            return next;
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
     * Deletes user photo. Assumes that it has already been checked that user owns photo.
     * @param pid   photo pid
     * @return      true on success
     * @throws HttpMessageException on error or not owner
     */
    public boolean deletePhoto(int pid) throws HttpMessageException {
        Connection conn = null;
        try {
            conn = dbcp.getConnection();
        } catch (SQLException err) {
            System.err.println("Error connecting to database.");
            err.printStackTrace();
        }try {
            // deleting from photo should cascade down to other tables gracefully
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM photo WHERE photo.pid=?");
            stmt.setInt(1, pid);
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

    /**
     * Get photo details
     * @param pid   pid of photo
     * @return      photo details
     * @throws HttpMessageException on failure
     */
    public Photo details(int pid) throws HttpMessageException {
        try (Connection conn = dbcp.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT uri, filename, mime_type, upload_date, caption, " +
                    "uid, orig_uid, exposure_time, f_number, iso, width, height, capture_date FROM photo " +
                    "WHERE pid=?");
            stmt.setInt(1, pid);

            ResultSet rs = stmt.executeQuery();
            if(!rs.next())
                throw new HttpMessageException(401, IMAGE_NOT_FOUND);

            Photo.PhotoMetadata photoMetadata = new Photo.PhotoMetadata();
            photoMetadata.mimeType = rs.getString("mime_type");
            photoMetadata.exposureTime = rs.getDouble("exposure_time");
            photoMetadata.fNumber = rs.getDouble("f_number");
            photoMetadata.iso = rs.getInt("iso");
            photoMetadata.width = rs.getInt("width");
            photoMetadata.height = rs.getInt("height");
            photoMetadata.captureDate = rs.getDate("capture_date");
            Photo photo = new Photo(rs.getString("uri"),
                    rs.getString("filename"),
                    rs.getString("caption"),
                    rs.getDate("upload_date"),
                    photoMetadata);
            return photo;
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }
}
