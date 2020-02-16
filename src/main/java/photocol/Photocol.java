/* Main class for the Photocol app */

package photocol;

import photocol.store.CollectionStore;
import photocol.store.PhotoStore;
import photocol.store.UserStore;

public class Photocol {
    public static void main(String[] args) {
        UserStore us = new UserStore();
        PhotoStore ps = new PhotoStore();
        CollectionStore cs = new CollectionStore();

        Endpoints ep = new Endpoints(us, ps, cs);
    }
}
