package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Collections;
import java.util.List;

public class LcAnalysisClientPlugin extends Plugin implements ActionPlugin {

    public static final String PLUGIN_NAME = "analysis-lc";

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(new ActionHandler<>(LcDictReloadAction.INSTANCE, LcTransportDictReloadAction.class));
    }
}
