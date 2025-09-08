package com.djeno.lab1.jca;

import jakarta.resource.ResourceException;

public interface YookassaConnectionFactory {
    YookassaConnection getConnection() throws ResourceException;
}
