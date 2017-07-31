package org.elasticsearch.plugin.analysis.lc;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionListenerResponseHandler;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.rest.*;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LcRestAction extends BaseRestHandler {

    private Client client;

    private TransportService transportService;

    private ClusterService clusterService;

    @Inject
    public LcRestAction(Client client, Settings settings, RestController controller, ClusterService clusterService, TransportService transportService) {
        super(settings);
        controller.registerHandler(RestRequest.Method.GET, "/_lc/reload", this);

        this.client = client;
        this.clusterService = clusterService;
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
            restResultBuilder.field("message", "Reload custom_dict.");
            restResultBuilder.endObject();

            BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, restResultBuilder.string());
            channel.sendResponse(bytesRestResponse);
        };
    }

    private void internalReloadCustomDictionaries() {
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
        nodesInfoRequest.clear().plugins(true);
        NodesInfoResponse nodesInfoResponse = client.admin().cluster().nodesInfo(nodesInfoRequest).actionGet();

        List<DiscoveryNode> targetNodes = Lists.newLinkedList();
        for (DiscoveryNode discoveryNode : clusterService.state().nodes()) {
            NodeInfo info = nodesInfoResponse.getNodesMap().get(discoveryNode.getId());
            for (PluginInfo pluginInfo : info.getPlugins().getPluginInfos()) {
                /**
                 * only invoke installed analysis-lc plugin nodes
                 */
                if (LcAnalysisPlugin.PLUGIN_NAME.equalsIgnoreCase(pluginInfo.getName())) {
                    targetNodes.add(discoveryNode);
                }
            }
        }

        CountDownLatch countDownLatch = new CountDownLatch(targetNodes.size());
        ActionListener<LcDictReloadResponse> reloadActionListener = new ActionListener<LcDictReloadResponse>() {
            @Override
            public void onResponse(LcDictReloadResponse reloadResponse) {
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                HanLpLogger.error(this, "onFailure", e);
                countDownLatch.countDown();
            }
        };

        ActionListenerResponseHandler<LcDictReloadResponse> responseHandler
                = new ActionListenerResponseHandler<>(reloadActionListener, LcDictReloadResponse::new);

        for (DiscoveryNode discoveryNode : targetNodes) {
            LcDictReloadRequest reloadRequest = new LcDictReloadRequest();
            transportService.sendRequest(discoveryNode, LcDictReloadAction.NAME, reloadRequest, responseHandler);
        }

        try {
            countDownLatch.await(10, TimeUnit.MINUTES);
        }
        catch (InterruptedException ex) {
            HanLpLogger.error(this, "custom dict reload main thread interrupted", ex);
        }
    }
}
