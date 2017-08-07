package org.elasticsearch.plugin.analysis.lc;

import com.google.common.base.Stopwatch;
import org.elasticsearch.action.LatchedActionListener;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LcRestAction extends BaseRestHandler {

    @Inject
    public LcRestAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.GET, "/_lc/reload", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        return channel -> {
            XContentBuilder resultMessageBuilder = JsonXContent.contentBuilder().prettyPrint();
            resultMessageBuilder.startObject();

            StringBuilder errorMessageBuilder = new StringBuilder();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Stopwatch stopwatch = Stopwatch.createStarted();

            LcDictReloadRequest reloadRequest = new LcDictReloadRequest();
            client.executeLocally(LcDictReloadAction.INSTANCE, reloadRequest, new LatchedActionListener<>(new ActionListenerAdapter<LcDictReloadResponse>() {
                @Override
                public void onResponseWithException(LcDictReloadResponse lcDictReloadResponse) throws Exception {
                    resultMessageBuilder.field("took", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    lcDictReloadResponse.toXContent(resultMessageBuilder, ToXContent.EMPTY_PARAMS);
                }

                @Override
                public void onFailure(Exception e) {
                    errorMessageBuilder.append(e.getClass().getName()).append(":").append(e.getMessage());
                }
            }, countDownLatch));

            countDownLatch.await(5, TimeUnit.SECONDS);

            if (errorMessageBuilder.length() > 0) {
                resultMessageBuilder.field("message", errorMessageBuilder.toString());
            }

            resultMessageBuilder.endObject();

            BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, resultMessageBuilder.string());
            channel.sendResponse(bytesRestResponse);
        };
    }

}
