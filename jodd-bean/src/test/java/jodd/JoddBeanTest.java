// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static jodd.Jodd.*;

public class JoddBeanTest {

	@Test
	public void testLoadedModules() {
		assertTrue(Jodd.isModuleLoaded(BEAN));
		assertFalse(Jodd.isModuleLoaded(HTTP));
		assertFalse(Jodd.isModuleLoaded(MADVOC));
		assertFalse(Jodd.isModuleLoaded(MAIL));
		assertFalse(Jodd.isModuleLoaded(PETITE));
		assertFalse(Jodd.isModuleLoaded(PROPS));
		assertFalse(Jodd.isModuleLoaded(PROXETTA));
		assertFalse(Jodd.isModuleLoaded(SERVLET));
		assertFalse(Jodd.isModuleLoaded(UPLOAD));
		assertFalse(Jodd.isModuleLoaded(VTOR));
	}
}