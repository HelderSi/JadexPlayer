package jadex.examples.hunterprey.creature.preys.basicbehaviour;

import java.util.*;
import jadex.examples.hunterprey.*;
import jadex.runtime.*;

/**
 *  Try to run away from a hunter.
 */
public class EscapePreyPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Creature me = ((Creature)getBeliefbase().getBelief("my_self").getFact());
		Vision vision = (Vision)getBeliefbase().getBelief("vision").getFact();
		WorldObject[] obs = vision.getObjects();
		HashMap points = new HashMap();
		points.put(RequestMove.DIRECTION_UP, new Integer(0));
		points.put(RequestMove.DIRECTION_DOWN, new Integer(0));
		points.put(RequestMove.DIRECTION_RIGHT, new Integer(0));
		points.put(RequestMove.DIRECTION_LEFT, new Integer(0));

		for(int i=0; i<obs.length; i++)
		{
			if(obs[i] instanceof Hunter)
			{
				String[] dirs = me.getDirections(obs[i]);
				for(int j=0; j<dirs.length; j++)
				{
					int actual = ((Integer)points.get(dirs[j])).intValue();
					if(actual!=Integer.MAX_VALUE)
						points.put(dirs[j], new Integer(((Integer)points.get(dirs[j])).intValue()+1));
				}
			}
			if(obs[i] instanceof Obstacle)
			{
				if(me.getDistance(obs[i])==1)
				{
					String[] dirs = me.getDirections(obs[i]);
					points.put(dirs[0], new Integer(Integer.MAX_VALUE));
				}
			}
		}

		Object[] sortpoints = points.entrySet().toArray(new Map.Entry[points.size()]);

		Arrays.sort(sortpoints, new Comparator()
		{
			public int	compare(Object o1, Object o2)
			{
				return ((Integer)((Map.Entry)o1).getValue()).intValue()
						- ((Integer)((Map.Entry)o2).getValue()).intValue();
			}
		});

		// todo: make random selection of equal directions!
		//System.out.println("+++ "+SUtil.arrayToString(sortpoints));

		IGoal move = createGoal("move");
		move.getParameter("direction").setValue(((Map.Entry)sortpoints[0]).getKey());
		dispatchSubgoalAndWait(move);

	}

}