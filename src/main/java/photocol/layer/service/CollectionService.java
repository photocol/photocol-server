package photocol.layer.service;

import photocol.layer.store.CollectionStore;

public class CollectionService {

    private CollectionStore collectionStore;
    public CollectionService(CollectionStore collectionStore){
        this.collectionStore = collectionStore;
    }
}
