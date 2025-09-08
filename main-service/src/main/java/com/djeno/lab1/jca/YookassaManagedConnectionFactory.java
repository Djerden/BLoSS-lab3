package com.djeno.lab1.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import lombok.Getter;
import lombok.Setter;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

@Getter
@Setter
@ConnectionDefinition(
        connectionFactory = YookassaConnectionFactory.class,
        connectionFactoryImpl = YookassaConnectionFactoryImpl.class,
        connection = YookassaConnection.class,
        connectionImpl = YookassaConnectionImpl.class
)
public class YookassaManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation {

    private String shopId;
    private String apiKey;
    private ResourceAdapter ra;



    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new YookassaConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new YookassaConnectionImpl();
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxReqInfo) throws ResourceException {
        return new YookassaManagedConnection(
        );
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connections, Subject subject, ConnectionRequestInfo cxReqInfo) throws ResourceException {
        for (Object mc : connections) {
            if (mc instanceof YookassaManagedConnection) {
                return (ManagedConnection) mc;
            }
        }
        return null;
    }

    @Override public void setLogWriter(PrintWriter out) throws ResourceException {
    }
    @Override public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.ra;
    }

    @Override public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        if (this.ra != null && !(ra instanceof YookassaResourceAdapter)) {
            throw new ResourceException("Invalid resource adapter provided");
        }
        this.ra = (YookassaResourceAdapter) ra;
    }

}
