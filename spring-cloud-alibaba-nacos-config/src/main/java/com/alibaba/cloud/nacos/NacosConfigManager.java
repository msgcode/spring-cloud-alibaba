/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosConfigManager {

	private static ConfigService service = null;

	@Autowired
	private NacosConfigProperties properties;

	@Autowired
	private Environment environment;

	private ConfigService configService;

	/**
	 * @see NacosConfigManager#getConfigService() .
	 * @return ConfigService
	 */
	public ConfigService configServiceInstance() {
		return configService;
	}

	public void initConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public ConfigService getConfigService() {
		if (service == null) {
			try {
				service = NacosFactory
						.createConfigService(properties.getConfigServiceProperties());
				initConfigService(service);
			}
			catch (NacosException e) {
				throw new NacosConnectionFailureException(properties.getServerAddr(),
						e.getMessage(), e);
			}
		}
		return service;
	}

	@PostConstruct
	public void init() {
		this.overrideFromEnv();
	}

	private void overrideFromEnv() {
		if (StringUtils.isEmpty(properties.getServerAddr())) {
			String serverAddr = environment
					.resolvePlaceholders("${spring.cloud.nacos.config.server-addr:}");
			if (StringUtils.isEmpty(serverAddr)) {
				serverAddr = environment
						.resolvePlaceholders("${spring.cloud.nacos.server-addr:}");
			}
			properties.setServerAddr(serverAddr);
		}
	}

}