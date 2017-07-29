package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

public class LcDictReloadResponse extends ActionResponse implements StatusToXContent {

    private RestStatus restStatus;

    private String loadResultMessage;

    public LcDictReloadResponse() {

    }

    public LcDictReloadResponse(RestStatus restStatus, String loadResultMessage) {
        this.loadResultMessage = loadResultMessage;
        this.restStatus = restStatus;
    }

    @Override
    public RestStatus status() {
        return restStatus;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder.field("message", loadResultMessage).field("status", restStatus.getStatus());
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        loadResultMessage = in.readString();
        restStatus = RestStatus.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(loadResultMessage);
        RestStatus.writeTo(out, restStatus);
    }


    @Override
    public String toString() {
        return Strings.toString(this, true);
    }
}
