package org.elasticsearch.plugin.analysis.lc;


import com.google.common.collect.Lists;
import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.LatchedActionListener;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.analysis.lc.service.CustomDictionaryReloadService;
import org.elasticsearch.plugin.analysis.lc.service.DictionaryReloadTransportService;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LcTransportDictReloadAction extends HandledTransportAction<LcDictReloadRequest, LcDictReloadResponse> {

    private Client client;

    private ClusterService clusterService;

    private DictionaryReloadTransportService dictionaryReloadTransportService;

    @Inject
    public LcTransportDictReloadAction(Settings settings, ThreadPool threadPool, ActionFilters actionFilters, CustomDictionaryReloadService reloadService,
                                       IndexNameExpressionResolver indexNameExpressionResolver, Client client, TransportService transportService, ClusterService clusterService) {
        super(settings, LcDictReloadAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, LcDictReloadRequest::new);
        this.clusterService = clusterService;
        this.client = client;

        DictionaryReloadTransportService.registerRequestHandler(transportService, reloadService);
        dictionaryReloadTransportService = new DictionaryReloadTransportService(settings, transportService);
    }

    @Override
    protected void doExecute(Task task, LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        try {
            LcDictReloadResponse dictReloadResponse = reloadAllNodesCustomDictionary(task, request);
            listener.onResponse(dictReloadResponse);

            HanLpLogger.info(this, dictReloadResponse.toString());
        }
        catch (Exception ex) {
            listener.onFailure(ex);
        }
    }

    @Override
    protected void doExecute(LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        throw new UnsupportedOperationException("task required");
    }

    private LcDictReloadResponse reloadAllNodesCustomDictionary(Task parentTask, LcDictReloadRequest request) throws Exception {
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
        nodesInfoRequest.clear().plugins(true);
        NodesInfoResponse nodesInfoResponse = client.admin().cluster().nodesInfo(nodesInfoRequest).actionGet();

        List<DiscoveryNode> targetNodes = Lists.newLinkedList();
        for (DiscoveryNode discoveryNode : clusterService.state().nodes()) {
            NodeInfo info = nodesInfoResponse.getNodesMap().get(discoveryNode.getId());
            for (PluginInfo pluginInfo : info.getPlugins().getPluginInfos()) {
                if (LcAnalysisPlugin.PLUGIN_NAME.equalsIgnoreCase(pluginInfo.getName())) {
                    targetNodes.add(discoveryNode);
                }
            }
        }

        if (targetNodes.isEmpty()) {
            return new LcDictReloadResponse(RestStatus.OK, Collections.<NodeDictReloadResult>emptyList());
        }

        List<NodeDictReloadResult> nodeDictReloadResults = Lists.newLinkedList();
        CountDownLatch countDownLatch = new CountDownLatch(targetNodes.size());

        ActionListener<NodeDictReloadTransportResponse> reloadActionListener = new LatchedActionListener<>(new ActionListenerAdapter<NodeDictReloadTransportResponse>() {
            @Override
            public void onResponseWithException(NodeDictReloadTransportResponse nodeDictReloadTransportResponse) throws Exception {
                nodeDictReloadResults.add(nodeDictReloadTransportResponse.nodeDictReloadResult());
            }

            @Override
            public void onFailure(Exception e) {
                HanLpLogger.error(this, "onFailure: " + e.getMessage(), e);
            }
        }, countDownLatch);

        for (DiscoveryNode discoveryNode : targetNodes) {
            NodeDictReloadTransportRequest reloadRequest = new NodeDictReloadTransportRequest();
            dictionaryReloadTransportService.sendExecuteNodeReload(discoveryNode, reloadRequest, parentTask, reloadActionListener);
        }

        try {
            countDownLatch.await(5, TimeUnit.MINUTES);
        }
        catch (InterruptedException ex) {
            HanLpLogger.error(this, "custom dict reload main thread interrupted", ex);
        }

        return new LcDictReloadResponse(RestStatus.OK, nodeDictReloadResults);
    }
}
