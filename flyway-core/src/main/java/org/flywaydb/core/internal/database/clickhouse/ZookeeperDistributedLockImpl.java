/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.clickhouse;


import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.mysql.MySQLNamedLockTemplate;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import javax.annotation.concurrent.ThreadSafe;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@ThreadSafe
public class ZookeeperDistributedLockImpl {

    private static final Log LOG = LogFactory.getLog(MySQLNamedLockTemplate.class);
    public static final String FLYWAY_NODE_PREFIX = "/flyway-";

    private final ZkClient zkClient;
    private final String lockName;

    private CountDownLatch countDownLatch = null;

    public ZookeeperDistributedLockImpl(String zookeeperPath, String dbName) {
        this.zkClient = new ZkClient(zookeeperPath);
        lockName = FLYWAY_NODE_PREFIX + dbName;
    }

    /**
     * Executes this callback with a named lock.
     *
     * @param callable The callback to execute.
     * @return The result of the callable code.
     */
    public <T> T execute(Callable<T> callable) {
        try {
            getLock();
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to acquire Clickhouse named lock: " + lockName, e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new FlywayException(e);
        } finally {
            unlock();
        }
    }

    public void getLock() {
        if (lock()) {
            LOG.debug("Lock acquired successfully lockName: " + lockName);
        } else {
            waitLock();
            getLock();
        }
    }

    // Try to get the lock
    private boolean lock() {
        try {
            zkClient.createEphemeral(lockName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Listen to a node, anonymous callback function to achieve monitoring of node information changes,
    public void waitLock() {

        //Once zookeeper detects changes in node information, it will trigger an anonymous anonymous callback
        IZkDataListener iZkDataListener = new IZkDataListener() {

            public void handleDataDeleted(String path) throws Exception {
                //wake up the thread waiting
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }

            public void handleDataChange(String path, Object data) throws Exception {
                // Node only delete, change ignore
            }
        };

        // Register event listener
        zkClient.subscribeDataChanges(lockName, iZkDataListener);

        // If the node exists, you need to wait until you receive the event notification
        if (zkClient.exists(lockName)) {
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zkClient.unsubscribeDataChanges(lockName, iZkDataListener);
    }

    // release the lock
    public void unlock() {
        zkClient.delete(lockName);
        zkClient.close();
        LOG.debug("Unlock successfully lockName: " + lockName);
    }
}
