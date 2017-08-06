package org.elasticsearch.plugin.analysis.lc;

import org.elasticsearch.action.ActionListener;

public abstract class ActionListenerAdapter<Response> implements ActionListener<Response> {

    public void onResponseWithException(Response response) throws Exception {

    }

    @Override
    public void onResponse(Response response) {
        try {
            onResponseWithException(response);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onFailure(Exception e) {

    }
}
