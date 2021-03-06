/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.ozone;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdds.conf.OzoneConfiguration;
import org.apache.hadoop.hdds.scm.StorageContainerManager;
import org.apache.hadoop.ozone.client.OzoneClient;
import org.apache.hadoop.ozone.ksm.KeySpaceManager;
import org.apache.hadoop.hdds.scm.protocolPB
    .StorageContainerLocationProtocolClientSideTranslatorPB;
import org.apache.hadoop.test.GenericTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Interface used for MiniOzoneClusters.
 */
public interface MiniOzoneCluster {

  /**
   * Returns the configuration object associated with the MiniOzoneCluster.
   *
   * @return Configuration
   */
  Configuration getConf();

  /**
   * Waits for the cluster to be ready, this call blocks till all the
   * configured {@link HddsDatanodeService} registers with
   * {@link StorageContainerManager}.
   *
   * @throws TimeoutException In case of timeout
   * @throws InterruptedException In case of interrupt while waiting
   */
  void waitForClusterToBeReady() throws TimeoutException, InterruptedException;

  /**
   * Waits/blocks till the cluster is out of chill mode.
   *
   * @throws TimeoutException TimeoutException In case of timeout
   * @throws InterruptedException In case of interrupt while waiting
   */
  void waitTobeOutOfChillMode() throws TimeoutException, InterruptedException;

  /**
   * Returns {@link StorageContainerManager} associated with this
   * {@link MiniOzoneCluster} instance.
   *
   * @return {@link StorageContainerManager} instance
   */
  StorageContainerManager getStorageContainerManager();

  /**
   * Returns {@link KeySpaceManager} associated with this
   * {@link MiniOzoneCluster} instance.
   *
   * @return {@link KeySpaceManager} instance
   */
  KeySpaceManager getKeySpaceManager();

  /**
   * Returns the list of {@link HddsDatanodeService} which are part of this
   * {@link MiniOzoneCluster} instance.
   *
   * @return List of {@link HddsDatanodeService}
   */
  List<HddsDatanodeService> getHddsDatanodes();

  /**
   * Returns an {@link OzoneClient} to access the {@link MiniOzoneCluster}.
   *
   * @return {@link OzoneClient}
   * @throws IOException
   */
  OzoneClient getClient() throws IOException;

  /**
   * Returns an RPC based {@link OzoneClient} to access the
   * {@link MiniOzoneCluster}.
   *
   * @return {@link OzoneClient}
   * @throws IOException
   */
  OzoneClient getRpcClient() throws IOException;

  /**
   * Returns an REST based {@link OzoneClient} to access the
   * {@link MiniOzoneCluster}.
   *
   * @return {@link OzoneClient}
   * @throws IOException
   */
  OzoneClient getRestClient() throws IOException;

  /**
   * Returns StorageContainerLocationClient to communicate with
   * {@link StorageContainerManager} associated with the MiniOzoneCluster.
   *
   * @return StorageContainerLocation Client
   * @throws IOException
   */
  StorageContainerLocationProtocolClientSideTranslatorPB getStorageContainerLocationClient()
      throws IOException;

  /**
   * Restarts StorageContainerManager instance.
   *
   * @throws IOException
   */
  void restartStorageContainerManager() throws IOException;

  /**
   * Restarts KeySpaceManager instance.
   *
   * @throws IOException
   */
  void restartKeySpaceManager() throws IOException;

  /**
   * Restart a particular HddsDatanode.
   *
   * @param i index of HddsDatanode in the MiniOzoneCluster
   */
  void restartHddsDatanode(int i);

  /**
   * Shutdown a particular HddsDatanode.
   *
   * @param i index of HddsDatanode in the MiniOzoneCluster
   */
  void shutdownHddsDatanode(int i);

  /**
   * Shutdown the MiniOzoneCluster.
   */
  void shutdown();

  /**
   * Returns the Builder to construct MiniOzoneCluster.
   *
   * @param conf OzoneConfiguration
   *
   * @return MiniOzoneCluster builder
   */
  static Builder newBuilder(OzoneConfiguration conf) {
    return new MiniOzoneClusterImpl.Builder(conf);
  }

  /**
   * Builder class for MiniOzoneCluster.
   */
  abstract class Builder {

    protected static final int DEFAULT_HB_INTERVAL_MS = 1000;
    protected static final int DEFAULT_HB_PROCESSOR_INTERVAL_MS = 100;

    protected final OzoneConfiguration conf;
    protected final String path;

    protected String clusterId;

    protected Optional<Boolean> enableTrace = Optional.of(false);
    protected Optional<Integer> hbInterval = Optional.empty();
    protected Optional<Integer> hbProcessorInterval = Optional.empty();
    protected Optional<String> scmId = Optional.empty();
    protected Optional<String> ksmId = Optional.empty();

    protected Boolean ozoneEnabled = true;
    protected Boolean randomContainerPort = true;

    // Use relative smaller number of handlers for testing
    protected int numOfKsmHandlers = 20;
    protected int numOfScmHandlers = 20;
    protected int numOfDatanodes = 1;

    protected Builder(OzoneConfiguration conf) {
      this.conf = conf;
      this.clusterId = UUID.randomUUID().toString();
      this.path = GenericTestUtils.getTempPath(
          MiniOzoneClusterImpl.class.getSimpleName() + "-" + clusterId);
    }

    /**
     * Sets the cluster Id.
     *
     * @param id cluster Id
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setClusterId(String id) {
      clusterId = id;
      return this;
    }

    /**
     * Sets the SCM id.
     *
     * @param id SCM Id
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setScmId(String id) {
      scmId = Optional.of(id);
      return this;
    }

    /**
     * Sets the KSM id.
     *
     * @param id KSM Id
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setKsmId(String id) {
      ksmId = Optional.of(id);
      return this;
    }

    /**
     * If set to true container service will be started in a random port.
     *
     * @param randomPort enable random port
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setRandomContainerPort(boolean randomPort) {
      randomContainerPort = randomPort;
      return this;
    }

    /**
     * Sets the number of HddsDatanodes to be started as part of
     * MiniOzoneCluster.
     *
     * @param val number of datanodes
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setNumDatanodes(int val) {
      numOfDatanodes = val;
      return this;
    }


    /**
     * Sets the number of HeartBeat Interval of Datanodes, the value should be
     * in MilliSeconds.
     *
     * @param val HeartBeat interval in milliseconds
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setHbInterval(int val) {
      hbInterval = Optional.of(val);
      return this;
    }

    /**
     * Sets the number of HeartBeat Processor Interval of Datanodes,
     * the value should be in MilliSeconds.
     *
     * @param val HeartBeat Processor interval in milliseconds
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setHbProcessorInterval(int val) {
      hbProcessorInterval = Optional.of(val);
      return this;
    }

    /**
     * When set to true, enables trace level logging.
     *
     * @param trace true or false
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder setTrace(Boolean trace) {
      enableTrace = Optional.of(trace);
      return this;
    }

    /**
     * Modifies the configuration such that Ozone will be disabled.
     *
     * @return MiniOzoneCluster.Builder
     */
    public Builder disableOzone() {
      ozoneEnabled = false;
      return this;
    }

    /**
     * Constructs and returns MiniOzoneCluster.
     *
     * @return {@link MiniOzoneCluster}
     *
     * @throws IOException
     */
    public abstract MiniOzoneCluster build() throws IOException;
  }
}