package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.transport.TransportResponse;

import java.io.IOException;

public class NodeDictReloadTransportResponse extends TransportResponse {

    private NodeDictReloadResult nodeDictReloadResult;

    public NodeDictReloadTransportResponse() {

    }

    public NodeDictReloadTransportResponse(NodeDictReloadResult nodeDictReloadResult) {
        this.nodeDictReloadResult = nodeDictReloadResult;
    }

    public NodeDictReloadResult nodeDictReloadResult() {
        return nodeDictReloadResult;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        nodeDictReloadResult = new NodeDictReloadResult();
        nodeDictReloadResult.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        nodeDictReloadResult.writeTo(out);
    }
}
