package jadex.micro.examples.remoteservice;

import jadex.base.Starter;
import jadex.bridge.ComponentIdentifier;
import jadex.bridge.CreationInfo;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentManagementService;
import jadex.bridge.IExternalAccess;
import jadex.commons.Future;
import jadex.commons.IFuture;
import jadex.commons.SUtil;
import jadex.commons.concurrent.DefaultResultListener;
import jadex.commons.service.SServiceProvider;

/**
 *  Start two Jadex platforms and one agent on each platform.
 *  On the 'remote' platform the 'math' agent is created, which offers an 
 *  IMathService interface via its service provider.
 *  On the 'local' platform the 'user' agent is created, which fetches the
 *  add service via the remote management service (by knowing the remote platform name/address). 
 */
public class StartScenario
{
	/**
	 *  Main for starting hello world agent.
	 */
	public static void main(String[] args)
	{
		startScenario(null);
	}
	
	/**
	 *  Start the scenario.
	 */
	public static IFuture startScenario(final String[] libpaths)
	{
		final Future ret = new Future();
		
		String[] defargs = new String[]{"-platformname", "local", "-tcpport", "10000", "-niotcpport", "10001"};
		
		Starter.createPlatform(createArguments(defargs, libpaths))
			.addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				final IExternalAccess lplat = (IExternalAccess)result;
				String[] defargs = new String[]{"-platformname", "remote", "-tcpport", "11000", "-niotcpport", "11001"};
				
				Starter.createPlatform(createArguments(defargs, libpaths))
					.addResultListener(new DefaultResultListener()
				{
					public void resultAvailable(Object source, Object result)
					{
						final IExternalAccess rplat = (IExternalAccess)result;
						
						SServiceProvider.getServiceUpwards(lplat.getServiceProvider(), IComponentManagementService.class)
							.addResultListener(new DefaultResultListener()
						{
							public void resultAvailable(Object source, Object result)
							{
								final IComponentManagementService lcms = (IComponentManagementService)result;
					
								SServiceProvider.getServiceUpwards(rplat.getServiceProvider(), IComponentManagementService.class)
									.addResultListener(new DefaultResultListener()
								{
									public void resultAvailable(Object source, Object result)
									{
										IComponentManagementService rcms = (IComponentManagementService)result;
										
										rcms.createComponent("math", "jadex.micro.examples.remoteservice.MathAgent.class", null, null)
											.addResultListener(new DefaultResultListener()
										{
											public void resultAvailable(Object source, Object result)
											{
	//											System.out.println("started remote: "+result);
												
												IComponentIdentifier rrms = new ComponentIdentifier("rms@remote", 
													new String[]{"tcp-mtp://127.0.0.1:11000", "nio-mtp://127.0.0.1:11001"});
												
												lcms.createComponent("proxy", "jadex.base.service.remote.ProxyAgent.class", 
													new CreationInfo(SUtil.createHashMap(new String[]{"componentid"}, new Object[]{rrms})), null)
													.addResultListener(new DefaultResultListener()
												{
													public void resultAvailable(Object source, Object result)
													{
														lcms.createComponent("user", "jadex.micro.examples.remoteservice.UserAgent.class", null, new DefaultResultListener()
														{
															public void resultAvailable(Object source, Object result)
															{
																//System.out.println("killed local user: "+result);
															
																ret.setResult(new IExternalAccess[]{lplat, rplat});
															}
														});
													}
												});
											}
										});
									}
								});
							}
						});			
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  Merge arguments.
	 */
	public static String[] createArguments(String[] defargs, String[] libpaths)
	{
		String[] args = defargs;
		if(libpaths!=null)
		{
			args = new String[defargs.length+2];
			System.arraycopy(defargs, 0, args, 0, defargs.length);
			
			StringBuffer lib = new StringBuffer();
			lib.append("new String[]{");
			for(int i=0; i<libpaths.length; i++)
			{
				lib.append("\"");
				lib.append(libpaths[i]);
				lib.append("\"");
				if(i+1<libpaths.length)
				lib.append(", ");
			}
			lib.append("}");
			
			args[defargs.length] = "-libpath";
			args[defargs.length+1] = lib.toString();
		}
		
		return args;
	}
}
