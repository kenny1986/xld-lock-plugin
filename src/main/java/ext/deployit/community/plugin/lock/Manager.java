/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package ext.deployit.community.plugin.lock;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;
import com.xebialabs.deployit.plugin.api.udm.ControlTask;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

/**
 * Lock manager CI that provides control tasks to list and clear locks.
 */
@SuppressWarnings("serial")
@Metadata(root = Metadata.ConfigurationItemRoot.INFRASTRUCTURE, virtual = false, description = "Manager for container locks")
public class Manager extends BaseContainer {

	@SuppressWarnings("rawtypes")
    @ControlTask(description="Clears all locks")
	public List<Step> clearLocks() {
		Step clearLocksStep = new Step() {

			@Override
			public String getDescription() {
				return "Clearing all locks";
			}

			@Override
			public StepExitCode execute(ExecutionContext arg0) throws Exception {
				new LockHelper().clearLocks();
				return StepExitCode.SUCCESS;
			}

			@Override
			public int getOrder() {
				return 0;
			}
		};
		
		return newArrayList(clearLocksStep);
	}
	
	@SuppressWarnings("rawtypes")
    @ControlTask(description="Lists all locks")
	public List<Step> listLocks() {
		Step listLocksStep = new Step() {

			@Override
			public String getDescription() {
				return "Listing all locks";
			}

			@Override
			public StepExitCode execute(ExecutionContext ctx) throws Exception {
				ctx.logOutput("The following CIs are currently locked:");

				List<String> locksListing = new LockHelper().listLocks();
				if (locksListing.isEmpty()) {
					ctx.logOutput("<none>");
				} else {
					for (String string : locksListing) {
						ctx.logOutput("- " + string);
					}
				}
				
				return StepExitCode.SUCCESS;
			}

			@Override
			public int getOrder() {
				return 0;
			}
		};
		
		return newArrayList(listLocksStep);
	}
}
