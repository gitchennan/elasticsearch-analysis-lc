package org.elasticsearch.plugin.analysis.lc.hanlp.api;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class LcPinyinConvertRequest extends ActionRequest {

    private String[] input;

    @Override
    public ActionRequestValidationException validate() {
        if (input == null) {
            ActionRequestValidationException ex = new ActionRequestValidationException();
            ex.addValidationError("input is null.");
            return ex;
        }
        return null;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        input = in.readStringArray();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeStringArray(input);
    }

    public String[] getInput() {
        return input;
    }

    public void setInput(String[] input) {
        this.input = input;
    }
}
