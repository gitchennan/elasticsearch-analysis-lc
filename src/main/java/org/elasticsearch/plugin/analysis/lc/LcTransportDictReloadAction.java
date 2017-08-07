package org.elasticsearch.plugin.analysis.lc;


import com.google.common.collect.Lists;
import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionListener;
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

public class LcTransportDictReloadAction extends HandledTransportAction<LcDictReloadRequest, LcDictReloadResponse> {

    private Client client;

    private ClusterService clusterService;

    private DictionaryReloadTransportService dictionaryReloadTransportService;

    private CustomDictionaryReloadService reloadService;

    @Inject
    public LcTransportDictReloadAction(Settings settings, ThreadPool threadPool, ActionFilters actionFilters, CustomDictionaryReloadService reloadService,
                                       IndexNameExpressionResolver indexNameExpressionResolver, Client client, TransportService transportService, ClusterService clusterService) {
        super(settings, LcDictReloadAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, LcDictReloadRequest::new);
        this.clusterService = clusterService;
        this.client = client;

        this.reloadService = reloadService;
        dictionaryReloadTransportService = new DictionaryReloadTransportService(settings, transportService, clusterService);
        dictionaryReloadTransportService.registerRequestHandler(reloadService);
    }

    @Override
    protected void doExecute(Task task, LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        try {
            HanLpLogger.info(this, "received dict reload request, node: " + clusterService.localNode().getName());
            LcDictReloadResponse dictReloadResponse = reloadAllNodesCustomDictionary(task, request);
            listener.onResponse(dictReloadResponse);
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
            if (clusterService.localNode().getId().equals(discoveryNode.getId())) {
                // ignore local node
                continue;
            }

            for (PluginInfo pluginInfo : info.getPlugins().getPluginInfos()) {
                if (LcAnalysisPlugin.PLUGIN_NAME.equalsIgnoreCase(pluginInfo.getName())) {
                    targetNodes.add(discoveryNode);
                }
            }
        }

        if (targetNodes.isEmpty()) {
            return new LcDictReloadResponse(RestStatus.OK, Collections.<NodeDictReloadResult>emptyList());
        }

        List<NodeDictReloadResult> nodeDictReloadResults = Collections.synchronizedList(Lists.newArrayList());
//        CountDownLatch countDownLatch = new CountDownLatch(targetNodes.size());

        ActionListener<NodeDictReloadTransportResponse> reloadActionListener = new ActionListener<NodeDictReloadTransportResponse>() {
            @Override
            public void onResponse(NodeDictReloadTransportResponse nodeDictReloadTransportResponse) {
                nodeDictReloadResults.add(nodeDictReloadTransportResponse.nodeDictReloadResult());
            }

            @Override
            public void onFailure(Exception e) {
                HanLpLogger.error(this, "onFailure: " + e.getMessage(), e);
            }
        };

        HanLpLogger.info(this, "send node level dict reload request to nodes: " + targetNodes.toString());
        for (DiscoveryNode discoveryNode : targetNodes) {
            NodeDictReloadTransportRequest reloadRequest = new NodeDictReloadTransportRequest();
            dictionaryReloadTransportService.sendExecuteNodeReload(discoveryNode, reloadRequest, parentTask, reloadActionListener);
        }

//        try {
//            HanLpLogger.info(this, "await for remote nodes callback, node: " + clusterService.localNode().getName());
//            countDownLatch.await(5, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException ex) {
//            HanLpLogger.error(this, "custom dict reload main thread interrupted", ex);
//        }

        NodeDictReloadTransportResponse localResponse = reloadService.doPrivilegedReloadCustomDictionary(new NodeDictReloadTransportRequest());
        nodeDictReloadResults.add(localResponse.nodeDictReloadResult());

        return new LcDictReloadResponse(RestStatus.OK, nodeDictReloadResults);
    }
}
