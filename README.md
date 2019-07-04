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

Occassionally a node will fail to start.  In this situation we can run a node individually by opening a terminal window within the node’s folder (cd build/nodes) and running:

```java -jar corda.jar```


#### OUR DEMO:


For now we will execute all flows from the node shell. Once all flows are working will will integrate Spring etc.


### A Broker requests a quote from an Insurer (creating a Proposal) 

In our cordapp any node can create a proposal to any other node.  The node that is making the proposal is the _proposer_ and the node that is receiving the proposal is the _proposee_.  Please note that these role's change each time a proposal is sent back or forth.  

The commands below must be executed from the relevant node shell.  Change the amounts, parties as required, and be sure to add the correct linear ID (you can access this by doing a vault query, more below).


#### CREATING A PROPOSAL:

``ProposalFlow$Initiator``

This flow creates a proposal to Insurer C, this proposal will have the data Amount = 10, and isBuyer = true.

In the Broker shell execute the following command:

```flow start ProposalFlow$Initiator isBuyer: true, amount: 10, counterparty: InsurerC```


#### VIEWING A PROPOSAL:

Now go to InsurerC shell and execute the command below:
```run vaultQuery contractStateType: negotiation.contracts.ProposalState```


This allow's us to view the proposal in Insurer C's vault.  This flow can be executed from any shell to allow us to view the vault contents.  Note down the linear ID (its near the top of the state data).

#### MODIFYING A PROPOSAL:

Now assume that Insurer C wishes to modify the agreement and return it to the Broker, execute the following:

```flow start ModificationFlow$Initiator proposalId: xx, newAmount: 8```

Where xx is the linear ID taken from the vault, also notice that we have amended the amount to 8.

Again go back to Broker Shell and execute a vault query:

```run vaultQuery contractStateType: negotiation.contracts.ProposalState```

#### ACCEPTING THE PROPOSAL
Now we can see that the state has changed.  We can go back and forth as many times as we wish until we finally reach agreement.  Once the broker and Insurer C agree on an amount we execute the acceptance flow:

```flow start AcceptanceFlow$Initiator proposalId: xx```

This converts the proposal state into a trade state.  Next the insurer must convert the trade state into a policy state (TBD).

SENDING A MESSAGE FROM ONE NODE TO ANOTHER:

From the node shell for any node execute the following:

```flow start YoFlow target: [NODE_NAME]```

Where [NODE_NAME] is the name of the recipient node.

We can view all sent/recieived messages by querying our vault:

```run vaultQuery contractStateType: net.corda.yo.YoState```

#### GROUP CHAT:

To start a group chat, go to the Broker Node shell:

##### 1. START A GROUP CHAT

flow start com.template.flows.StartGroupChat notary: "O=Notary, L=London, C=GB"

##### 2. ADD INSURER A:

flow start com.template.flows.AddGroupMemberFlow gameID: "d6786ae3-ee75-4e14-bf60-734d98fa6609", member: "O=InsurerA, L=New York, C=US"

##### 3. ADD INSURER B:

flow start com.template.flows.AddGroupMemberFlow gameID: "d6786ae3-ee75-4e14-bf60-734d98fa6609", member: "O=InsurerB, L=London, C=GB"

##### 4. ADD A MESSAGE TO THE GROUP CHAT:

flow start group.chat.flows.AddGroupMessageFlow gameID: "d6786ae3-ee75-4e14-bf60-734d98fa6609", message: "APEX INSURTECH ROCKS!"

##### 5. CHECK GROUP CHAT MESSAGES:
 
 Execute a vault query to confirm that Insurer A has been added to the group chat:

run vaultQuery contractStateType: com.template.states.GroupChatState

TO DO:

- Negotiation - DONE
- One to One Chat - DONE
- Group Chat -  DONE
- File Upload 
- IoT - See Jira for Fuzz comments
- Claim State - This state will be used to handle claims
- Payment - Corda Settler
- ReInsurancePolicyState - A very simple variation of the Insurance Policy but inputs will the InsurancePolicyState
- Spring Boot to create API's - FRONT END
- Basic Front End using above API's - FRONT END
- 3 dashboard types, Broker, Insurer, Reinsurer - FRONT END

Deadline is end of July.  It may seem as a lot but I know exactly which cordapps we need to use to create our final cordapp.   
    
# ADDING ADDITIONAL FUNCTIONS FROM OTHER CORDAPPS:

We can extend this base template to incorporate features from other cordapps.  This requires some reconfiguring of the code.  This is our mission!  You should extend this template as follows:

* Add state and contract definitions under `contracts/src/main/kotlin/`
* Add flow definitions under `workflows/src/main/kotlin/`
* Extend or replace the client and webserver under `clients/src/main/kotlin/`
