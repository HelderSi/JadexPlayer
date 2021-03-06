package jadex.commons.service;

import jadex.commons.Future;
import jadex.commons.IFuture;

import java.util.Collection;
import java.util.Map;

/**
 *  Search for services at the local provider and all parents. 
 */
public class LocalSearchManager implements ISearchManager
{
	//-------- attributes --------
	
	/** Force search flag. */
	protected boolean forcedsearch;
	
	//-------- constructors --------
	
	/**
	 *  Create a new local search manager.
	 */
	public LocalSearchManager()
	{
		this(false);
	}
	
	/**
	 *  Create a new local search manager.
	 */
	public LocalSearchManager(boolean forcedsearch)
	{
		this.forcedsearch = forcedsearch;
	}
	
	//-------- ISearchManager interface --------
	
	/**
	 *  Search for services, starting at the given service provider.
	 *  @param provider	The service provider to start the search at.
	 *  @param decider	The visit decider to select nodes and terminate the search.
	 *  @param selector	The result selector to select matching services and produce the final result. 
	 *  @param services	The local services of the provider (class->list of services).
	 */
	public IFuture	searchServices(IServiceProvider provider, IVisitDecider decider, IResultSelector selector, Map services, Collection results)
	{
		// local search is always allowed?!
		// problem: first gsm searches a node, then lsm searches the same node = double visit
		if(!selector.isFinished(results))// && decider.searchNode(null, provider, results))
		{
			try
			{
				selector.selectServices(services, results);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
//		if(selector instanceof TypeResultSelector && results.toString().indexOf("Add")!=-1)
//			System.out.println("lsm: "+provider+" "+results);
		return new Future(selector.getResult(results));
	}
	
	/**
	 *  Get the cache key.
	 *  Needs to identify this element with respect to its important features so that
	 *  two equal elements should return the same key.
	 */
	public Object getCacheKey()
	{
		// Do not cache local results.
		return null;
	}

	/**
	 *  Get the forcedsearch.
	 *  @return The forcedsearch.
	 */
	public boolean isForcedSearch()
	{
		return forcedsearch;
	}
	
	/**
	 *  Set the forcedsearch.
	 *  @param forcedsearch The forcedsearch to set.
	 */
	public void setForcedSearch(boolean forcedsearch)
	{
		this.forcedsearch = forcedsearch;
	}

}
