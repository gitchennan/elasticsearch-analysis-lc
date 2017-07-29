package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;

public class LcDictReloadRequest extends ActionRequest {
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
}
