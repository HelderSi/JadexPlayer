package jadex.micro.examples.remoteservice;

import jadex.commons.IFuture;
import jadex.commons.service.IService;

/**
 *  Simple interface for a component service.
 */
public interface IMathService extends IService
{
	/**
	 *  Tests a non-blocking call.
	 * 
	 *  Add two numbers.
	 *  @param a First number.
	 *  @param b Second number.
	 *  @return Future that will deliver the sum of a and b.
	 */
	public IFuture addNB(int a, int b);
	
	/**
	 *  Tests a blocking call (should be avoided!).
	 * 
	 *  Add two numbers.
	 *  @param a First number.
	 *  @param b Second number.
	 *  @return The sum of a and b.
	 */
	public int addB(int a, int b);
	
	/**
	 *  Tests a constant call, i.e. call without 
	 *  parameters are assumed to be constant so that
	 *  their value can be cached on local side.
	 *   
	 *  Get the PI value.
	 */
	public double getPi();
	
	/**
	 *  Tests a void message call (default is asynchronous).
	 * 
	 *  Print out some message.
	 *  @param message The message.
	 */
	public void printMessage(String message);
	
	/**
	 *  Tests a method that throws an exception.
	 *  
	 *  Does a divide by zero operation and causes an exception.
	 */
	public IFuture divZero();
}
