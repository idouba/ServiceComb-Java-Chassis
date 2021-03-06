/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.api.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.serviceregistry.config.InstancePropertiesLoader;
import io.servicecomb.serviceregistry.definition.DefinitionConst;

/**
 * Created by   on 2016/12/5.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroserviceInstance {
  // even disconnected from service center
  // instanceId will not be changed
  // when register to service center again, use the old instanceId.
  private String instanceId;

  private String serviceId;

  private List<String> endpoints = new ArrayList<>();

  private String hostName;

  private MicroserviceInstanceStatus status = MicroserviceInstanceStatus.UP;

  private Map<String, String> properties = new HashMap<>(); // reserved key list: region|az|stage|group

  private HealthCheck healthCheck;

  private String environment;

  private String stage;

  private DataCenterInfo dataCenterInfo;

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public List<String> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
  }

  public MicroserviceInstanceStatus getStatus() {
    return status;
  }

  public void setStatus(MicroserviceInstanceStatus status) {
    this.status = status;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public HealthCheck getHealthCheck() {
    return healthCheck;
  }

  public void setHealthCheck(HealthCheck healthCheck) {
    this.healthCheck = healthCheck;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  @Deprecated
  public String getStage() {
    return stage;
  }

  @Deprecated
  public void setStage(String stage) {
    this.stage = stage;
  }

  public DataCenterInfo getDataCenterInfo() {
    return dataCenterInfo;
  }

  public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
    this.dataCenterInfo = dataCenterInfo;
  }

  // Some properties of microservice instance are dynamic changed, not cover them all now.
  public static MicroserviceInstance createFromDefinition(Configuration configuration) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    // default hard coded values
    microserviceInstance.setStage(DefinitionConst.defaultStage);
    microserviceInstance
        .setEnvironment(configuration.getString(DefinitionConst.serviceEnvironmentKey, DefinitionConst.defaultEnvironment));
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.HEARTBEAT);
    microserviceInstance.setHealthCheck(healthCheck);

    // load properties
    Map<String, String> propertiesMap = InstancePropertiesLoader.INSTANCE.loadProperties(configuration);
    microserviceInstance.setProperties(propertiesMap);

    // load data center information
    loadDatacenterInfo(microserviceInstance);
    return microserviceInstance;
  }

  private static void loadDatacenterInfo(MicroserviceInstance microserviceInstance) {
    String dataCenterName = DynamicPropertyFactory.getInstance()
        .getStringProperty("cse.datacenter.name", null)
        .get();
    if (StringUtils.isNotEmpty(dataCenterName)) {
      DataCenterInfo dataCenterInfo = new DataCenterInfo();
      dataCenterInfo.setName(dataCenterName);
      dataCenterInfo
          .setRegion(DynamicPropertyFactory.getInstance().getStringProperty("cse.datacenter.region", null).get());
      dataCenterInfo.setAvailableZone(
          DynamicPropertyFactory.getInstance().getStringProperty("cse.datacenter.availableZone", null).get());
      microserviceInstance.setDataCenterInfo(dataCenterInfo);
    }
  }
}
