package net.multiphasicapps.tac;

import org.testng.annotations.Test;

/**
 * This is an interface for anything which is something that can be tested
 * within the SquirrelJME test framework.
 *
 * @since 2020/02/23
 */
public interface TestInterface
	extends Runnable
{
	/**
	 * {@inheritDoc}
	 * @since 2020/02/26
	 */
	@Override
	@Test
	void run();
	
	/**
	 * Runs the test.
	 *
	 * @param __mainargs Arguments to the test.
	 * @return The execution result of the test.
	 * @since 2020/02/23
	 */
	TestExecution runExecution(String... __mainargs);
}
