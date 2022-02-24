/*
   Copyright (c) 2022 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.linkedin.d2.balancer.clusterfailout;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import com.linkedin.d2.balancer.LoadBalancerState;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FailedoutClusterManagerTest
{
  private final static String CLUSTER_NAME = "Cluster";
  private final static String PEER_CLUSTER_NAME1 = "ClusterPeer1";
  private final static String PEER_CLUSTER_NAME2 = "ClusterPeer2";

  @Mock
  private LoadBalancerState _loadBalancerState;

  private FailedoutClusterManager<ClusterFailoutConfig> _manager;

  @BeforeMethod
  public void setup()
  {
    MockitoAnnotations.initMocks(this);
    _manager = new FailedoutClusterManager<>(CLUSTER_NAME, _loadBalancerState);
  }

  @Test
  public void testAddPeerClusterWatches()
  {
    _manager.addPeerClusterWatches(new HashSet<>(Arrays.asList(PEER_CLUSTER_NAME1, PEER_CLUSTER_NAME2)));
    verify(_loadBalancerState).listenToCluster(eq(PEER_CLUSTER_NAME1), any());
    verify(_loadBalancerState).listenToCluster(eq(PEER_CLUSTER_NAME2), any());
  }

  @Test
  public void testAddPeerClusterWatchesWithPeerClusterAdded()
  {
    _manager.addPeerClusterWatches(new HashSet<>(Arrays.asList(PEER_CLUSTER_NAME1)));
    _manager.addPeerClusterWatches(new HashSet<>(Arrays.asList(PEER_CLUSTER_NAME1, PEER_CLUSTER_NAME2)));
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME2), any());
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME1), any());
  }

  @Test
  public void testAddPeerClusterWatchesWithPeerClusterRemoved()
  {
    _manager.addPeerClusterWatches(new HashSet<>(Arrays.asList(PEER_CLUSTER_NAME1, PEER_CLUSTER_NAME2)));
    _manager.addPeerClusterWatches(new HashSet<>(Arrays.asList(PEER_CLUSTER_NAME1)));
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME1), any());
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME2), any());

    // TODO(RESILIEN-51): Unregister watch for PEER_CLUSTER_NAME2
  }

  @Test
  public void testUpdateFailoutConfigWithNull() {
    _manager.updateFailoutConfig(null);
    verify(_loadBalancerState, never()).listenToCluster(any(), any());
    assertFalse(_manager.getFailoutConfig().isPresent());
  }

  @Test
  public void testUpdateFailoutConfigWithoutActiveFailout() {
    ClusterFailoutConfig config = mock(ClusterFailoutConfig.class);
    when(config.isFailedOut()).thenReturn(false);
    when(config.getPeerClusters()).thenReturn(Collections.singleton(PEER_CLUSTER_NAME1));
    _manager.updateFailoutConfig(config);
    verify(_loadBalancerState, never()).listenToCluster(any(), any());
    assertTrue(_manager.getFailoutConfig().isPresent());
  }

  @Test
  public void testUpdateFailoutConfigWithActiveFailout() {
    ClusterFailoutConfig config = mock(ClusterFailoutConfig.class);
    when(config.isFailedOut()).thenReturn(true);
    when(config.getPeerClusters()).thenReturn(Collections.singleton(PEER_CLUSTER_NAME1));
    _manager.updateFailoutConfig(config);
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME1), any());
    assertTrue(_manager.getFailoutConfig().isPresent());
  }

  @Test
  public void testUpdateFailoutConfigUpdate() {
    ClusterFailoutConfig config = mock(ClusterFailoutConfig.class);
    when(config.isFailedOut()).thenReturn(true);
    when(config.getPeerClusters()).thenReturn(Collections.singleton(PEER_CLUSTER_NAME1));
    _manager.updateFailoutConfig(config);
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME1), any());
    when(config.getPeerClusters()).thenReturn(new HashSet<>(Arrays.asList(PEER_CLUSTER_NAME1, PEER_CLUSTER_NAME2)));
    _manager.updateFailoutConfig(config);
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME2), any());
    assertTrue(_manager.getFailoutConfig().isPresent());
  }

  @Test
  public void testUpdateFailoutConfigUpdateToNull() {
    ClusterFailoutConfig config = mock(ClusterFailoutConfig.class);
    when(config.isFailedOut()).thenReturn(true);
    when(config.getPeerClusters()).thenReturn(Collections.singleton(PEER_CLUSTER_NAME1));
    _manager.updateFailoutConfig(config);
    assertTrue(_manager.getFailoutConfig().isPresent());
    verify(_loadBalancerState, times(1)).listenToCluster(eq(PEER_CLUSTER_NAME1), any());
    _manager.updateFailoutConfig(null);
    // TODO(RESILIEN-51): Verify unregister watch for PEER_CLUSTER_NAME1
    assertFalse(_manager.getFailoutConfig().isPresent());
  }
}
