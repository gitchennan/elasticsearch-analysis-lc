package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.transport.TransportRequest;

import java.io.IOException;

public class NodeDictReloadTransportRequest extends TransportRequest {
    @Override
    public String getDescription() {
        return "Reload node level custom dictionary request.";
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }
}
