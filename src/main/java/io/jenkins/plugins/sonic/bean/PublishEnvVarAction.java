/*
 *  Copyright (C) [SonicCloudOrg] Sonic Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.jenkins.plugins.sonic.bean;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;

public class PublishEnvVarAction extends InvisibleAction implements EnvironmentContributingAction {
	/**
	 * The environment variable key.
	 */
	private final String key;

	/**
	 * The environment variable value.
	 */
	private final String value;

	/**
	 * Constructor.
	 *
	 * @param key   the environment variable key
	 * @param value the environment variable value
	 */
	public PublishEnvVarAction(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see hudson.model.EnvironmentContributingAction#buildEnvVars(hudson.model.AbstractBuild, hudson.EnvVars)
	 */
	@Override
	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
		env.put(key, value);
	}
}
