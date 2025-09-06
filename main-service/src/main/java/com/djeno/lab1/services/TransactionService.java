package com.djeno.lab1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransactionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final JtaTransactionManager jtaTransactionManager;

    public TransactionStatus begin(String transactionName, int timeoutSec) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(transactionName);
        def.setTimeout(timeoutSec);
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = jtaTransactionManager.getTransaction(def);

        log.info("Transaction [{}] started with timeout {} sec, active: {}", transactionName, timeoutSec, !status.isCompleted());
        return status;
    }

    public void commit(TransactionStatus status) {
        if (!status.isCompleted()) {
            jtaTransactionManager.commit(status);
            log.info("Transaction [{}] committed successfully", status.getTransactionName());
        }
    }

    public void rollback(TransactionStatus status) {
        if (!status.isCompleted()) {
            jtaTransactionManager.rollback(status);
            log.warn("Transaction [{}] rolled back", status.getTransactionName());
        }
    }

    public <T> T execute(String txName, int timeoutSec, TransactionCallback<T> action) {
        TransactionStatus status = null;
        try {
            status = begin(txName, timeoutSec);
            T result = action.doInTransaction(status);
            commit(status);
            return result;
        } catch (Exception e) {
            if (status != null) {
                rollback(status);
            }
            log.error("Transaction [{}] failed, rolling back. Root cause: {}", txName, e.getMessage(), e);
            throw e;
        }
    }

    public <T> T execute(TransactionDefinition def, TransactionCallback<T> action) {
        TransactionStatus status = null;
        try {
            status = jtaTransactionManager.getTransaction(def);
            log.info("Transaction [{}] started with timeout {} sec, active: {}", def.getName(), def.getTimeout(), !status.isCompleted());
            T result = action.doInTransaction(status);
            commit(status);
            return result;
        } catch (Exception e) {
            if (status != null) {
                rollback(status);
            }
            log.error("Transaction [{}] failed, rolling back. Root cause: {}", def.getName(), e.getMessage(), e);
            throw e;
        }
    }
}
