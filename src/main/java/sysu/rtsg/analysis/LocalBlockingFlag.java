package sysu.rtsg.analysis;

import sysu.rtsg.entity.Resource;

public class LocalBlockingFlag {
    public boolean isLongResource;
    public Resource resource;
    public Long blocking;

    public LocalBlockingFlag(boolean isLongResource, Resource resource, Long blocking){
        this.isLongResource = isLongResource;
        this.resource = resource;
        this.blocking = blocking;
    }
}
