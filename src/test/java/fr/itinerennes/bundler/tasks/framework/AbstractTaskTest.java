package fr.itinerennes.bundler.tasks.framework;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.itinerennes.bundler.tasks.framework.AbstractTask;
import fr.itinerennes.bundler.tasks.framework.PostExec;
import fr.itinerennes.bundler.tasks.framework.PreExec;

public class AbstractTaskTest {
	
	abstract class A extends AbstractTask {

		boolean aPre = false;
		boolean aPost = false;
		
		@PreExec
		public void preA() {
			aPre = true;
		}
		
		@PostExec
		public void postA() {
			aPost = true;
		}
	}
	
	class B extends A {

		boolean bPre = false;
		boolean bPost = false;
		
		@PreExec
		public void preB() {
			bPre = true;
		}
		
		@PostExec
		public void postB() {
			bPost = true;
		}

		@Override
		protected void execute() {
		}
	}

	@Test
	public void runExecutesPreExecMethods() {
		final B task = new B();
		assertFalse(task.aPre);
		assertFalse(task.bPre);
		task.run();
		assertTrue(task.aPre);
		assertTrue(task.bPre);
	}

	@Test
	public void runExecutesPostExecMethods() {
		final B task = new B();
		assertFalse(task.aPost);
		assertFalse(task.bPost);
		task.run();
		assertTrue(task.aPost);
		assertTrue(task.bPost);
	}
}
