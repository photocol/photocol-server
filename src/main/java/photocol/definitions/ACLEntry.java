package photocol.definitions;

// to be used by EndpointRequestModel when updating ACL list
public class ACLEntry {
    public String username;
    public Role role;
    public int uid;
    public ACLOperation operation;

    // for use when username known
    public ACLEntry(String username, Role role) {
        this.username = username;
        this.role = role;
        this.operation = null;
    }
    public ACLEntry(String username, int role) {
        this(username, Role.fromInt(role));
    }
    // for use when uid known (i.e., for checking uids on update)
    public ACLEntry(int uid, int role) {
        this.uid = uid;
        this.role = Role.fromInt(role);
        this.operation = null;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setOperation(ACLOperation operation) {
        this.operation = operation;
    }

    // for use when updating collection, set by service layer and used by store layer
    public enum ACLOperation { OP_INSERT, OP_DELETE, OP_UPDATE, OP_INSERT_OWNER, OP_UPDATE_OWNER; };

    public enum Role {
        ROLE_OWNER(0),
        ROLE_VIEWER(1),
        ROLE_EDITOR(2),
        ROLE_NONE(-1);

        private int roleInt;
        Role(int roleInt) {
            this.roleInt = roleInt;
        }
        public int toInt() {
            return roleInt;
        }
        public static Role fromInt(int roleInt) {
            switch (roleInt) {
                case 0: return ROLE_OWNER;
                case 1: return ROLE_VIEWER;
                case 2: return ROLE_EDITOR;
            }
            return null;
        }
    }
}
