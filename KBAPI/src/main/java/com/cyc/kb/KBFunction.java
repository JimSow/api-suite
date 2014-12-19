package com.cyc.kb;

/*
 * #%L
 * File: KBFunction.java
 * Project: KB API
 * %%
 * Copyright (C) 2013 - 2014 Cycorp, Inc
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
import java.util.Collection;

import com.cyc.kb.config.KBAPIDefaultContext;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KBApiException;
import com.cyc.kb.exception.KBTypeException;

/**
 * The interface for Cyc functions. <code>KBFunctions</code> are applied to one
 * or more arguments to form non-atomic {@link KBTerm}s.
 *
 * @author vijay
 */
public interface KBFunction extends Relation {

  /**
   * Create an object of type <code>O</code> with <code>args</code> as the
   * arguments to this <code>KBFunction</code>. If no corresponding term exists
   * in the Cyc KB, one will be created.
   * <p>
   * Note that it can take <b>significantly longer</b> to create a term using a
   * KBFunction than it does to create a constant because the Cyc KB triggers
   * forward rules that will add facts about the functional term. Hence, the
   * time required to create a functional term, such as
   * <code>(#$FruitFn #$Apple)</code>, is longer than the time required to
   * create a non-functional term, such as <code>#$Dog</code>.
   * <p>
   *
   * The <code>O</code> parameter determines what class the returned Object will
   * be. For example, if creating <code>(#$FruitFn #$AppleTree)</code>, the
   * <code>O</code> should be <code>KBCollection</code> or
   * <code>FirstOrderCollection</code>. If a return type is specified that is in
   * conflict with the <code>#$resultIsa</code> for the underlying
   * <code>#$Function</code>, a KBApiException may be thrown.
   *
   * @param <O> The class of objects constructed by this function
   * @param retType	the class of type <code>O</code>, whose object instance is
   * generated by this function. Must be a subclass of KBObject.
   * @param args	An array of arguments for the new functional term. Arguments
   * may include any subclass of KBObject and Java Language native objects,
   * String, Number and Date.
   *
   * @return	a new KBObject of type <code>O</code>
   *
   * @throws CreateException
   * @throws KBTypeException
   *
   * @throws UnsupportedOperationException as a catch all, but normally should
   * not happen with KB API objects.
   */
  // @todo remove passing in of retType
  public <O> O findOrCreateFunctionalTerm(Class<O> retType, Object... args)
          throws KBTypeException, CreateException;

  /**
   * Returns the <code>#$resultIsa</code>s of this <code>KBFunction</code> from
   * the default context specified by {@link KBAPIDefaultContext#forQuery()}.
   * Any functional term using this <code>KBFunction</code> will be an instance
   * of each of these Collections. The set of collections defines the Range of
   * this function.
   * <p>
   *
   * @return	all <code>#$resultIsa</code> collections for this
   * <code>KBFunction</code>.
   */
  public Collection<KBCollection> getResultIsa();

  /**
   * Returns the <code>#$resultIsa</code>as of this <code>KBFunction</code> from
   * the context <code>ctx</code>. Any functional term using this
   * <code>KBFunction</code> will be an instance of each of these Collections.
   * The set of collections defines the Range of this function.
   * <p>
   *
   * @param	ctxStr the string representation of the context of the query
   *
   * @return	the relevant <code>#$resultIsa</code> collections for this
   * <code>KBFunction</code>.
   */
  public Collection<KBCollection> getResultIsa(String ctxStr);

  /**
   * Returns the <code>#$resultIsa</code>s of this <code>KBFunction</code> from
   * the context. Any functional term using this <code>KBFunction</code> will be
   * an instance of each of these Collections. The set of collections defines
   * the Range of this function.
   * <p>
   *
   * @param	ctx the context of the query
   *
   * @return	the relevant <code>#$resultIsa</code> collections for * this
   * <code>KBFunction</code>.
   */
  public Collection<KBCollection> getResultIsa(Context ctx);

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
   * @return	this
   *
   * @throws CreateException
   * @throws KBTypeException
   */
  public KBFunction addResultIsa(String colStr, String ctxStr)
          throws KBTypeException, CreateException;

  /**
   * creates a new <code>#$resultIsa</code> {@link Fact} in the supplied
   * context.
   * <p>
   * Future versions of this API may assume that the context is always
   * <code>UniversalVocabularyMt</code>.
   *
   * @param ctx	the context where the fact is asserted
   * @param col	the KBCollection the generated object is a type of
   *
   * @return	this
   *
   * @throws CreateException
   * @throws KBTypeException
   */
  public KBFunction addResultIsa(KBCollection col, Context ctx)
          throws KBTypeException, CreateException;

  /**
   * Returns the <code>#$resultGenl</code>s of this <code>KBFunction</code> from
   * the default context specified by {@link KBAPIDefaultContext#forQuery()}.
   * Any functional term using this <code>KBFunction</code> will be a
   * specialization of each of these <code>KBCollection</code>s. The set of
   * collections defines the Range of this function.
   * <p>
   *
   * @return	all <code>#$resultGenl</code> collections for this function
   * @throws KBApiException
   */
  // @todo Check if we can throw a better exception
  public Collection<KBCollection> getResultGenl() throws KBApiException;

  /**
   * Returns the <code>#$resultGenl</code>s of this <code>KBFunction</code> from
   * the context. Any functional term using this <code>KBFunction</code> will be
   * a specialization of each of these <code>KBCollection</code>s. The set of
   * collections defines the Range of this function.
   * <p>
   *
   * @param	ctxStr the string representation of the context of query
   *
   * @return	all <code>#$resultGenl</code> collections for this function that
   * are visible from the context
   */
  public Collection<KBCollection> getResultGenl(String ctxStr);

  /**
   * Returns the <code>#$resultGenl</code>s of this <code>KBFunction</code> from
   * the context. Any functional term using this <code>KBFunction</code> will be
   * a specialization of each of these <code>KBCollection</code>s. The set of
   * collections defines the Range of this function.
   * <p>
   *
   * @param	ctx the context of query
   *
   * @return	all <code>#$resultGenl</code> collections for this function that
   * are visible from the context
   */
  public Collection<KBCollection> getResultGenl(Context ctx);

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
   * @throws KBTypeException
   */
  public KBFunction addResultGenl(String colStr, String ctxStr)
          throws KBTypeException, CreateException;

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
   * @throws KBTypeException
   */
  public KBFunction addResultGenl(KBCollection col, Context ctx)
          throws KBTypeException, CreateException;

}
