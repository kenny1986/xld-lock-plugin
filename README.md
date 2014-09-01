# Lock plugin #

This document describes the functionality provided by the Lock plugin.

See the **XL Deploy Reference Manual** for background information on XL Deploy and deployment concepts.

## Overview

The Lock plugin is a XL Deploy plugin that adds capabilities for preventing simultaneous deployments.

###Features

* Lock a specific environment / application combination for exclusive use by one deployment
* Lock a complete environment for exclusive use by one deployment
* Lock specific containers for exclusive use by one deployment
* List and clear locks using a lock manager CI

## Requirements

* **XL Deploy requirements**
	* **XL Deploy**: version 4.0+
	* **Other XL Deploy Plugins**: None

## Installation

Place the plugin JAR file into your `SERVER_HOME/plugins` directory. 

## Locking deployments

When a deployment is configured, the Lock plugin examines the CIs involved in the deployment to determine whether any of them must be locked for exclusive use. If so,
it contributes a step to the beginning of the deployment plan to acquire the required locks. If the necessary locks can't be obtained, the deployment will enter a PAUSE 
state and can be continued at a later time.

If lock acquisition is successful, the deployment will continue to execute. During a deployment, the locks are retained, even if the deployment fails and requires 
manual intervention. When the deployment finishes (either successfully or is aborted), the locks will be released.

## Configuration

The locks plugin adds synthetic properties to specific CIs in XL Deploy that are used to control locking behavior. The following CIs can be locked:

* *udm.DeployedApplication*: this ensures that only one depoyment of a particular application to an environment can be in progress at once
* *udm.Environment*: this ensures that only one depoyment to a particular environment can be in progress at once
* *udm.Container*: this ensures that only one depoyment can use the specific container at once

Each of the above CIs has the following synthetic property added:

* *allowConcurrentDeployments* (default: true): indicates whether concurrent deployments are allowed. If false, the Lock plugin will lock the CI prior to a deployment.

## Implementation

Each lock is stored as a file in a directory under the XL Deploy installation directory. The _lock.Manager_ CI can be created in the _Infrastructure_ section of XL Deploy to list and clear all of the current locks.
