package photocol.layer.store;

import photocol.definitions.Photo;
import photocol.definitions.response.StatusResponse;
import photocol.layer.DataBase.Method.InitDB;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static photocol.definitions.response.StatusResponse.Status.*;

public class PhotoStore {

    private Connection conn;
    public PhotoStore() {
        // TODO: change this
        this.conn = new InitDB().initialDB("photocol");
    }

    // check if photo uri is taken; used when generating random photo uris
    public StatusResponse checkIfPhotoExists(String uri) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT pid FROM photocol.photo WHERE uri=?");
            stmt.setString(1, uri);

            ResultSet rs = stmt.executeQuery();
            return new StatusResponse(rs.next() ? STATUS_OK : STATUS_IMAGE_NOT_FOUND);
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_IMAGE_NOT_FOUND);
        }
    }

    // get all images that belong to a user
    public StatusResponse<List<Photo>> getUserPhotos(int uid) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT uri,description,upload_date FROM photocol.photo WHERE uid=?");
            stmt.setInt(1, uid);

            ResultSet rs = stmt.executeQuery();
            List<Photo> userPhotos = new ArrayList<>();
            while(rs.next())
                userPhotos.add(new Photo(rs.getString("uri"), rs.getString("description"), rs.getDate("upload_date")));
            return new StatusResponse<>(STATUS_OK, userPhotos);
        } catch(Exception err) {
            err.printStackTrace();
            // TODO: change status code to something more fitting
            return new StatusResponse<>(STATUS_MISC);
        }
    }

    // check to see if user owns photo or has access to a collection that contains the image; returns pid
    public StatusResponse<Integer> checkPhotoPermissions(String uri, int uid) {
        try {

            // check if user owns the image
            PreparedStatement stmt = conn.prepareStatement("SELECT pid FROM photocol.photo WHERE uid=? AND uri=?");
            stmt.setInt(1, uid);
            stmt.setString(2, uri);

            ResultSet rs = stmt.executeQuery();
            if(rs.next())
                return new StatusResponse(STATUS_OK, rs.getInt("pid"));

            // check if user is in one of the collections that contains the image
            // TODO: check if this join is actually correct; not sure how to use joins
            // TODO: can probably simplify to one join if duplicate imageuri to icj table
            stmt = conn.prepareStatement("SELECT photocol.photo.pid " +
                    "FROM photocol.acl " +
                    "INNER JOIN photocol.icj ON photocol.acl.cid=photocol.icj.cid " +
                    "INNER JOIN photocol.photo ON photocol.photo.pid=photocol.icj.pid " +
                    "WHERE photocol.acl.uid=? AND photocol.photo.uri=?");
            stmt.setInt(1, uid);
            stmt.setString(2, uri);

            rs = stmt.executeQuery();
            if(!rs.next())
                return new StatusResponse<>(STATUS_IMAGE_NOT_FOUND);
            return new StatusResponse<>(STATUS_OK, rs.getInt("pid"));
        } catch(Exception err) {
            err.printStackTrace();
            return new StatusResponse(STATUS_IMAGE_NOT_FOUND);
        }
    }

    // create image in database
    public StatusResponse createImage(String uri, String desc, int uid) {
        // assume uri is already checked to be unique in service layer
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO photocol.photo (uri, upload_date, description, uid, orig_uid) VALUES(?,?,?,?,?)");
            stmt.setString(1, uri);
            stmt.setDate(2, new Date(new java.util.Date().getTime()));
            stmt.setString(3, desc);
            stmt.setInt(4, uid);
            stmt.setInt(5, uid);

            if(stmt.executeUpdate()<1)
                return new StatusResponse(STATUS_MISC);

            return new StatusResponse(STATUS_OK);
        } catch(Exception err) {
            err.printStackTrace();
            // TODO: add more fitting status name
            return new StatusResponse(STATUS_MISC);
        }
    }
}
