package com.djeno.lab1.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import javax.transaction.xa.XAResource;
import java.io.Serializable;

@Connector(
        displayName = "Yookassa Resource Adapter",
        vendorName = "BLPS",
        eisType = "Yokass",
        version = "1.0"
)
public class YookassaResourceAdapter implements ResourceAdapter, Serializable {
    private transient BootstrapContext bootstrapContext;

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        // No cleanup needed
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        // Not used for outbound
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        // Not used for outbound
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }
}
