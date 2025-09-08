package com.djeno.lab1.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;

public class YookassaManagedConnection implements ManagedConnection {

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        YookassaConnectionImpl connection = new YookassaConnectionImpl();
        return connection;
    }

    @Override
    public void destroy() throws ResourceException {

    }

    @Override
    public void cleanup() throws ResourceException {

    }

    @Override
    public void associateConnection(Object o) throws ResourceException {

    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {

    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {

    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws ResourceException {

    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }
}
