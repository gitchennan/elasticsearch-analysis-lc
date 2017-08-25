package org.elasticsearch.plugin.analysis.lc.dict.reload;

import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class LcDictReloadRequest extends ActionRequest {
    @Override
    public ActionRequestValidationException validate() {
        HanLpLogger.debug(this, "there's nothing to validate");
        return null;
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
