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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.linkedin.d2.balancer.LoadBalancerClusterListener;
import com.linkedin.d2.balancer.LoadBalancerState;
import com.linkedin.d2.balancer.LoadBalancerStateItem;
import com.linkedin.d2.balancer.properties.ClusterFailoutProperties;
import com.linkedin.d2.balancer.properties.ClusterStoreProperties;

/**
 * Class responsible for providing cluster failout config for each cluster.
 */
public abstract class ZKClusterFailoutConfigProvider<T extends ClusterFailoutConfig> implements ClusterFailoutConfigProvider<T>, LoadBalancerClusterListener
{
  private final Map<String, FailedoutClusterManager<T>> _failedoutClusters = new HashMap<>();
  private final LoadBalancerState _loadBalancerState;

  public ZKClusterFailoutConfigProvider(@Nonnull LoadBalancerState loadBalancerState)
  {
    _loadBalancerState = loadBalancerState;
  }

  public void start()
  {
    _loadBalancerState.registerClusterListener(this);
  }

  public void stop()
  {
    _loadBalancerState.unregisterClusterListener(this);
  }

  /**
   * Converts {@link ClusterStoreProperties} into a {@link ClusterFailoutConfig} that will be used for routing requests.
   * @param clusterFailoutProperties The properties defined for a cluster failout.
   * @return Parsed and processed config that's ready to be used for routing requests.
   */
  public abstract @Nullable
  T createFailoutConfig(@Nullable ClusterFailoutProperties clusterFailoutProperties);

  @Override
  public Optional<T> getFailoutConfig(String clusterName)
  {
    final FailedoutClusterManager<T> failedoutClusterManager = _failedoutClusters.get(clusterName);
    if (failedoutClusterManager == null)
    {
      return Optional.empty();
    }
    return failedoutClusterManager.getFailoutConfig();
  }

  @Override
  public void onClusterAdded(String clusterName)
  {
    LoadBalancerStateItem<ClusterFailoutProperties> item = _loadBalancerState.getClusterFailoutProperties(clusterName);
    if (item != null)
    {
      final ClusterFailoutProperties failoutProperties = item.getProperty();

      final T failoutConfig = createFailoutConfig(failoutProperties);
      _failedoutClusters.computeIfAbsent(clusterName, name -> new FailedoutClusterManager<>(clusterName, _loadBalancerState))
        .updateFailoutConfig(failoutConfig);
    }
  }

  @Override
  public void onClusterRemoved(String clusterName)
  {
    FailedoutClusterManager<T> manager = _failedoutClusters.remove(clusterName);
    if (manager != null)
    {
      manager.updateFailoutConfig(null);
    }
  }
}
