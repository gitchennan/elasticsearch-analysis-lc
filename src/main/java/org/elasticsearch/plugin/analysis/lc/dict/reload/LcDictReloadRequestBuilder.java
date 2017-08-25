package org.elasticsearch.plugin.analysis.lc.dict.reload;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class LcDictReloadRequestBuilder extends ActionRequestBuilder<LcDictReloadRequest, LcDictReloadResponse, LcDictReloadRequestBuilder> {

    public LcDictReloadRequestBuilder(ElasticsearchClient client, Action<LcDictReloadRequest,
            LcDictReloadResponse, LcDictReloadRequestBuilder> action) {
        this(client, action, new LcDictReloadRequest());
    }

    public LcDictReloadRequestBuilder(ElasticsearchClient client, Action<LcDictReloadRequest,
            LcDictReloadResponse, LcDictReloadRequestBuilder> action, LcDictReloadRequest request) {
        super(client, action, request);
    }
}
