/*
 * Copyright 2019 Thomas Hoermann
 * https://github.com/thomas-hoer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cryptui.util;

public final class Assert {

	private Assert() {
	}

	/**
	 * Checks a boolean value to be true.
	 *
	 * @param bool    The value checked to be true.
	 * @param message If the value is false an exception with the message message is
	 *                thrown.
	 */
	public static void assertTrue(final boolean bool, final String message) {
		if (!bool) {
			throw new AssertionException(message);
		}
	}

	/**
	 * Tests two values ​​for equality.
	 *
	 * @param expected Expected value for comparison.
	 * @param actual   Value that is checked to be equal to expected.
	 * @param message  If the values are different an exception with the message
	 *                 message is thrown.
	 */
	public static void assertEqual(final int expected, final int actual, final String message) {
		if (expected != actual) {
			throw new AssertionException(message);
		}
	}
}
