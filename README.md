# **APEX ASPIRE**

If you are reading this it is because you are part of a privileged few, whom I trust very much.  This repo has been cloned from the original Kotlin version of the CorDapp template. The Java equivalent is
[here](https://github.com/corda/cordapp-template-java/).**

#### Set up Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

#### Starting all the nodes at the same time

To start all the nodes and webservers at the same time, cd to the Aspire main folder and execute the following:


#### To deploy in Linux:

```./gradlew clean deployNodes``` Deploys Nodes

```cd build/nodes``` Navigate to nodes folder

```./runnodes``` Runs the nodes

```cd Aspire``` Navigate back to main folder

```./gradlew runTemplateClient``` Deploys Webservers


The clean switch starts fresh nodes, clearing the vault data that may be left from any previous nodes

#### To deploy in windows:

```gradlew.bat clean deployNodes``` Deploys Nodes

```cd build/nodes``` Navigate to nodes folder

```runnodes``` Runs the nodes

```cd Aspire``` Navigate back to main folder

```gradlew.bat runTemplateClient``` Deploys Webservers


#### Starting node's individually

Occassionally node will fail to start.  If this is the case we can run node's individually by opening a terminal window within the node’s folder and running:

```java -jar corda.jar```

## The Spring Webserver

`clients/src/main/kotlin/com/template/webserver/` defines a simple Spring webserver that connects to a node via RPC and allows you to interact with the node over HTTP.

The API endpoints are defined here:

     clients/src/main/kotlin/com/template/webserver/Controller.kt

And a static webpage is defined here:

     clients/src/main/resources/static/


By default, it connects to the node with RPC address `localhost:10006` with the username `user1` and the password `test`.

#### Interacting with the webserver

The static webpage is served on:

    http://localhost:10050

While the sole template endpoint is served on:

    http://localhost:10050/templateendpoint


### OUR DEMO:

#### A Broker requests a quote from an Insurer (creating a Proposal) 

In our cordapp any node can create a proposal to any other node.  The node that is making the proposal is the _proposer_ and the node that is receiving the proposal is the _proposee_.  Please note that these role's change each time a proposal is sent back or forth.  The commands below must be executed from the relevant node shell.  Change the amounts, parties as required, and be sure to add the correct linear ID (you can access this by doing a vault query, more below).

CREATING A PROPOSAL:

``ProposalFlow$Initiator``

This flow creates a proposal to Insurer C, this proposal will have the data Amount = 10, and isBuyer = true.

In the Broker shell execute the following command:

```flow start ProposalFlow$Initiator isBuyer: true, amount: 10, counterparty: InsurerC```


VIEWING A PROPOSAL:

Now go to InsurerC shell and execute the command below:

```run vaultQuery contractStateType: negotiation.contracts.ProposalState```


This allow's us to view the proposal in Insurer C's vault.  This flow can be executed from any shell to allow us to view the vault contents.  Note down the linear ID (its near the top of the state data).

MODIFYING A PROPOSAL:

Now assume that Insurer C wishes to modify the agreement and return it to the Broker, execute the following:

```flow start ModificationFlow$Initiator proposalId: xx, newAmount: 8```

Where xx is the linear ID taken from the vault, also notice that we have amended the amount to 8.

Again go back to Broker Shell and execute a vault query:

```run vaultQuery contractStateType: negotiation.contracts.ProposalState```

Now we can see that the state has changed.  We can go back and forth as many times as we wish until we finally reach agreement.  Once the broker and Insurer C agree on an amount we execute the acceptance flow:

```flow start AcceptanceFlow$Initiator proposalId: xx```

This converts the proposal state into a trade state.  Next the insurer must convert the trade state into a policy state (TBD).


    
# Extending the template

You should extend this template as follows:

* Add your own state and contract definitions under `contracts/src/main/kotlin/`
* Add your own flow definitions under `workflows/src/main/kotlin/`
* Extend or replace the client and webserver under `clients/src/main/kotlin/`

TESTING:

## Running tests inside IntelliJ

We recommend editing your IntelliJ preferences so that you use the Gradle runner - this means that the quasar utils
plugin will make sure that some flags (like ``-javaagent`` - see below) are
set for you.

To switch to using the Gradle runner:

* Navigate to ``File, Settings, Build, Execution, Deployment -> Build Tools -> Gradle -> Runner`` (or search for `runner`)
  * Windows: this is in "Settings"
  * MacOS: this is in "Preferences"
* Set "Delegate IDE build/run actions to gradle" to true
* Set "Run test using:" to "Gradle Test Runner"

If you would prefer to use the built in IntelliJ JUnit test runner, you can run ``gradlew installQuasar`` which will
copy your quasar JAR file to the lib directory. You will then need to specify ``-javaagent:lib/quasar.jar``
and set the run directory to the project root directory for each test.


For a guided example of how to extend this template, see the Hello, World! tutorial 
[here](https://docs.corda.net/hello-world-introduction.html).
