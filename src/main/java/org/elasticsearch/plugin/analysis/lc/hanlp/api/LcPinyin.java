package org.elasticsearch.plugin.analysis.lc.hanlp.api;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

public class LcPinyin implements Streamable, ToXContent {

    private String pinyin;

    private String pinyinHead;

    private int pinyinLength;

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinyinHead() {
        return pinyinHead;
    }

    public void setPinyinHead(String pinyinHead) {
        this.pinyinHead = pinyinHead;
    }

    public int getPinyinLength() {
        return pinyinLength;
    }

    public void setPinyinLength(int pinyinLength) {
        this.pinyinLength = pinyinLength;
    }

    public LcPinyin() {
    }

    public LcPinyin(String pinyin, String pinyinHead, int pinyinLength) {
        this.pinyin = pinyin;
        this.pinyinHead = pinyinHead;
        this.pinyinLength = pinyinLength;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        pinyin = in.readString();
        pinyinHead = in.readString();
        pinyinLength = in.readInt();
    }


    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(pinyin);
        out.writeString(pinyinHead);
        out.writeInt(pinyinLength);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("pinyin", pinyin);
        builder.field("pinyinHead", pinyinHead);
        builder.field("pinyinLength", pinyinLength);
        return builder;
    }

    @Override
    public String toString() {
        return Strings.toString(this, true);
    }
}
