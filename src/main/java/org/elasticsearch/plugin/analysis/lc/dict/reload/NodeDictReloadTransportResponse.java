package org.elasticsearch.plugin.analysis.lc.dict.reload;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.TransportResponse;

import java.io.IOException;

public class NodeDictReloadTransportResponse extends TransportResponse implements ToXContent {

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

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        nodeDictReloadResult.toXContent(builder, params);
        return builder;
    }

    @Override
    public String toString() {
        return Strings.toString(this, true);
    }
}
