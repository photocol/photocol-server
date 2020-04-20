package photocol.layer.store;

import photocol.definitions.Photo;
import photocol.definitions.exception.HttpMessageException;
import photocol.layer.DataBase.Method.InitDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static photocol.definitions.exception.HttpMessageException.Error.*;

public class PhotoStore {

    private Connection conn;
    public PhotoStore() {
        // TODO: change this
        this.conn = new InitDB().initialDB("photocol");
    }

    /**
     * Check if photo uri is taken; used when generating random photo uris
     * @param uri   uri to check
     * @return      whether photo uri is taken
     * @throws HttpMessageException on error
     */
    public boolean checkIfPhotoExists(String uri) throws HttpMessageException {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT pid FROM photocol.photo WHERE uri=?");
            stmt.setString(1, uri);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch(Exception err) {
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
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uri,description,upload_date " +
                    "FROM photocol.photo WHERE uid=?");
            stmt.setInt(1, uid);

            ResultSet rs = stmt.executeQuery();
            List<Photo> userPhotos = new ArrayList<>();
            while(rs.next())
                userPhotos.add(new Photo(rs.getString("uri"),
                                         rs.getString("description"),
                                         rs.getDate("upload_date")));
            return userPhotos;
        } catch(SQLException err) {
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
        try {
            // check if user owns the photo
            PreparedStatement stmt = conn.prepareStatement("SELECT pid FROM photocol.photo WHERE uid=? AND uri=?");
            stmt.setInt(1, uid);
            stmt.setString(2, uri);

            ResultSet rs = stmt.executeQuery();
            if(rs.next())
                return rs.getInt("pid");

            if(checkOwner)
                throw new HttpMessageException(401, NOT_PHOTO_OWNER);

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
            if(!rs.next())
                throw new HttpMessageException(404, IMAGE_NOT_FOUND);

            // success
            return rs.getInt("pid");
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }

    /**
     * Create photo in database
     * @param uri   uri of photo
     * @param desc  default photo description
     * @param uid   uid of owner
     * @return      true on success
     * @throws HttpMessageException on error
     */
    public boolean createImage(String uri, String desc, int uid) throws HttpMessageException {
        // assume uri is already checked to be unique in service layer
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO photocol.photo " +
                    "(uri, upload_date, description, uid, orig_uid) VALUES(?,?,?,?,?)");
            stmt.setString(1, uri);
            stmt.setDate(2, new Date(new java.util.Date().getTime()));
            stmt.setString(3, desc);
            stmt.setInt(4, uid);
            stmt.setInt(5, uid);

            // this should never happen
            if(stmt.executeUpdate()<1)
                throw new HttpMessageException(500, DATABASE_QUERY_ERROR);

            return true;
        } catch(SQLException err) {
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
        try {

            // TODO: change this to foreign keys, use foreign key cascading
            //       see: mysqltutorial.org/mysql-on-delete-cascade
            PreparedStatement stmt = conn.prepareStatement("DELETE photo, icj FROM photo " +
                    "LEFT JOIN icj ON icj.pid=photo.pid WHERE photo.pid=?");
            stmt.setInt(1, pid);
            stmt.executeUpdate();

            return true;
        } catch(SQLException err) {
            err.printStackTrace();
            throw new HttpMessageException(500, DATABASE_QUERY_ERROR);
        }
    }
}
