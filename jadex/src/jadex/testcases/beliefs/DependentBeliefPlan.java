package jadex.testcases.beliefs;

import jadex.runtime.Plan;
import jadex.planlib.TestReport;

/**
 *  Test if dependent beliefs work correctly.
 */
public class DependentBeliefPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Test dependent belief.");
		getBeliefbase().getBelief("my_value").setFact("magic");
		if(getBeliefbase().getBelief("react_on_value").getFact().equals("magic"))
		{
			tr.setSucceeded(true);
			getLogger().info("Test 1 succeeded.");
		}
		else
		{
			getLogger().info("Test 1 failed.");
			tr.setReason("Dependent belief not correctly set.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#2", "Test dependent belief set.");
		getBeliefbase().getBeliefSet("my_values").removeFact(new Integer(1));
		if(getBeliefbase().getBelief("react_on_values").getFact().equals("magic"))
		{
			tr.setSucceeded(true);
			getLogger().info("Test 2 succeeded.");
		}
		else
		{
			getLogger().info("Test 2 failed.");
			tr.setReason("Dependent belief not correctly set.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}