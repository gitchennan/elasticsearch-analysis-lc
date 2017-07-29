package org.elasticsearch.plugin.analysis.lc;

import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionListenerResponseHandler;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.*;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LcRestAction extends BaseRestHandler {

    private TransportService transportService;

    private ClusterService clusterService;

    @Inject
    public LcRestAction(Settings settings, RestController controller, ClusterService clusterService, TransportService transportService) {
        super(settings);
        this.clusterService = clusterService;
        controller.registerHandler(RestRequest.Method.GET, "/_lc/reload", this);

        this.transportService = transportService;
        this.clusterService = clusterService;
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        return channel -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            internalReloadCustomDictionaries();
            stopwatch.stop();

            XContentBuilder restResultBuilder = XContentFactory.jsonBuilder().prettyPrint();
            restResultBuilder.startObject();
            restResultBuilder.field("took", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            restResultBuilder.field("message", "Reload custom dictionaries.");
            restResultBuilder.endObject();

            BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, restResultBuilder.string());
            channel.sendResponse(bytesRestResponse);
        };
    }

    private void internalReloadCustomDictionaries() {
        CountDownLatch countDownLatch = new CountDownLatch(clusterService.state().nodes().getSize());
        ActionListener<LcDictReloadResponse> reloadActionListener = new ActionListener<LcDictReloadResponse>() {
            @Override
            public void onResponse(LcDictReloadResponse reloadResponse) {
                HanLpLogger.info(this, "onResponse: " + reloadResponse.toString());

                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                HanLpLogger.error(this, "onFailure", e);

                countDownLatch.countDown();
            }
        };

        ActionListenerResponseHandler<LcDictReloadResponse> responseHandler
                = new ActionListenerResponseHandler<LcDictReloadResponse>(reloadActionListener, LcDictReloadResponse::new);

        for (DiscoveryNode discoveryNode : clusterService.state().nodes()) {
            transportService.sendRequest(discoveryNode, LcDictReloadAction.NAME, new LcDictReloadRequest(), responseHandler);
        }

        try {
            countDownLatch.await(10, TimeUnit.MINUTES);
        }
        catch (InterruptedException ex) {
            HanLpLogger.error(this, "custom dict reload main thread interrupted", ex);
        }
    }
}
