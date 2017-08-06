package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

public class NodeDictReloadResult implements Streamable, ToXContent {

    private int totalWords = 0;

    private String reloadResultMessage = "OK";

    private String nodeName;

    public NodeDictReloadResult() {

    }

    public NodeDictReloadResult(String nodeName, int totalWords, String reloadResultMessage) {
        this.nodeName = nodeName;
        this.totalWords = totalWords;
        this.reloadResultMessage = reloadResultMessage;
    }

    public String nodeName() {
        return nodeName;
    }

    public int totalWords() {
        return totalWords;
    }

    public String reloadResultMessage() {
        return reloadResultMessage;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        nodeName = in.readString();
        totalWords = in.readInt();
        reloadResultMessage = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(nodeName);
        out.writeInt(totalWords);
        out.writeString(reloadResultMessage);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("node_name", nodeName);
        builder.field("total_words", totalWords);
        builder.field("message", reloadResultMessage);
        return builder;
    }
}
