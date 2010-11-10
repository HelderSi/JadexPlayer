package jadex.testcases.misc;

import jadex.adapter.fipa.SFipa;
import jadex.planlib.TestReport;
import jadex.runtime.AgentEvent;
import jadex.runtime.GoalFailureException;
import jadex.runtime.IAgentListener;
import jadex.runtime.IBeliefListener;
import jadex.runtime.IBeliefSetListener;
import jadex.runtime.ICondition;
import jadex.runtime.IConditionListener;
import jadex.runtime.IGoal;
import jadex.runtime.IGoalListener;
import jadex.runtime.IInternalEvent;
import jadex.runtime.IInternalEventListener;
import jadex.runtime.IMessageEvent;
import jadex.runtime.IMessageEventListener;
import jadex.runtime.IPlanListener;
import jadex.runtime.Plan;
import jadex.runtime.impl.RCapability;
import jadex.runtime.planwrapper.CapabilityWrapper;

/**
 *  Test agent callbacks.
 */
public class CallbackPlan extends Plan
{
	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		// Test if all added listeners will also be removed.
		TestReport tr15 = new TestReport("#15", "Test if all listeners will also be removed");
		int start = ((RCapability)((CapabilityWrapper)getCapability()).unwrap())
			.getAgent().getSystemEventListeners().length;
		
		// Belief (set) tests
		
		final TestReport tr1 = new TestReport("#1", "Test if belief changes can be observed in a listener.");
		getBeliefbase().getBelief("bel").addBeliefListener(new IBeliefListener()
		{
			public void beliefChanged(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("belief changed: "+ae);
				getExternalAccess().getBeliefbase().getBelief("bel").removeBeliefListener(this);
				tr1.setSucceeded(true);
			}
		}, false);
		getBeliefbase().getBelief("bel").setFact(new Integer(1));
		waitFor(200);
		if(!tr1.isSucceeded())
			tr1.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr1);
		
		final TestReport tr2 = new TestReport("#2", "Test if belief set added can be observed in a listener.");
		getBeliefbase().getBeliefSet("belset").addBeliefSetListener(new IBeliefSetListener()
		{
			public void factAdded(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("fact added: "+ae);
				getExternalAccess().getBeliefbase().getBeliefSet("belset").removeBeliefSetListener(this);
				tr2.setSucceeded(true);
			}
			
			public void factRemoved(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("fact removed: "+ae);
			}
			
			public void beliefSetChanged(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("belief set changed: "+ae);			
			}
		}, false);
		getBeliefbase().getBeliefSet("belset").addFact(new Integer(1));
		waitFor(200);
		if(!tr2.isSucceeded())
			tr2.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr2);
		
		// todo: rename to #3
		final TestReport tr2b = new TestReport("#2b", "Test if belief set removed can be observed in a listener.");
		getBeliefbase().getBeliefSet("belset").addBeliefSetListener(new IBeliefSetListener()
		{
			public void factAdded(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("fact added: "+ae);
			}
			
			public void factRemoved(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("fact removed: "+ae);
				getExternalAccess().getBeliefbase().getBeliefSet("belset").removeBeliefSetListener(this);
				tr2b.setSucceeded(true);
			}
			
			public void beliefSetChanged(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("belief set changed: "+ae);			
			}
		}, false);
		getBeliefbase().getBeliefSet("belset").removeFact(new Integer(1));
		waitFor(200);
		if(!tr2b.isSucceeded())
			tr2b.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr2b);
		
		// Goal tests
		
		final TestReport tr3 = new TestReport("#3", "Test if goal finished can be observed in a listener.");
		IGoal goal = createGoal("goal");
		IGoalListener gl = new IGoalListener()
		{
			public void goalAdded(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Goal added called");
				//tr3.setSucceeded(true);
			}

			public void goalFinished(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Goal finished called");
				tr3.setSucceeded(true);
			}
		};
		goal.addGoalListener(gl, false);
		try
		{
			dispatchSubgoalAndWait(goal);
		}
		catch(GoalFailureException e)
		{
		}
		if(!tr3.isSucceeded())
			tr3.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr3);
		goal.removeGoalListener(gl);
		
		final TestReport tr4 = new TestReport("#4", "Test if goal added can be observed in a listener.");
		final TestReport tr5 = new TestReport("#5", "Test if goal finished can be observed in a listener.");
		// todo: rename tr6
		final TestReport tr5b = new TestReport("#5b", "Test if waitFor can be called in async listener.");
		getGoalbase().addGoalListener("goal", new IGoalListener()
		{
			public void goalAdded(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Goal added called");
				tr4.setSucceeded(true);
			}

			public void goalFinished(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Goal finished called");
				getExternalAccess().getGoalbase().removeGoalListener("goal", this);
				tr5.setSucceeded(true);
				try
				{
					getExternalAccess().waitFor(100);
					tr5b.setSucceeded(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}, true); 
		// Create a goal by setting "bel" to 2
		getBeliefbase().getBelief("bel").setFact(new Integer(2));
		waitFor(200);
		if(!tr4.isSucceeded())
			tr4.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr4);
		if(!tr5.isSucceeded())
			tr5.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr5);
		if(!tr5b.isSucceeded())
			tr5b.setReason("Could not call waitFor() in listener");
		getBeliefbase().getBeliefSet("reports").addFact(tr5b);
			
		// Internal event tests
		
		final TestReport tr6 = new TestReport("#6", "Test if internal event occurred can be observed in a listener.");
		getEventbase().addInternalEventListener("internal_event", new IInternalEventListener()
		{
			public void internalEventOccurred(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Internal event occurred called");
				getExternalAccess().getEventbase().removeInternalEventListener("internal_event", this);
				tr6.setSucceeded(true);
			}
		}, false);
		IInternalEvent ievent = createInternalEvent("internal_event");
		dispatchInternalEvent(ievent);
		waitFor(200);
		if(!tr6.isSucceeded())
			tr6.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr6);
		
		// Message event tests
		
		final TestReport tr7 = new TestReport("#7", "Test if message event sent can be observed in a listener.");
		final TestReport tr8 = new TestReport("#8", "Test if message event reply can be observed in a listener.");
		final TestReport tr9 = new TestReport("#9", "Test if message event sent can be observed in a listener.");
		
		final IMessageEvent mevent = createMessageEvent("message_event");
		mevent.getParameterSet(SFipa.RECEIVERS).addValue(getAgentIdentifier());
		mevent.getParameter(SFipa.SENDER).setValue(getAgentIdentifier());
		
		IMessageEventListener mel = new IMessageEventListener()
		{
			public void messageEventReceived(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Message event received");
				tr7.setSucceeded(true);
			}

			public void messageEventSent(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Message event sent");
				tr8.setSucceeded(true);
			}
		};
		getEventbase().addMessageEventListener("message_event", new IMessageEventListener()
		{
			public void messageEventReceived(AgentEvent ae)
			{
			}

			public void messageEventSent(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Message event sent");
				getExternalAccess().getEventbase().removeMessageEventListener("message_event", this);
				tr9.setSucceeded(true);
			}
		}, false);
		
		mevent.addMessageEventListener(mel, false);
		sendMessage(mevent);
		
		waitFor(200);
		if(!tr7.isSucceeded())
			tr7.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr7);
		if(!tr9.isSucceeded())
			tr9.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr9);
		
		IMessageEvent rep = mevent.createReply("message_event");
		sendMessage(rep);
		waitFor(200);
		if(!tr8.isSucceeded())
			tr8.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr8);
		mevent.removeMessageEventListener(mel);
		
		// Condition tests.
	
		final TestReport tr10 = new TestReport("#10", "Test if condition triggered can be observed in a listener.");
		ICondition cond = createCondition("$beliefbase.bel==3");
		IConditionListener cl = new IConditionListener()
		{
			public void conditionTriggered(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Condition triggered");
				tr10.setSucceeded(true);
			}
		};
		cond.addConditionListener(cl, false);
		cond.traceOnce();
		getBeliefbase().getBelief("bel").setFact(new Integer(3));
		waitFor(200);
		if(!tr10.isSucceeded())
			tr10.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr10);
		cond.removeConditionListener(cl);
		
		final TestReport tr11 = new TestReport("#11", "Test if adf condition triggered can be observed in a listener.");
		getExpressionbase().addConditionListener("condition", new IConditionListener()
		{
			public void conditionTriggered(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Condition triggered");
				getExternalAccess().getExpressionbase().removeConditionListener("condition", this);
				tr11.setSucceeded(true);
			}
		}, false);
		cond = getCondition("condition");
		cond.traceOnce();
		getBeliefbase().getBelief("bel").setFact(new Integer(5));
		waitFor(200);
		if(!tr11.isSucceeded())
			tr11.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr11);
		
		// Plan tests.
		
		final TestReport tr12 = new TestReport("#12", "Test if plan added can be observed in a listener.");
		final TestReport tr13 = new TestReport("#13", "Test if plan removed can be observed in a listener.");
		IPlanListener pl = new IPlanListener()
		{
			public void planAdded(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Plan added");
				tr12.setSucceeded(true);
			}

			public void planFinished(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Plan removed");
				tr13.setSucceeded(true);
			}
		};
		getPlanbase().addPlanListener("plan", pl, false);
		getBeliefbase().getBelief("bel").setFact(new Integer(4));
		waitFor(200);
		if(!tr12.isSucceeded())
			tr12.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr12);
		if(!tr13.isSucceeded())
			tr13.setReason("Listener was not notified.");
		getBeliefbase().getBeliefSet("reports").addFact(tr13);
		getPlanbase().removePlanListener("plan", pl);
		
		// Count listeners.
		int end = ((RCapability)((CapabilityWrapper)getCapability()).unwrap())
			.getAgent().getSystemEventListeners().length;
		if(start==end)
			tr15.setSucceeded(true);
		else
			tr15.setReason("The listener count is different: "+start+" vs. "+end);
		getBeliefbase().getBeliefSet("reports").addFact(tr15);
		
		final TestReport tr14 = new TestReport("#14", "Test if plan removed (on this plan) can be observed in a listener.");
		addPlanListener(new IPlanListener()
		{
			public void planAdded(AgentEvent ae)
			{
			}

			public void planFinished(AgentEvent ae)
			{
				getExternalAccess().getLogger().info("Plan removed");
				tr14.setSucceeded(true);
				getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(tr14);
				CallbackPlan.this.removePlanListener(this);
			}
		}, false);
			
//		final TestReport tr16 = new TestReport("#16", "Test if agent killed can be observed in a listener.");
		// todo: make this work
//		getScope().addAgentListener(new IAgentListener()
//		{
//			public void agentTerminating(AgentEvent ae)
//			{
//				System.out.println("Agent terminating invoked");
//				getExternalAccess().removeAgentListener(this);
//				getExternalAccess().getLogger().info("Agent died invoked");
//				tr16.setSucceeded(true);
//				getExternalAccess().getBeliefbase().getBeliefSet("reports").addFact(tr16);
//			}
//		}, false);
	}
}