package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.List;

public class LcDictReloadResponse extends ActionResponse implements StatusToXContent {

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
        this.nodeDictReloadResults = nodeDictReloadResults;
        this.message = message;
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
        if (nodeDictReloadResults != null) {
            for (NodeDictReloadResult nodeDictReloadResult : nodeDictReloadResults) {
                builder.startObject(nodeDictReloadResult.nodeName());

                builder.field("total_words", nodeDictReloadResult.totalWords());
                builder.field("result_message", nodeDictReloadResult.reloadResultMessage());

                builder.endObject();
            }
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
        int resultSize = in.readInt();
        if (resultSize > 0) {
            nodeDictReloadResults = in.readStreamableList(() -> {
                NodeDictReloadResult nodeDictReloadResult = null;
                try {
                    String nodeName = in.readString();
                    int totalWords = in.readInt();
                    String reloadResultMessage = in.readString();
                    nodeDictReloadResult = new NodeDictReloadResult(nodeName, totalWords, reloadResultMessage);
                }
                catch (Exception ex) {
                    nodeDictReloadResult = new NodeDictReloadResult("unknown-node", 0, "Failed to deserialize reload result from stream.");
                }
                return nodeDictReloadResult;
            });
        }

        boolean hasMessage = in.readBoolean();
        if (hasMessage) {
            message = in.readString();
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        RestStatus.writeTo(out, restStatus);
        if (nodeDictReloadResults != null) {
            out.writeInt(nodeDictReloadResults.size());
            out.writeStreamableList(nodeDictReloadResults);
        }
        else {
            out.writeInt(0);
        }

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
        return Strings.toString(this, true);
    }
}
