package com.cyc.kb;

/*
 * #%L
 * File: KbFunction.java
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
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;

import java.util.Collection;

/**
 * The interface for Cyc functions. <code>KbFunctions</code> are applied to one
 * or more arguments to form non-atomic {@link KbTerm}s.
 *
 * @author vijay
 */
public interface KbFunction extends Relation {

  /**
   * Create an object of type <code>O</code> with <code>args</code> as the
   * arguments to this <code>KbFunction</code>. If no corresponding term exists
   * in the Cyc KB, one will be created.
   * <p>
   * Note that it can take <b>significantly longer</b> to create a term using a
   * KbFunction than it does to create a constant because the Cyc KB triggers
   * forward rules that will add facts about the functional term. Hence, the
   * time required to create a functional term, such as
   * <code>(#$FruitFn #$Apple)</code>, is longer than the time required to
   * create a non-functional term, such as <code>#$Dog</code>.
   * <p>
   *
   * The <code>O</code> parameter determines what class the returned Object will
   * be. For example, if creating <code>(#$FruitFn #$AppleTree)</code>, the
   * <code>O</code> should be <code>KbCollection</code> or
   * <code>FirstOrderCollection</code>. If a return type is specified that is in
   * conflict with the <code>#$resultIsa</code> for the underlying
   * <code>#$Function</code>, a KbException may be thrown.
   *
   * @param <O> The class of objects constructed by this function
   * @param retType the class of type <code>O</code>, whose object instance is
   * generated by this function. Must be a subclass of KbObject.
   * @param args An array of arguments for the new functional term. Arguments
   * may include any subclass of KbObject and Java Language native objects,
   * String, Number and Date.
   *
   * @return a new KbObject of type <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   *
   * @throws UnsupportedOperationException as a catch all, but normally should
   * not happen with KB API objects.
   */
  // @todo remove passing in of retType
  public <O> O findOrCreateFunctionalTerm(Class<O> retType, Object... args)
          throws KbTypeException, CreateException;

  /**
   * Returns the <code>#$resultIsa</code>s of this <code>KbFunction</code> from
   * the default context specified by {@link com.cyc.kb.DefaultContext#forQuery()}.
   * Any functional term using this <code>KbFunction</code> will be an instance
   * of each of these Collections. The set of collections defines the Range of
   * this function.
   * <p>
   *
   * @return all <code>#$resultIsa</code> collections for this
   * <code>KbFunction</code>.
   */
  public Collection<KbCollection> getResultIsa();

  /**
   * Returns the <code>#$resultIsa</code>as of this <code>KbFunction</code> from
   * the context <code>ctx</code>. Any functional term using this
   * <code>KbFunction</code> will be an instance of each of these Collections.
   * The set of collections defines the Range of this function.
   * <p>
   *
   * @param ctxStr the string representation of the context of the query
   *
   * @return the relevant <code>#$resultIsa</code> collections for this
   * <code>KbFunction</code>.
   */
  public Collection<KbCollection> getResultIsa(String ctxStr);

  /**
   * Returns the <code>#$resultIsa</code>s of this <code>KbFunction</code> from
   * the context. Any functional term using this <code>KbFunction</code> will be
   * an instance of each of these Collections. The set of collections defines
   * the Range of this function.
   * <p>
   *
   * @param ctx the context of the query
   *
   * @return the relevant <code>#$resultIsa</code> collections for * this
   * <code>KbFunction</code>.
   */
  public Collection<KbCollection> getResultIsa(Context ctx);

  /**
   * creates a new <code>#$resultIsa</code> {@link Fact} in the supplied
   * context.
   * <p>
   * The elements of the Range of this function, are instances of the collection
   * specified by <code>col</code>.
   *
   * @param colStr the string representing the resultIsa collection
   * @param ctxStr the string representing the context where the fact is
   * asserted
   *
   * @return this
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  public KbFunction addResultIsa(String colStr, String ctxStr)
          throws KbTypeException, CreateException;

  /**
   * creates a new <code>#$resultIsa</code> {@link Fact} in the supplied
   * context.
   * <p>
   * Future versions of this API may assume that the context is always
   * <code>UniversalVocabularyMt</code>.
   *
   * @param ctx the context where the fact is asserted
   * @param col the KbCollection the generated object is a type of
   *
   * @return this
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  public KbFunction addResultIsa(KbCollection col, Context ctx)
          throws KbTypeException, CreateException;

  /**
   * Returns the <code>#$resultGenl</code>s of this <code>KbFunction</code> from
   * the default context specified by {@link com.cyc.kb.DefaultContext#forQuery()}.
   * Any functional term using this <code>KbFunction</code> will be a
   * specialization of each of these <code>KbCollection</code>s. The set of
   * collections defines the Range of this function.
   * <p>
   *
   * @return all <code>#$resultGenl</code> collections for this function
   * @throws KbException
   */
  // @todo Check if we can throw a better exception
  public Collection<KbCollection> getResultGenl() throws KbException;

  /**
   * Returns the <code>#$resultGenl</code>s of this <code>KbFunction</code> from
   * the context. Any functional term using this <code>KbFunction</code> will be
   * a specialization of each of these <code>KbCollection</code>s. The set of
   * collections defines the Range of this function.
   * <p>
   *
   * @param ctxStr the string representation of the context of query
   *
   * @return all <code>#$resultGenl</code> collections for this function that
   * are visible from the context
   */
  public Collection<KbCollection> getResultGenl(String ctxStr);

  /**
   * Returns the <code>#$resultGenl</code>s of this <code>KbFunction</code> from
   * the context. Any functional term using this <code>KbFunction</code> will be
   * a specialization of each of these <code>KbCollection</code>s. The set of
   * collections defines the Range of this function.
   * <p>
   *
   * @param ctx the context of query
   *
   * @return all <code>#$resultGenl</code> collections for this function that
   * are visible from the context
   */
  public Collection<KbCollection> getResultGenl(Context ctx);

  /**
   * creates a new <code>#$resultGenl</code> {@link Fact} in the specified
   * context.
   * <p>
   * The elements of the Range of this function, are specializations of the
   * collection specified by <code>collectionStr</code>
   *
   * @param colStr the string representing the <code>#$resultGenl</code>
   * collection.
   * @param ctxStr the string representing the context where the fact is
   * asserted
   *
   * @return this
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  public KbFunction addResultGenl(String colStr, String ctxStr)
          throws KbTypeException, CreateException;

  /**
   * creates a new <code>#$resultGenl</code> {@link Fact} in the specified
   * context.
   * <p>
   * The elements of the Range of this function, are specializations of the
   * collection specified by <code>collection</code>
   *
   * @param col the <code>#$resultGenl</code> collection.
   * @param ctx the context where the fact is asserted
   *
   * @return this
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  public KbFunction addResultGenl(KbCollection col, Context ctx)
          throws KbTypeException, CreateException;

}
