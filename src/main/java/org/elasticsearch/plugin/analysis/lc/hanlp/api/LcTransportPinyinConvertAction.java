package org.elasticsearch.plugin.analysis.lc.hanlp.api;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.dictionary.py.PinyinDictionary;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.List;
import java.util.Map;

public class LcTransportPinyinConvertAction extends HandledTransportAction<LcPinyinConvertRequest, LcPinyinConvertResponse> {

    @Inject
    public LcTransportPinyinConvertAction(Settings settings, ThreadPool threadPool, ActionFilters actionFilters,
                                          IndexNameExpressionResolver indexNameExpressionResolver, TransportService transportService) {
        super(settings, LcPinyinConvertAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, LcPinyinConvertRequest::new);
    }

    @Override
    protected void doExecute(LcPinyinConvertRequest request, ActionListener<LcPinyinConvertResponse> listener) {
        try {
            String[] input = request.getInput();
            Map<String, List<LcPinyin>> pinyinMap = Maps.newHashMap();
            for (String word : input) {
                List<LcPinyin> lcPinyinList = Lists.newArrayList();
                List<Pinyin> wordPinyinList = PinyinDictionary.INSTANCE.convertToPinyin(word);

                for (Pinyin pinyin : wordPinyinList) {
                    LcPinyin lcPinyin = new LcPinyin(pinyin.getPinyinWithoutTone(), pinyin.getHeadString(), pinyin.getPinyinWithoutTone().length());
                    lcPinyinList.add(lcPinyin);
                }
                pinyinMap.put(word, lcPinyinList);
            }

            LcPinyinConvertResponse response = new LcPinyinConvertResponse("OK", pinyinMap);
            listener.onResponse(response);
        }
        catch (Exception ex) {
            listener.onFailure(ex);
        }
    }
}
