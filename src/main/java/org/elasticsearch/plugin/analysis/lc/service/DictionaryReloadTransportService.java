package org.elasticsearch.plugin.analysis.lc.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionListenerResponseHandler;
import org.elasticsearch.cluster.node.DiscoveryNode;
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

    public DictionaryReloadTransportService(Settings settings, TransportService transportService) {
        super(settings);
        this.transportService = transportService;
    }

    public void sendExecuteNodeReload(DiscoveryNode node, NodeDictReloadTransportRequest request, Task parentTask,
                                      ActionListener<NodeDictReloadTransportResponse> listener) {
        transportService.sendChildRequest(node, DICT_RELOAD_ACTION_NAME, request, parentTask,
                new ActionListenerResponseHandler<>(listener, NodeDictReloadTransportResponse::new));
    }

    public static void registerRequestHandler(TransportService transportService, CustomDictionaryReloadService customDictionaryReloadService) {
        transportService.registerRequestHandler(DICT_RELOAD_ACTION_NAME, NodeDictReloadTransportRequest::new, ThreadPool.Names.SAME,
                new TaskAwareTransportRequestHandler<NodeDictReloadTransportRequest>() {
                    @Override
                    public void messageReceived(NodeDictReloadTransportRequest request, TransportChannel channel, Task task) throws Exception {
                        NodeDictReloadTransportResponse nodeDictReloadTransportResponse = customDictionaryReloadService.doPrivilegedReloadCustomDictionary(request);
                        channel.sendResponse(nodeDictReloadTransportResponse);
                    }
                });
    }
}
