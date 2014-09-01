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
	private final LockHelper lockHelper;

	public AcquireAllLocksStep(LockHelper lockHelper, Set<ConfigurationItem> cisToBeLocked) {
		this.lockHelper = lockHelper;
		this.cisToBeLocked = cisToBeLocked;
	}

	@Override
	public StepExitCode execute(ExecutionContext context) throws Exception {
		context.logOutput("Attempting to acquire locks on CIs " + cisToBeLocked);
		
		if (lockHelper.atomicallyLock(cisToBeLocked)) {
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
