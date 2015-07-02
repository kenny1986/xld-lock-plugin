/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package ext.deployit.community.plugin.lock;

import java.io.Serializable;
import java.util.Set;

import com.xebialabs.deployit.engine.spi.execution.ExecutionStateListener;
import com.xebialabs.deployit.engine.spi.execution.StepExecutionStateEvent;
import com.xebialabs.deployit.engine.spi.execution.TaskExecutionStateEvent;
import static com.xebialabs.deployit.engine.api.execution.TaskExecutionState.*;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;

public class LockCleanupListener implements ExecutionStateListener, Serializable {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LockCleanupListener.class);

	private final LockHelper lockHelper;
	private final Set<ConfigurationItem> cisToBeUnlocked;

	public LockCleanupListener(LockHelper lockHelper, Set<ConfigurationItem> cisToBeUnlocked) {
		this.lockHelper = lockHelper;
		this.cisToBeUnlocked = cisToBeUnlocked;
	}
	
	@Override
	public void stepStateChanged(StepExecutionStateEvent event) {

		logger.info("Step event: {}", event);
	}

	@Override
	public void taskStateChanged(TaskExecutionStateEvent event) {
		logger.info("Task event: {}", event);

		switch(event.currentState()) {
			case CANCELLED:
			case DONE:
				lockHelper.unlock(cisToBeUnlocked);
				break;
			default:
				break;
		}
	}

}
