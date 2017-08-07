package org.elasticsearch.plugin.analysis.lc.service;

import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionListenerResponseHandler;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.analysis.lc.NodeDictReloadTransportRequest;
import org.elasticsearch.plugin.analysis.lc.NodeDictReloadTransportResponse;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TaskAwareTransportRequestHandler;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportService;

public class DictionaryReloadTransportService extends AbstractComponent {

    public static final String DICT_RELOAD_ACTION_NAME = "dictionaries:data/write/reload[node_reload]";

    private final TransportService transportService;

    private final ClusterService clusterService;

    public DictionaryReloadTransportService(Settings settings, TransportService transportService, ClusterService clusterService) {
        super(settings);
        this.transportService = transportService;
        this.clusterService = clusterService;
    }

    public void sendExecuteNodeReload(DiscoveryNode node, NodeDictReloadTransportRequest request, Task parentTask,
                                      ActionListener<NodeDictReloadTransportResponse> listener) {
        transportService.sendChildRequest(node, DICT_RELOAD_ACTION_NAME, request, parentTask,
                new ActionListenerResponseHandler<>(listener, NodeDictReloadTransportResponse::new));
    }

    public void registerRequestHandler(CustomDictionaryReloadService reloadService) {
        transportService.registerRequestHandler(DICT_RELOAD_ACTION_NAME, NodeDictReloadTransportRequest::new, ThreadPool.Names.GENERIC,
                new TaskAwareTransportRequestHandler<NodeDictReloadTransportRequest>() {
                    @Override
                    public void messageReceived(NodeDictReloadTransportRequest request, TransportChannel channel, Task task) throws Exception {
                        HanLpLogger.info(this, "received child dict reload request, node:" + clusterService.localNode().getName());
                        NodeDictReloadTransportResponse nodeDictReloadTransportResponse = reloadService.doPrivilegedReloadCustomDictionary(request);
                        channel.sendResponse(nodeDictReloadTransportResponse);
                    }
                });
    }
}