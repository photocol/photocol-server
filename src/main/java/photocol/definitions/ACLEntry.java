package photocol.definitions;

// to be used by EndpointRequestModel when updating ACL list
public class ACLEntry {
    public String email;
    public Role role;

    // for use when converting from db
    public ACLEntry(String email, int role) {
        this.email = email;
        this.role = Role.fromRole(role);
    }

    public enum Role {
        ROLE_OWNER(0),
        ROLE_VIEWER(1),
        ROLE_EDITOR(2);

        private int roleInt;
        Role(int roleInt) {
            this.roleInt = roleInt;
        }
        public int toInt() {
            return roleInt;
        }
        public static Role fromRole(int roleInt) {
            switch (roleInt) {
                case 0: return ROLE_OWNER;
                case 1: return ROLE_VIEWER;
                case 2: return ROLE_EDITOR;
            }
            return null;
        }
    }
}
