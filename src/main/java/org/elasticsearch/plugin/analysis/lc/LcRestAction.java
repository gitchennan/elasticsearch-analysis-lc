package org.elasticsearch.plugin.analysis.lc;

import com.google.common.base.Stopwatch;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.plugin.analysis.lc.dict.reload.LcDictReloadAction;
import org.elasticsearch.plugin.analysis.lc.dict.reload.LcDictReloadRequest;
import org.elasticsearch.plugin.analysis.lc.dict.reload.LcDictReloadResponse;
import org.elasticsearch.rest.*;

import java.io.IOException;
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
            Stopwatch stopwatch = Stopwatch.createStarted();
            LcDictReloadRequest reloadRequest = new LcDictReloadRequest();

            client.executeLocally(LcDictReloadAction.INSTANCE, reloadRequest, new ActionListener<LcDictReloadResponse>() {
                @Override
                public void onResponse(LcDictReloadResponse lcDictReloadResponse) {
                    try {
                        XContentBuilder resultMessageBuilder = JsonXContent.contentBuilder().prettyPrint();
                        resultMessageBuilder.startObject();

                        resultMessageBuilder.field("took", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                        lcDictReloadResponse.toXContent(resultMessageBuilder, ToXContent.EMPTY_PARAMS);

                        resultMessageBuilder.endObject();

                        BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, resultMessageBuilder.string());
                        channel.sendResponse(bytesRestResponse);
                    }
                    catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    String errorMessage = null;
                    try {
                        XContentBuilder resultMessageBuilder = JsonXContent.contentBuilder().prettyPrint();
                        resultMessageBuilder.startObject();
                        resultMessageBuilder.field("message", e.getClass().getName() + ":" + e.getMessage());
                        resultMessageBuilder.endObject();

                        errorMessage = resultMessageBuilder.string();
                    }
                    catch (IOException iex) {
                        errorMessage = e.getClass().getName() + ":" + e.getMessage();
                    }

                    BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, errorMessage);
                    channel.sendResponse(bytesRestResponse);
                }
            });
        };
    }

}
