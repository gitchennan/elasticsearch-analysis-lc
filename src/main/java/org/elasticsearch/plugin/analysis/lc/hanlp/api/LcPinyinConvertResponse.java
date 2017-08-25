package org.elasticsearch.plugin.analysis.lc.hanlp.api;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LcPinyinConvertResponse extends ActionResponse implements ToXContent {

    private String message;

    private Map<String, List<LcPinyin>> pinyinMap;

    public LcPinyinConvertResponse() {

    }

    public LcPinyinConvertResponse(String message, Map<String, List<LcPinyin>> pinyinMap) {
        this.message = message;
        this.pinyinMap = pinyinMap;
    }

    public String message() {
        return message;
    }

    public Map<String, List<LcPinyin>> pinyinMap() {
        return pinyinMap;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (message != null) {
            builder.field("message", message);
        }

        if (pinyinMap != null) {
            builder.startObject("pinyinMap");
            builder.map(pinyinMap);
            builder.endObject();
        }
        return builder;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);

        boolean hasMessage = in.readBoolean();
        if (hasMessage) {
            message = in.readString();
        }

        boolean hasMap = in.readBoolean();
        if (hasMap) {
            pinyinMap = in.readMapOfLists(new Writeable.Reader<String>() {
                @Override
                public String read(StreamInput in) throws IOException {
                    return in.readString();
                }
            }, new Writeable.Reader<LcPinyin>() {
                @Override
                public LcPinyin read(StreamInput in) throws IOException {
                    LcPinyin lcPinyin = new LcPinyin();
                    lcPinyin.readFrom(in);
                    return lcPinyin;
                }
            });
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);

        if (message != null) {
            out.writeBoolean(true);
            out.writeString(message);
        }
        else {
            out.writeBoolean(false);
        }

        if (pinyinMap != null) {
            out.writeBoolean(true);
            out.writeMapOfLists(pinyinMap, new Writeable.Writer<String>() {
                @Override
                public void write(StreamOutput out, String value) throws IOException {
                    out.writeString(value);
                }
            }, new Writeable.Writer<LcPinyin>() {
                @Override
                public void write(StreamOutput out, LcPinyin value) throws IOException {
                    value.writeTo(out);
                }
            });
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
