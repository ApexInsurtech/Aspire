# **APEX ASPIRE**

If you are reading this it is because you are part of a privileged few, whom I trust very much.  This repo has been cloned from the original Kotlin version of the CorDapp template. The Java equivalent is
[here](https://github.com/corda/cordapp-template-java/).**

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

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
### OUR DEMO:

#### A Broker requests a quote from an Insurer (creating a Proposal) 

In our cordapp any node can create a proposal to any other node.  The node that is making the proposal is the _proposer_ and the node that is receiving the proposal is the _proposee_.  Please note that these role's change each time a proposal is sent back or forth.  The commands below must be executed from the relevant node shell.  Change the amounts, parties as required, and be sure to add the correct linear ID (you can access this by doing a vault query, more below).

## CREATING A PROPOSAL:

``ProposalFlow$Initiator``

This flow creates a proposal to Insurer C, this proposal will have the data Amount = 10, and isBuyer = true.

In the Broker shell execute the following command:

```flow start ProposalFlow$Initiator isBuyer: true, amount: 10, counterparty: InsurerC```


## VIEWING A PROPOSAL:

Now go to InsurerC shell and execute the command below:
```run vaultQuery contractStateType: negotiation.contracts.ProposalState```


This allow's us to view the proposal in Insurer C's vault.  This flow can be executed from any shell to allow us to view the vault contents.  Note down the linear ID (its near the top of the state data).

## MODIFYING A PROPOSAL:

Now assume that Insurer C wishes to modify the agreement and return it to the Broker, execute the following:

```flow start ModificationFlow$Initiator proposalId: xx, newAmount: 8```

Where xx is the linear ID taken from the vault, also notice that we have amended the amount to 8.

Again go back to Broker Shell and execute a vault query:

```run vaultQuery contractStateType: negotiation.contracts.ProposalState```

Now we can see that the state has changed.  We can go back and forth as many times as we wish until we finally reach agreement.  Once the broker and Insurer C agree on an amount we execute the acceptance flow:

```flow start AcceptanceFlow$Initiator proposalId: xx```

This converts the proposal state into a trade state.  Next the insurer must convert the trade state into a policy state (TBD).

## NODE TO NODE CHAT:

We have integrated the Yo cordapp to allow nodes to send a YO to each other.  We must now modify and rename the yo.kt file and the code so that it can accept a string message from the node sending the Yo.  Also at the moment if I send a Yo to another node, I cannot see the Yo in my vault...I suspect this is due to the contract rules.

We use the node shell to interact with the nodes. The following command sends a Yo! to another node:

flow start YoFlow target: [NODE_NAME]

Note you can't sent a Yo! to yourself because that's not cool!

To see all the Yo's! other nodes have sent you in your vault (you do not store the Yo's! you send yourself), run:

run vaultQuery contractStateType: net.corda.yo.YoState

## GROUP CHAT:

To start a group chat, go to the Broker Node shell:

'flow start com.template.flows.StartGroupChat notary: "O=Notary, L=London, C=GB"'

This will start a group chat.  For our situation the Node that starts this flow will be considered the dealer...In our case we shall rename this to Moderator.  Only the moderator can invite new members.  The rules around joining or leaving the group chat will be defined in GroupChatContract.kt.

Now we can add another node (e.g Insurer A) to our group chat:

`flow start com.template.flows.AddPlayerFlow gameID: <GameID>, player: "O=InsurerA, L=New York, C=US"`

Execute a vault query to confirm that Insurer A has been added to the group chat:

`run vaultQuery contractStateType: com.template.states.GroupChatState`

If we execute the above query from Broker or Insurer A nodes we will be able to see the group chat state.  No other node will be able to see it.  Great work Hafsa!!!
