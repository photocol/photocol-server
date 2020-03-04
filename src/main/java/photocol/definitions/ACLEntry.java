package photocol.definitions;

// to be used by EndpointRequestModel when updating ACL list
public class ACLEntry {
    public String username;
    public Role role;
    public int uid;

    // for use when converting from db
    public ACLEntry(String email, int role) {
        this.username = username;
        this.role = Role.fromInt(role);
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

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
