package org.elasticsearch.plugin.analysis.lc.hanlp.api;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class LcPinyinConvertAction extends Action<LcPinyinConvertRequest, LcPinyinConvertResponse, LcPinyinConvertRequestBuilder> {

    public static final LcPinyinConvertAction INSTANCE = new LcPinyinConvertAction();

    public static final String NAME = "hanlpapis:api/pinyin/convert";

    private LcPinyinConvertAction() {
        super(NAME);
    }

    @Override
    public LcPinyinConvertRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new LcPinyinConvertRequestBuilder(client, this);
    }

    @Override
    public LcPinyinConvertResponse newResponse() {
        return new LcPinyinConvertResponse();
    }
}
