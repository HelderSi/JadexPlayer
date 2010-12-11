package jadex.bdi.runtime;

import jadex.commons.IFuture;

/**
 *  Interface for all beliefsets (concrete and referenced).
 */
public interface IEABeliefSet extends IEAElement
{
	/**
	 *  Add a fact to a belief.
	 *  @param fact The new fact.
	 */
	public IFuture addFact(Object fact);

	/**
	 *  Remove a fact to a belief.
	 *  @param fact The new fact.
	 */
	public IFuture removeFact(Object fact);

	/**
	 *  Add facts to a parameter set.
	 */
	public IFuture addFacts(Object[] values);

	/**
	 *  Remove all facts from a belief.
	 */
	public IFuture removeFacts();

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public IFuture getFact(Object oldval);

	/**
	 *  Test if a fact is contained in a belief.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public IFuture containsFact(Object fact);

	/**
	 *  Get the facts of a beliefset.
	 *  @return The facts.
	 */
	public IFuture getFacts();

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 */
//	public void updateFact(Object newfact);

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public IFuture size();
	
	/**
	 *  Indicate that a fact of this belief set was modified.
	 *  Calling this method causes an internal facts changed
	 *  event that might cause dependent actions.
	 */
	public IFuture modified(Object fact);

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(Object fact);*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(Object oldfact, Object newfact);*/

	/**
	 *  Get the value class.
	 *  @return The valuec class.
	 */
	public IFuture getClazz();

	/**
	 *  Is this belief accessable.
	 *  @return False, if the belief cannot be accessed.
	 * /
	public boolean isAccessible();*/
	
	//-------- listeners --------
	
	/**
	 *  Add a belief set listener.
	 *  @param listener The belief set listener.
	 */
	public IFuture addBeliefSetListener(IBeliefSetListener listener);
	
	/**
	 *  Remove a belief set listener.
	 *  @param listener The belief set listener.
	 */
	public IFuture removeBeliefSetListener(IBeliefSetListener listener);
}