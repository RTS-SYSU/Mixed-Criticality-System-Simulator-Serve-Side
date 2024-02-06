package sysu.rtsg.analysis;

import sysu.rtsg.entity.Resource;

public class ArBData {
    public Resource resource;
    public long local_blocking;
    public int remote_processor_size;
    public ArBData(Resource resource, long local_blocking, int remote_processor_size) {
        this.resource = resource;
        this.local_blocking = local_blocking;
        this.remote_processor_size = remote_processor_size;
    }

}
