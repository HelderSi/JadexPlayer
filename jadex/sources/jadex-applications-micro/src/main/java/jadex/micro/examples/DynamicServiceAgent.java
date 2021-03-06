package jadex.micro.examples;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.service.BasicService;
import jadex.commons.service.IServiceProvider;
import jadex.micro.MicroAgent;

/**
 *  An agent that dynamically adds services at runtime.
 */
public class DynamicServiceAgent extends MicroAgent
{
	/**
	 *  Perform the agents actions.
	 */
	public void executeBody()
	{
		IComponentStep addservice = new IComponentStep()
		{
			public Object execute(IInternalAccess ia)
			{
				addDirectService(new DummyService(getServiceProvider()));
				waitFor(3000, this);
				return null;
			}
		};
		
		addservice.execute(this);
	}
	
	public class DummyService	extends BasicService	implements IDummyService
	{
		public DummyService(IServiceProvider provider)
		{
			super(provider.getId(), IDummyService.class, null);
		}
		
		public String toString()
		{
			return getServiceIdentifier().getServiceName();
		}
	}
	
	public interface IDummyService
	{
		public String	toString();
	}
}
