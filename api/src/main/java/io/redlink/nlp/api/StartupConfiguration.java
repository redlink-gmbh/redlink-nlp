/*******************************************************************************
 * Copyright (c) 2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.redlink.nlp.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String STARTUP_THREAD_POOL_NAME = "startup";

    @Value("${startup.poolSize:-1}")
    private int poolSize = -1;

    private ExecutorService startupThreadPool;

    @Bean(name = STARTUP_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = STARTUP_THREAD_POOL_NAME)
    protected ExecutorService getStartupExecutorService() {
        if (poolSize < 1) {
            try {
                poolSize = Runtime.getRuntime().availableProcessors();
            } catch (RuntimeException e) {
                log.warn("Unable to lookup the number of available processors via Runtime ({} : {})", e.getClass().getSimpleName(), e.getMessage());
            } finally {
                if (poolSize < 1) {
                    poolSize = 1;
                }
            }
        }
        if (startupThreadPool == null) {
            synchronized (this) {
                if (startupThreadPool == null) {
                    startupThreadPool = Executors.newFixedThreadPool(poolSize);
                }
            }
        }
        return startupThreadPool;
    }

}
