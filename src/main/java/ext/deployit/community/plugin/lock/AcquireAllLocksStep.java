/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package ext.deployit.community.plugin.lock;

import java.util.Set;

import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;

public class AcquireAllLocksStep implements Step {

	private static final int ACQUIRE_LOCKS_ORDER = 2;
	private final Set<ConfigurationItem> cisToBeLocked;
	private boolean enableRetry;
	private int retryInSeconds;
	private int retryLimit;
	private final LockHelper lockHelper;

	public AcquireAllLocksStep(LockHelper lockHelper, Set<ConfigurationItem> cisToBeLocked, boolean enableRetry, int retryInSeconds, int retryLimit) {
		this.lockHelper = lockHelper;
		this.cisToBeLocked = cisToBeLocked;
		this.enableRetry = enableRetry;
		this.retryInSeconds = retryInSeconds;
		this.retryLimit = retryLimit;
	}

	@Override
	public StepExitCode execute(ExecutionContext context) throws Exception {
		context.logOutput("Attempting to acquire locks on CIs " + cisToBeLocked);


		boolean locked = lockHelper.atomicallyLock(cisToBeLocked);
		if (!locked && enableRetry) {
			int retryCount = 0;
			while(!locked && retryCount < retryLimit ) {
				context.logOutput("Will retry in "+ retryInSeconds +" seconds");
				Thread.sleep(retryInSeconds*1000);
				context.logOutput("Attempting to acquire locks on CIs " + cisToBeLocked);
				locked = lockHelper.atomicallyLock(cisToBeLocked);
				retryCount++;
			}
		}
		if (locked) {
			context.logOutput("All locks acquired");
			context.setAttribute("lockCleanupListener", new LockCleanupListener(lockHelper, cisToBeLocked));
			return StepExitCode.SUCCESS;
		} else {
			context.logError("Failed to acquire one or more locks");
			return StepExitCode.PAUSE;
		}
	}

	@Override
	public String getDescription() {
		return "Acquiring locks for the following CIs: " + cisToBeLocked.toString();
	}

	@Override
	public int getOrder() {
		return ACQUIRE_LOCKS_ORDER;
	}

}
