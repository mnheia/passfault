package org.owasp.passfault.impl;

import org.owasp.passfault.api.PatternCollection;
import org.owasp.passfault.api.PatternCollectionFactory;

/**
 * Created by cammorris on 1/16/17.
 */
public class TestingPatternCollectionFactory implements PatternCollectionFactory {
  @Override
  public PatternCollection build(CharSequence password) {
    return new PatternCollectionImpl(password, false);
  }

  static public PatternCollectionFactory getInstance() {
    return new TestingPatternCollectionFactory();
  }
}
