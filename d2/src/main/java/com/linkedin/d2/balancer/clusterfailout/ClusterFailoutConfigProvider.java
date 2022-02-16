/*
   Copyright (c) 2012 LinkedIn Corp.

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

import java.util.Optional;


/**
 * Class responsible for providing cluster failout config for each cluster.
 */
public interface ClusterFailoutConfigProvider<T extends ClusterFailoutConfig> {
  /**
   * Gets the failout config for a cluster.
   * @param clusterName The name of the cluster to get failout config for.
   * @return Corresponding failout config if cluster has an associated failed out config.
   */
  Optional<T> getFailoutConfig(String clusterName);
}
