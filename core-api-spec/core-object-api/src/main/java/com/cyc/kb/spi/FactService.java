/*
 * Copyright 2015 Cycorp, Inc..
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

package com.cyc.kb.spi;

/*
 * #%L
 * File: FactService.java
 * Project: Core API Object Specification
 * %%
 * Copyright (C) 2013 - 2015 Cycorp, Inc
 * %%
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
 * #L%
 */

import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.Sentence;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.InvalidFormulaInContextException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbTypeException;

/**
 *
 * @author nwinant
 */
public interface FactService<T extends Fact> extends AssertionService<T> {

  /**
   * Provides implementation for {@link com.cyc.kb.FactFactory#get(java.lang.String) }.
   *
   * @param hlid the <code>hlid</code> corresponding to the Fact in the KB
   *
   * @return a Fact based on <code>hlid</code>
   *
   * @throws KbTypeException if fact based on <code>hlid</code> is not an instance of assertion
   *
   * @throws CreateException
   */
  @Override
  T get(String hlid) throws KbTypeException, CreateException;

  /**
   * Provides implementation for
   * {@link com.cyc.kb.FactFactory#get(java.lang.String, java.lang.String) }.
   *
   * @param formulaStr string representation of the formula to be found
   * @param ctxStr string representation of the context of the formula
   *
   * @return a Fact based on <code>formulaStr</code> and <code>ctxStr</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   * @throws KbObjectNotFoundException if no fact with the given formula string is found in the
   * context
   */
  @Override
  T get(String formulaStr, String ctxStr)
          throws KbTypeException, CreateException, KbObjectNotFoundException;

  /**
   * Provides implementation for
   * {@link com.cyc.kb.FactFactory#get(com.cyc.kb.Sentence, com.cyc.kb.Context) }.
   *
   * @param formula formula to be found
   * @param ctx context of the formula
   *
   * @return a Fact based on formula and context
   *
   * @throws CreateException
   * @throws KbTypeException
   * @throws KbObjectNotFoundException if no fact with the given formula string is found in the
   * context
   */
  @Override
  T get(Sentence formula, Context ctx)
          throws KbTypeException, CreateException, KbObjectNotFoundException;

  /**
   * Provides implementation for {@link com.cyc.kb.FactFactory#findOrCreate(java.lang.String) }.
   *
   * @param formulaStr the string representation of the formula to be found or created
   *
   * @return a Fact based on <code>formulaStr</code> and the default assertion context
   *
   * @throws CreateException
   * @throws KbTypeException
   * @throws InvalidFormulaInContextException if no fact with the given formula string is found or
   * created in the context
   */
  @Override
  T findOrCreate(String formulaStr)
          throws CreateException, KbTypeException, InvalidFormulaInContextException;

  /**
   * Provides implementation for 
   * {@link com.cyc.kb.FactFactory#findOrCreate(java.lang.String, java.lang.String) }.
   *
   * @param formulaStr the string representation of the formula to be found or created
   * @param ctxStr the string representation of the context of the formula
   *
   * @return a Fact based on <code>formulaStr</code> and <code>ctxStr</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   * @throws InvalidFormulaInContextException if no fact with the given formula string is found or
   * created in the context
   */
  @Override
  T findOrCreate(String formulaStr, String ctxStr)
          throws CreateException, KbTypeException, InvalidFormulaInContextException;

  /**
   * Provides implementation for 
   * {@link com.cyc.kb.FactFactory#findOrCreate(java.lang.String, java.lang.String,
   * com.cyc.kb.Assertion.Strength, com.cyc.kb.Assertion.Direction) }.
   *
   * @param formulaStr the string representation of the formula to be found or created
   * @param ctxStr the string representation of the context of the formula
   * @param strength the strength of the assertion
   * @param direction the direction of the assertion
   *
   * @return a Fact based on formula and context
   *
   * @throws CreateException
   * @throws KbTypeException
   * @throws InvalidFormulaInContextException if no fact with the given formula string is found or
   * created in the context
   */
  @Override
  T findOrCreate(String formulaStr, String ctxStr, Strength strength, Direction direction)
          throws CreateException, KbTypeException, InvalidFormulaInContextException;

  /**
   * Provides implementation for {@link com.cyc.kb.FactFactory#findOrCreate(com.cyc.kb.Sentence) }.
   *
   * @param formula the formula to be found or created
   *
   * @return a Fact based on formula and default assertion context
   *
   * @throws CreateException
   * @throws KbTypeException
   *
   * @throws InvalidFormulaInContextException if no fact with the given formula string is found or
   * created in the context
   */
  @Override
  T findOrCreate(Sentence formula) throws KbTypeException, CreateException;

  /**
   * Provides implementation for
   * {@link com.cyc.kb.FactFactory#findOrCreate(com.cyc.kb.Sentence, com.cyc.kb.Context) }.
   *
   * @param formula the formula to be found or created
   * @param ctx the context of the formula
   *
   * @return a Fact based on formula and context
   *
   * @throws CreateException
   * @throws KbTypeException
   *
   * @throws InvalidFormulaInContextException if no fact with the given formula string is found or
   * created in the context
   */
  @Override
  T findOrCreate(Sentence formula, Context ctx)
          throws KbTypeException, CreateException, InvalidFormulaInContextException;

  /**
   * Provides implementation for 
   * {@link com.cyc.kb.FactFactory#findOrCreate(com.cyc.kb.Sentence, com.cyc.kb.Context, 
   * com.cyc.kb.Assertion.Strength, com.cyc.kb.Assertion.Direction) }.
   *
   * @param formula the formula to be found or created
   * @param ctx the context of the formula
   * @param strength the strength of the assertion
   * @param direction the direction of the assertion
   *
   * @return a Fact based on formula and context
   *
   * @throws CreateException
   * @throws KbTypeException
   *
   * @throws InvalidFormulaInContextException if no Fact with the given formula string is found or
   * created in the context
   *
   */
  @Override
  T findOrCreate(Sentence formula, Context ctx, Strength strength, Direction direction)
          throws CreateException, KbTypeException, InvalidFormulaInContextException;

}
