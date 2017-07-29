package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class LcDictReloadAction extends Action<LcDictReloadRequest, LcDictReloadResponse, LcDictReloadRequestBuilder> {

    public static final LcDictReloadAction INSTANCE = new LcDictReloadAction();

    public static final String NAME = "dictionaries:data/write/reload";

    private LcDictReloadAction() {
        super(NAME);
    }

    @Override
    public LcDictReloadRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new LcDictReloadRequestBuilder(client, this);
    }

    @Override
    public LcDictReloadResponse newResponse() {
        return new LcDictReloadResponse();
    }
}
