package fr.itinerennes.bundler.tasks.framework;

/*
 * [license]
 * Itinerennes data resources generator
 * ----
 * Copyright (C) 2013 - 2014 Dudie
 * ----
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * [/license]
 */

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
