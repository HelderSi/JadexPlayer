package jadex.application.space.agr;

import jadex.application.model.MSpaceInstance;
import jadex.commons.SReflect;

import java.util.ArrayList;
import java.util.List;

/**
 *  An instance of an AGR space. 
 */
public class MAGRSpaceInstance extends MSpaceInstance
{
	//-------- attributes --------
	
	/** The groups. */
	protected List groups;
	
	//-------- methods --------
	
	/**
	 *  Get the groups of this space.
	 *  @return An array of groups (if any).
	 */
	public MGroupInstance[] getMGroupInstances()
	{
		return groups==null? null:
			(MGroupInstance[])groups.toArray(new MGroupInstance[groups.size()]);
	}

	/**
	 *  Add a group to this space.
	 *  @param group The group to add. 
	 */
	public void addMGroupInstance(MGroupInstance group)
	{
		if(groups==null)
			groups	= new ArrayList();
		groups.add(group);
	}
	
	/**
	 *  Get a group per name.
	 *  @param name The name.
	 *  @return The group.
	 */
	public MGroupInstance getMGroupInstance(String name)
	{
		MGroupInstance	ret	= null;
		for(int i=0; ret==null && i<groups.size(); i++)
		{
			MGroupInstance	gi	= (MGroupInstance)groups.get(i);
			if(gi.getName().equals(name))
				ret	= gi;
		}
		return ret;
	}
	
	/**
	 *  Get a string representation of this AGR space instance.
	 *  @return A string representation of this AGR space instance.
	 */
	public String	toString()
	{
		StringBuffer	sbuf	= new StringBuffer();
		sbuf.append(SReflect.getInnerClassName(getClass()));
		sbuf.append("(name=");
		sbuf.append(getName());
		if(groups!=null)
		{
			sbuf.append(", groups=");
			sbuf.append(groups);
		}
		sbuf.append(")");
		return sbuf.toString();
	}

	/**
	 *  Get the implementation class of the space.
	 */
	public Class getClazz()
	{
		return AGRSpace.class;
	}
}
