/* Main class for the Photocol app */

package photocol;

import photocol.store.CollectionStore;
import photocol.store.PhotoStore;
import photocol.store.UserStore;

public class Photocol {
    public static void main(String[] args) {
        // initialize data store managers; short names b/c these will be used very often
        UserStore us = new UserStore();
        PhotoStore ps = new PhotoStore();
        CollectionStore cs = new CollectionStore();

        new Endpoints(us, ps, cs);
    }
}
