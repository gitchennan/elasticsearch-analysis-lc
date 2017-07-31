package org.elasticsearch.plugin.analysis.lc;

import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;

public class LcDictReloadRequest extends ActionRequest {
    @Override
    public ActionRequestValidationException validate() {
        HanLpLogger.debug(this, "there's nothing to validate");
        return null;
    }
}
