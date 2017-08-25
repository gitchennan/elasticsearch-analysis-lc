package org.elasticsearch.plugin.analysis.lc.dict.reload;


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
import org.elasticsearch.common.util.concurrent.AtomicArray;
import org.elasticsearch.plugin.analysis.lc.LcAnalysisPlugin;
import org.elasticsearch.plugin.analysis.lc.service.CustomDictionaryReloadService;
import org.elasticsearch.plugin.analysis.lc.service.DictionaryReloadTransportService;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

        dictionaryReloadTransportService = new DictionaryReloadTransportService(settings, transportService, clusterService);
        dictionaryReloadTransportService.registerRequestHandler(reloadService);
    }

    @Override
    protected void doExecute(Task task, LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        try {
            HanLpLogger.info(this, "received dict reload request, local_node: " + clusterService.localNode().getName());
            reloadAllNodesCustomDictionary(task, request, listener);
        }
        catch (Exception ex) {
            listener.onFailure(ex);
        }
    }

    @Override
    protected void doExecute(LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        throw new UnsupportedOperationException("task required");
    }

    private void reloadAllNodesCustomDictionary(Task parentTask, LcDictReloadRequest request, ActionListener<LcDictReloadResponse> listener) {
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
        nodesInfoRequest.clear().plugins(true);
        NodesInfoResponse nodesInfoResponse = client.admin().cluster().nodesInfo(nodesInfoRequest).actionGet();

        List<DiscoveryNode> targetNodes = Lists.newLinkedList();
        for (DiscoveryNode discoveryNode : clusterService.state().nodes()) {
            NodeInfo info = nodesInfoResponse.getNodesMap().get(discoveryNode.getId());
            targetNodes.addAll(info.getPlugins().getPluginInfos().stream()
                    .filter(pluginInfo -> LcAnalysisPlugin.PLUGIN_NAME.equalsIgnoreCase(pluginInfo.getName()))
                    .map(pluginInfo -> discoveryNode)
                    .collect(Collectors.toList()));
        }

        if (targetNodes.isEmpty()) {
            listener.onResponse(new LcDictReloadResponse(RestStatus.OK, Collections.<NodeDictReloadResult>emptyList()));
        }

        AtomicInteger counter = new AtomicInteger(targetNodes.size());
        AtomicArray<NodeDictReloadResult> nodeDictReloadResults = new AtomicArray<>(targetNodes.size());

        for (DiscoveryNode targetNode : targetNodes) {
            NodeDictReloadTransportRequest reloadRequest = new NodeDictReloadTransportRequest();
            sendChildReloadRequestToTargetNode(targetNode, parentTask, reloadRequest, counter, nodeDictReloadResults, listener);
        }
    }

    private void sendChildReloadRequestToTargetNode(DiscoveryNode targetNode, Task parentTask, NodeDictReloadTransportRequest reloadRequest,
                                                    AtomicInteger counter, AtomicArray<NodeDictReloadResult> nodeDictReloadResults,
                                                    ActionListener<LcDictReloadResponse> topListener) {
        ActionListener<NodeDictReloadTransportResponse> reloadActionListener = new ActionListener<NodeDictReloadTransportResponse>() {
            @Override
            public void onResponse(NodeDictReloadTransportResponse nodeDictReloadTransportResponse) {
                int count = counter.decrementAndGet();
                nodeDictReloadResults.set(count, nodeDictReloadTransportResponse.nodeDictReloadResult());

                if (count == 0) {
                    List<NodeDictReloadResult> nodeDictReloadResultList = Lists.newLinkedList();
                    nodeDictReloadResultList.addAll(nodeDictReloadResults.asList().stream().map(
                            nodeDictReloadResultEntry -> nodeDictReloadResultEntry.value).collect(Collectors.toList()));

                    Collections.sort(nodeDictReloadResultList, (r1, r2) -> r1.nodeName().compareTo(r2.nodeName()));
                    topListener.onResponse(new LcDictReloadResponse(RestStatus.OK, nodeDictReloadResultList));
                }
            }

            @Override
            public void onFailure(Exception e) {
                int count = counter.decrementAndGet();
                nodeDictReloadResults.set(count, new NodeDictReloadResult(targetNode.getName(), 0, e.getClass().getName() + ":" + e.getMessage()));

                if (count == 0) {
                    List<NodeDictReloadResult> nodeDictReloadResultList = Lists.newLinkedList();
                    nodeDictReloadResultList.addAll(nodeDictReloadResults.asList().stream().map(
                            nodeDictReloadResultEntry -> nodeDictReloadResultEntry.value).collect(Collectors.toList()));

                    Collections.sort(nodeDictReloadResultList, (r1, r2) -> r1.nodeName().compareTo(r2.nodeName()));
                    topListener.onResponse(new LcDictReloadResponse(RestStatus.OK, nodeDictReloadResultList));
                }
            }
        };

        HanLpLogger.info(this, "send node level dict reload request to target_node: " + targetNode.toString());
        dictionaryReloadTransportService.sendExecuteNodeReload(targetNode, reloadRequest, parentTask, reloadActionListener);
    }
}
