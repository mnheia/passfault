/* ©Copyright 2011 Cameron Morris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.owasp.passfault.dictionary;


import org.junit.BeforeClass;
import org.junit.Test;
import org.owasp.passfault.impl.TestingPatternCollectionFactory;

import static org.junit.Assert.assertEquals;

public class InsertionFinderTest {

  private static DictionaryPatternsFinder finder;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    FileDictionary dictionary = FileDictionary.newInstance(TestWords.getTestFile(), "tiny-lower");
    finder = new DictionaryPatternsFinder(dictionary, new InsertionStrategy(5), TestingPatternCollectionFactory.getInstance());
  }

  @Test
  public void plain2() throws Exception {
    assertEquals(finder.search("trouble").getCount(), 0);
  }

  @Test
  public void plain() throws Exception {
    assertEquals(finder.search("wisp").getCount(), 0);
  }

  @Test
  public void findWord() throws Exception {
    assertEquals(finder.search("trou$ble").getCount(), 2);
  }

  @Test
  public void tooMany() throws Exception {
    assertEquals(finder.search("t$r$o$u$b$l$e").getCount(), 0);
  }

  @Test
  public void garbageInFront() throws Exception {
    assertEquals(finder.search("xxxxtrou-ble").getCount(), 2);
  }

  @Test
  public void garbageInBack() throws Exception {
    assertEquals(finder.search("troub!lexxxx").getCount(), 2);
  }

  @Test
  public void findNonWord() throws Exception {
    assertEquals(finder.search("qqq123").getCount(), 0);
  }

  @Test
  public void findMultiWords() throws Exception {
    assertEquals(finder.search("tr-oubletro+uble").getCount(), 4);
  }
}
