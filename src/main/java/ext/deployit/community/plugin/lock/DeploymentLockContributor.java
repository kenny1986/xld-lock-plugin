/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package ext.deployit.community.plugin.lock;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;

/**
 * Write all in Java, it is cross-platform Create lock.Manager CI that can list logs and clear all locks (control tasks) Default is to use locking for each host
 * (find hostcontainer), can be turned off with synthetic property
 */
public class DeploymentLockContributor {
	private static final String CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY = "allowConcurrentDeployments";

	@Contributor
	public static void addDeploymentLockCheckStep(Deltas deltas, DeploymentPlanningContext ctx) {
		DeployedApplication deployedApplication = ctx.getDeployedApplication();
		Environment environment = deployedApplication.getEnvironment();

		Set<ConfigurationItem> cisToBeLocked = new HashSet<ConfigurationItem>();

		// Check whether locking is required:
		//
		// 1. on the DeployedApplication
		// 2. on the Environment
		// 3. on the individual containers
		if (shouldLockCI(deployedApplication)) {
			cisToBeLocked.add(deployedApplication);
		}
		
		if (shouldLockCI(environment)) {
			cisToBeLocked.add(environment);
		}

		cisToBeLocked.addAll(getContainersRequiringCheck(deltas));
		
		if (!cisToBeLocked.isEmpty()) {
			ctx.addStep(new AcquireAllLocksStep(new LockHelper(), cisToBeLocked));
		}
	}

	private static boolean shouldLockCI(ConfigurationItem ci) {
		return ci.hasProperty(CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY) &&
				FALSE.equals(ci.getProperty(CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY));
	}

	private static Set<Container> getContainersRequiringCheck(Deltas deltas) {
		Iterable<Container> containersInAction = transform(deltas.getDeltas(), new Function<Delta, Container>() {
			@Override
			public Container apply(Delta input) {
				return (input.getOperation() == Operation.DESTROY ? input.getPrevious().getContainer() : input.getDeployed().getContainer());
			}
		});

		HashSet<Container> containers = newHashSet(filter(containersInAction, new Predicate<Container>() {
			@Override
			public boolean apply(Container input) {
				// may be null
				return FALSE.equals(input.getProperty(CONCURRENT_DEPLOYMENTS_ALLOWED_PROPERTY));
			}
		}));
		return containers;
	}
}
