## equinox-test-distribution-experiment
This repository demonstrates how Eclipse Equinox environment can be set up on a remote Test Distribution agent.

### Structure
* [equinox-configuration](equinox-configuration): simulates Equinox configuration directory with `config.ini` and `bundles.info` files.
* [equinox-framework](equinox-framework): simulates OSGi framework JARs, those usually live in the Eclipse home directory.
* [external-osgi-bundles](external-osgi-bundles): simulates external dependencies coming from other modules, Gradle cache etc.
* [EquinoxSimulator](lib/src/test/java/io/github/pshevche/equinox/EquinoxSimulator.java): simulates the Equinox bundles launcher. It reads the `config.ini` file passed as an argument, and loads the content of all bundles into an `EquinoxWorkspace` object.
* [*BundlesAvailabilityTest](lib/src/test/java/io/github/pshevche/equinox/BaseBundlesAvailabilityTest.java): simulates tests interacting with started bundles. Tests will assert that the content of the bundle is accessible on remote and local executors.

### Examples
* [Using internal functionality to remap bundle paths](https://github.com/pshevche/equinox-test-distribution-experiment/tree/remap-equinox-config): this branch demonstrates how to transfer all bundles required to run tests to the remote agents, and how to update references to them in `config.ini` and `bundles.info` files.
* [Transferring bundles files without tracking them as inputs](https://github.com/pshevche/equinox-test-distribution-experiment/tree/remap-equinox-config-single-directory): declaring all bundles as inputs can hinder Gradle's caching. This branch demonstrates how to transfer files to the remote agent without declaring them as inputs.
* [Transferring workspace content as a single folder](https://github.com/pshevche/equinox-test-distribution-experiment/tree/remap-equinox-config-single-directory): this branch demonstrates how to package all workspace content as a single workspace, which will be transferred as a single zipped entry. This increases the file transfer times.

### How does this example work?
1. Declare bundles and configuration files referencing bundles via their absolute paths as `processedResources` (see [builds.gradle.kts L34-46](lib/build.gradle.kts)). All processed resources will be transferred to the agent, but Gradle won't consider them as task inputs. Test Distribution will replace all paths that it can detect in all files with their locations on the remote agent.
