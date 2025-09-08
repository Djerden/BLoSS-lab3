package com.djeno.lab1.jca;

import jakarta.resource.cci.Connection;

public interface YookassaConnection extends Connection {
    String createPayment(Long amount, Long orderId);

}
