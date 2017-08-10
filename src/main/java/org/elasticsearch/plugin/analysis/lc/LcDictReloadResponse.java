package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LcDictReloadResponse extends ActionResponse implements StatusToXContentObject {

    private RestStatus restStatus = RestStatus.OK;

    private List<NodeDictReloadResult> nodeDictReloadResults;

    private String message;

    public LcDictReloadResponse() {

    }

    public LcDictReloadResponse(RestStatus restStatus, List<NodeDictReloadResult> nodeDictReloadResults) {
        this(restStatus, nodeDictReloadResults, null);
    }

    public LcDictReloadResponse(RestStatus restStatus, List<NodeDictReloadResult> nodeDictReloadResults, String message) {
        this.restStatus = restStatus;
        this.message = message;

        if (nodeDictReloadResults == null) {
            this.nodeDictReloadResults = Collections.emptyList();
        }
        else {
            this.nodeDictReloadResults = nodeDictReloadResults;
        }
    }

    @Override
    public RestStatus status() {
        return restStatus;
    }

    public List<NodeDictReloadResult> nodeDictReloadResults() {
        return nodeDictReloadResults;
    }

    public String message() {
        return message;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("status", restStatus.getStatus());

        for (NodeDictReloadResult nodeDictReloadResult : nodeDictReloadResults) {
            builder.startObject(nodeDictReloadResult.nodeName());

            builder.field("total_words", nodeDictReloadResult.totalWords());
            builder.field("result_message", nodeDictReloadResult.reloadResultMessage());

            builder.endObject();
        }

        if (message != null) {
            builder.field("message", message);
        }
        return builder;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        restStatus = RestStatus.readFrom(in);
        nodeDictReloadResults = in.readStreamableList(NodeDictReloadResult::new);

        boolean hasMessage = in.readBoolean();
        if (hasMessage) {
            message = in.readString();
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        RestStatus.writeTo(out, restStatus);
        out.writeStreamableList(nodeDictReloadResults);

        if (message != null) {
            out.writeBoolean(true);
            out.writeString(message);
        }
        else {
            out.writeBoolean(false);
        }
    }

    @Override
    public String toString() {
        return Strings.toString(this);
    }
}
