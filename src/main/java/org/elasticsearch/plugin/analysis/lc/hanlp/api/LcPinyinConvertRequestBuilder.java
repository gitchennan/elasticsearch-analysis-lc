package org.elasticsearch.plugin.analysis.lc.hanlp.api;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class LcPinyinConvertRequestBuilder extends ActionRequestBuilder<LcPinyinConvertRequest, LcPinyinConvertResponse, LcPinyinConvertRequestBuilder> {

    public LcPinyinConvertRequestBuilder(ElasticsearchClient client,
                                         Action<LcPinyinConvertRequest, LcPinyinConvertResponse, LcPinyinConvertRequestBuilder> action) {
        this(client, action, new LcPinyinConvertRequest());
    }

    public LcPinyinConvertRequestBuilder(ElasticsearchClient client,
                                         Action<LcPinyinConvertRequest, LcPinyinConvertResponse, LcPinyinConvertRequestBuilder> action,
                                         LcPinyinConvertRequest request) {
        super(client, action, request);
    }

    public LcPinyinConvertRequestBuilder setInput(String... input) {
        request().setInput(input);
        return this;
    }
}
