package jadex.examples.cleanerworld.multi.environment;

import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Pick up some piece of waste.
 */
public class PickUpWastePlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PickUpWastePlan()
	{
		getLogger().info("Created: "+this);
	}

	//------ methods -------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		RequestPickUpWaste op = (RequestPickUpWaste)getParameter("action").getValue();
		Waste waste = op.getWaste();

		Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
		boolean success = env.pickUpWaste(waste);

		if(!success)
			fail();

		Done done = new Done();
		done.setAction(op);
		getParameter("result").setValue(done);
	}

}