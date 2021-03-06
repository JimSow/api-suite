package com.cyc.query;

/*
 * #%L
 * File: QueryImplTest.java
 * Project: Query Client
 * %%
 * Copyright (C) 2013 - 2016 Cycorp, Inc.
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
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.base.CycAccessManager;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.inference.params.OpenCycInferenceParameterEnum;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbFactory;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.Constants;
import com.cyc.kb.client.KbIndividualImpl;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.client.VariableImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import static com.cyc.query.QueryTestConstants.emu;
import static com.cyc.query.QueryTestConstants.zebra;
import static com.cyc.query.QueryImpl.QUERY_LOADER_REQUIREMENTS;
import static com.cyc.query.TestUtils.assumeCycSessionRequirements;
import static com.cyc.query.TestUtils.assumeKBObject;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.UnsupportedCycOperationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class QueryImplTest {

  private static final String queryStringAssembling = "(#$disjointWith #$Assembling ?COLL)";
  private static final String queryStringAbesAPresident = "(#$isa #$AbrahamLincoln #$UnitedStatesPresident)";
  private static final String queryStringConditional = "(implies " + queryStringAssembling + " " + queryStringAbesAPresident + ")";
  private Query q = null;
  private QueryResultSet r = null;

  @BeforeClass
  public static void setUpClass() throws Exception {
    TestUtils.ensureConstantsInitialized();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    if (q != null) {
      q.close();
    }
  }

  @Test
  public void testGetInferenceIdentifier() throws IOException, QueryConstructionException, SessionCommunicationException {
    System.out.println("testGetInferenceIdentifier");
    q = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt());
    q.retainInference();
    assertNull(q.getInferenceIdentifier());
    q.addListener(new QueryListener() {
      int problemStoreID;

      @Override
      public void notifyInferenceCreated(Query query) {
        try {
          assertNotNull(q.getInferenceIdentifier());
          problemStoreID = q.getInferenceIdentifier().getProblemStoreId();
          System.out.println("Problem store ID: " + problemStoreID);
          assertTrue("Got problem store ID " + problemStoreID, problemStoreID > 1);
        } catch (SessionCommunicationException ex) {
          ex.printStackTrace();
          throw new RuntimeException(ex);
        }
      }

      @Override
      public void notifyInferenceStatusChanged(InferenceStatus oldStatus, InferenceStatus newStatus, InferenceSuspendReason suspendReason, Query query) {
        System.out.println(oldStatus + " -> " + newStatus);
      }

      @Override
      public void notifyInferenceAnswersAvailable(Query query, List<QueryAnswer> newAnswers) {
        try {
          final int problemStoreID1 = q.getInferenceIdentifier().getProblemStoreId();
          assertEquals("Inference answers available; problem store ID now " + problemStoreID1,
                  problemStoreID, problemStoreID1);
        } catch (SessionCommunicationException ex) {
          ex.printStackTrace();
          throw new RuntimeException(ex);
        }
      }

      @Override
      public void notifyInferenceTerminated(Query query, Exception e) {
        try {
          final int problemStoreID1 = q.getInferenceIdentifier().getProblemStoreId();
          assertEquals("Inference terminated; problem store ID now " + problemStoreID1,
                  problemStoreID, problemStoreID1);
        } catch (SessionCommunicationException ex) {
          ex.printStackTrace();
          throw new RuntimeException(ex);
        }
      }
    });
    q.performInference();
    assertNotNull(q.getInferenceIdentifier());
  }

  @Test
  public void testQueryString() throws QueryConstructionException {
      String queryStr = queryStringAssembling;
      q = QueryFactory.getQuery(queryStr);
      QueryResultSet results = q.getResultSet();
      while (results.next()) {
        results.getObject("?COLL", KbObject.class);
      }
  }

  @Test
  public void testBooleanQueryString() throws QueryConstructionException {
      String queryStr = queryStringAbesAPresident;
      q = QueryFactory.getQuery(queryStr);
      assertTrue(q.isTrue());
  }

  @Test
  public void testCycAssertionAsBinding() throws KbException, QueryConstructionException, SessionCommunicationException, CycConnectionException {
    final VariableImpl var = new VariableImpl("AS");
    q = QueryFactory.getQuery(new SentenceImpl(testConstants().assertionSentence, var,
            testConstants().genlsAnimalX), Constants.inferencePSCMt());
    q.setMaxAnswerCount(1);
    final Object binding = ((QueryImpl)q).getAnswerCyc(0).getBinding(CycObjectFactory.makeCycVariable(var.getName()));
    assertTrue("Wanted a CycAssertion, got " + binding.getClass().getSimpleName(),
            binding instanceof CycAssertion);
  }

  @Test
  public void testFactAsBinding() throws KbException, QueryConstructionException, SessionCommunicationException {
    final VariableImpl var = new VariableImpl("AS");
    q = QueryFactory.getQuery(new SentenceImpl(testConstants().assertionSentence, var,
            testConstants().genlsAnimalX), Constants.inferencePSCMt());
    q.setMaxAnswerCount(1);
    final Object binding = q.getAnswer(0).getBinding(var);
    assertTrue("Wanted a Fact, got " + binding.getClass().getSimpleName(),
            binding instanceof Fact);
  }

  @Test
  public void testQueryStringString() throws QueryConstructionException {
      r = QueryFactory.getQuery(testConstants().whatIsAbe.toString(), testConstants().peopleDataMt.toString()).getResultSet();
      while (r.next()) {
        System.out.println("All types: " + r.getObject("?TYPE", KbCollection.class));
      }
  }

  private QueryTestConstants testConstants() throws KbRuntimeException {
    return QueryTestConstants.getInstance();
  }

  @Test
  public void testContinuableQuery() throws QueryConstructionException {
    System.out.println("testContinuableQuery");
    q = QueryFactory.getQuery(testConstants().whatIsAbe, testConstants().peopleDataMt);
    q.setInferenceMode(OpenCycInferenceParameterEnum.OpenCycInferenceMode.MINIMAL_MODE);
    q.setMaxAnswerCount(1);
    q.setContinuable(true);
    assertTrue("Query not continuable.", q.isContinuable());
    int answerCount = q.getAnswerCount();
    assertEquals("Expected one answer, got " + answerCount, 1, answerCount);
    q.continueQuery();
    int updatedAnswerCount = q.getAnswerCount();
    while (updatedAnswerCount > answerCount) {
      answerCount = updatedAnswerCount;
      q.continueQuery();
      updatedAnswerCount = q.getAnswerCount();
    }
    assertTrue("Found only " + answerCount + " answers.", answerCount > 1);
  }

  @Test
  public void testNonContinuableQuery() throws QueryConstructionException {
    System.out.println("test setContinuable(false)");
      q = QueryFactory.getQuery(testConstants().whatIsAbe, testConstants().peopleDataMt);
      q.setMaxAnswerCount(1);
      q.setInferenceMode(OpenCycInferenceParameterEnum.OpenCycInferenceMode.SHALLOW_MODE);
      q.setContinuable(false);
      assertFalse("Query parameters are continuable.", q.getInferenceParameters().isContinuable());
      q.continueQuery();
      assertFalse("Query is continuable.", q.isContinuable());
  }

  @Test
  public void testQueryStringStringString() throws QueryConstructionException {
      String queryStr = testConstants().whatIsAbe.toString();

      r = QueryFactory.getQuery(queryStr, testConstants().peopleDataMt.toString(),
              ":INFERENCE-MODE :MINIMAL :MAX-TIME 1 :MAX-NUMBER 12").getResultSet();
      while (r.next()) {
        System.out.println("TYPE: " + r.getObject("?TYPE", KbObject.class));
      }
  }

  /**
   * Test of getId method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testGetId() throws QueryConstructionException, KbException, SessionCommunicationException {
    System.out.println("getId and saveAs");
    q = QueryFactory.getQuery(queryStringAssembling);
    final KbIndividual id = q.saveAs("TestQuery-AssemblingSlots");
    try {
      assertEquals(id, q.getId());
    } finally {
      id.delete();
    }
  }

  /**
   * Test of load method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testLoad() throws QueryConstructionException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("load");
    //Query conceptFinder = QueryImpl.load(new KBIndividual("AURORAQuery-PredictAllFeaturesFromReifiedVideosUsingThisFeatureSet"));
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    q = QueryFactory.getQuery(queryStringAssembling);
    final KbIndividual id = q.saveAs("TestQuery-AssemblingSlots");
    try {
      final Query loadedQuery = QueryFactory.getQuery(id);
      assertEquals("Query contexts are different.", q.getContext(),
              loadedQuery.getContext());
      assertEquals("Query sentences are different.", ((QueryImpl)q).getQuerySentenceCyc(),
              ((QueryImpl)loadedQuery).getQuerySentenceCyc());
    } finally {
      id.delete();
    }
  }

  @Test
  public void testKBQIndexical() throws QueryConstructionException, DeleteException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexical");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    q = QueryFactory.getQuery(testConstants().theAnimalIsAnAnimal, Constants.inferencePSCMt());
    q.setMaxAnswerCount(10);
    KbIndividual z = q.saveAs("TestKBQueryIndexical-2012-01-16"); 

    try {
      Map<KbObject, Object> indexicals = new HashMap<KbObject, Object>();
      indexicals.put(testConstants().theAnimal,
              CycObjectFactory.makeCycVariable("X"));

      Query query = QueryFactory.getQuery(KbIndividualImpl.get("TestKBQueryIndexical-2012-01-16"),
              indexicals);
      r = query.getResultSet();
      while (r.next()) {
        System.out.println("Animal: " + r.getObject("?X", KbIndividual.class));
      }
    } finally {
      q.getId().delete();
    }
  }

  @Test
  public void testKBQIndexicalKBObject() throws QueryConstructionException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexicalKBObject");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    q = QueryFactory.getQuery(testConstants().theAnimalIsAnAnimal, Constants.inferencePSCMt());
    q.setMaxAnswerCount(10);
    q.saveAs("TestKBQueryKBObject-2014-04-1");

    try {
      Map<KbObject, Object> indexicals = new HashMap<KbObject, Object>();
      indexicals.put(testConstants().theAnimal, new VariableImpl("X"));

      r = QueryFactory.getQuery(KbIndividualImpl.get("TestKBQueryKBObject-2014-04-1"),
              indexicals).getResultSet();
      while (r.next()) {
        System.out.println("Animal: " + r.getObject("?X", KbIndividual.class));
      }
    } finally {
      q.getId().delete();
    }
  }

  @Test
  public void testKBQIndexicalString() throws QueryConstructionException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexicalString");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    q = QueryFactory.getQuery(testConstants().theAnimalIsAnAnimal, Constants.inferencePSCMt());
    q.setMaxAnswerCount(10);
    q.saveAs("TestKBQuery-Vijay-2012-01-16");
    try {
      // The bound query is converted to an unbound query.
      // The "(#$TheFn #$Dog)" in the query is replaced with "?X".
      Map<String, String> indexicals = new HashMap<String, String>();
      indexicals.put(testConstants().theAnimal.toString(), "?X");

      r = QueryFactory.getQuery("TestKBQuery-Vijay-2012-01-16", indexicals).getResultSet();

      while (r.next()) {
        System.out.println("Animal: " + r.getObject("?X", KbIndividual.class));
      }
    } finally {
      q.getId().delete();
    }
  }

  @Test
  public void testKBQIndexicalVarToConst() throws QueryConstructionException, KbTypeException, CreateException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexicalVarToConst");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    
    q = QueryFactory.getQuery(
            SentenceImpl.and(testConstants().xIsAnAnimal, testConstants().yOwnsX),
            Constants.everythingPSCMt());
    q.saveAs("TestKBQueryAnimalOwners-Vijay-2012-01-16");

    try {
      Map<KbObject, Object> indexicals = new HashMap<KbObject, Object>();
      indexicals.put(new VariableImpl("Y"), testConstants().abrahamLincoln);
      final Query loadedQuery = QueryFactory.getQuery(KbIndividualImpl.get("TestKBQueryAnimalOwners-Vijay-2012-01-16"), indexicals);
      System.out.println("Result of replacing variable: " + loadedQuery);
      r = loadedQuery.getResultSet();
      System.out.println("Result set: " + r);
      while (r.next()) {
        System.out.println("Answer " + r.getObject("?X", KbIndividual.class));
      }
      loadedQuery.close();
    } finally {
      q.getId().delete();
    }
  }

  @Test
  //@TODO Replace this with a test using vocabulary in OpenCyc (or at least RCyc).
  public void testKBQIndexicalAurora() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException, CycConnectionException, CreateException, KbTypeException, QueryConstructionException, KbException, UnsupportedCycOperationException {
    if (!CycAccessManager.getCurrentAccess().isOpenCyc()
            && CycAccessManager.getCurrentAccess().getLookupTool().find("AuroraConceptIDSourceStore") instanceof CycConstant) {
      System.out.println("testKBQIndexicalAurora");
      Map<KbObject, Object> binding = new HashMap<KbObject, Object>();
      binding.put(VariableImpl.get("?VIDEO-ID"), 27850);
      //Idiotically, need #$ here
      binding.put(KbIndividualImpl.findOrCreate("(#$TheFn #$AuroraConceptIDSourceStore)"),
              CycAccessManager.getCurrentAccess().getLookupTool().getKnownFortByName("MED12-SIN-Concept-List"));

      q = QueryFactory.getQuery(KbIndividualImpl.get(
                      "AURORAQuery-PredictAllFeaturesFromReifiedVideosUsingThisFeatureSet"),
              binding);
      q.setMaxTime(30).setMaxAnswerCount(10);
      r = q.getResultSet();
      while (r.next()) {
        System.out.println("Answer " + r.getObject("?READABLE",
                String.class));
      }
    }
  }

  /**
   * Test of getCategories method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetCategories() throws IOException, QueryConstructionException {
    System.out.println("getCategories");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertTrue(q.getCategories().isEmpty());
  }

  /**
   * Test of addCategory method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testAddCategory() throws IOException, QueryConstructionException {
    System.out.println("addCategory");
    q = QueryFactory.getQuery(queryStringAssembling);
    String cat = "Test Queries";
    q.addCategory(cat);
    assertTrue(q.getCategories().contains(cat));
  }

  /**
   * Test of getAnswerCount method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetAnswerCount() throws QueryConstructionException {
    System.out.println("getAnswerCount");
    q = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt());
    assertEquals(2, q.getAnswerCount());
  }

  @Test
  public void testGetAnswerSentence() throws QueryConstructionException, SessionCommunicationException, KbException {
    System.out.println("getAnswerSentence");
    q = QueryFactory.getQuery(testConstants().genlsSpecGenls, Constants.uvMt());
    q.setMaxAnswerCount(10);  
    
    final Variable varGenls = KbFactory.getVariable("?GENLS");
    final Variable varSpec = KbFactory.getVariable("?SPEC");
    final List<QueryAnswer> answers = q.getAnswers();
    assertFalse(answers.isEmpty());
    
    for (QueryAnswer answer : answers) {
      KbCollection bindingGenls = answer.getBinding(varGenls);
      KbCollection bindingSpec = answer.getBinding(varSpec);
      //System.out.println(varGenls + "=" + bindingGenls + "    " + varSpec + "=" + bindingSpec);
      final Sentence sentence = q.getAnswerSentence(answer);
      final Sentence inner = sentence.getArgument(1);
      System.out.println(inner);
      
      KbCollection arg1 = inner.getArgument(1);
      KbCollection arg2 = inner.getArgument(2);
      assertFalse(arg1 instanceof Variable);
      assertFalse(arg2 instanceof Variable);
      assertEquals(arg1, bindingSpec);
      //System.out.println(arg1 + " == " + bindingSpec);
      assertEquals(arg2, bindingGenls);
      //System.out.println(arg2 + " == " + bindingGenls);
    }
  }
  
  /**
   * Test of getContext method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetContext() throws QueryConstructionException {
    System.out.println("getContext");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertEquals(Constants.inferencePSCMt(), q.getContext());
  }

  /**
   * Test of getQuerySentence method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testgetQuerySentenceCyc() throws QueryConstructionException {
    System.out.println("getQuerySentence");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertEquals(queryStringAssembling, ((QueryImpl)q).getQuerySentenceCyc().cyclify());
  }

  /**
   * Test of getQuerySentenceMainClauseCyc method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testgetQuerySentenceMainClauseCyc() throws IOException, QueryConstructionException {
    System.out.println("getQuerySentenceMainClauseCyc");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertEquals(queryStringAssembling,
            ((QueryImpl)q).getQuerySentenceMainClauseCyc().cyclify());
  }

  /**
   * Test of getQuerySentenceHypothesizedClause method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetQuerySentenceHypothesizedClause() throws IOException, QueryConstructionException {
    System.out.println("getQuerySentenceHypothesizedClause");
    q = QueryFactory.getQuery(queryStringConditional);
    assertEquals(queryStringAssembling,
            ((QueryImpl)q).getQuerySentenceHypothesizedClauseCyc().cyclify());
    assertEquals(queryStringAbesAPresident,
            ((QueryImpl)q).getQuerySentenceMainClauseCyc().cyclify());
  }

  /**
   * Test of getMaxTime method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetMaxTime() throws IOException, QueryConstructionException {
    System.out.println("getMaxTime");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertEquals(null, q.getMaxTime());
  }

  /**
   * Test of getMaxNumber method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetMaxNumber() throws IOException, QueryConstructionException {
    System.out.println("getMaxNumber");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertEquals(null, q.getMaxAnswerCount());
  }

  /**
   * Test of getInferenceMode method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetInferenceMode() throws IOException, QueryConstructionException {
    System.out.println("getInferenceMode");
    q = QueryFactory.getQuery(queryStringAssembling);
    assertEquals(null, q.getInferenceMode());
  }

  /**
   * Test of getStatus method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetStatus() throws QueryConstructionException {
    System.out.println("getStatus");
    q = QueryFactory.getQuery(queryStringAssembling);
    q.performInference();
    assertEquals(InferenceStatus.SUSPENDED, q.getStatus());
  }

  /**
   * Test of get method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testGet() throws QueryConstructionException, IllegalArgumentException, KbException {
    System.out.println("get");
    r = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt()).getResultSet();
    r.next();
    final KbCollection animal = (KbCollection) r.getKbObject("?N", KbCollection.class);
    final List<KbCollection> emuAndZebra = Arrays.asList(emu(), zebra());
    assertTrue("Couldn't find " + animal + " (" + animal.getClass().getSimpleName()
            + ") in " + emuAndZebra, emuAndZebra.contains(animal));
  }

  /**
   * Test of isTrue method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testIsTrue() throws QueryConstructionException {
    System.out.println("isTrue");
    q = QueryFactory.getQuery(queryStringAbesAPresident);
    assertTrue(q.isTrue());
  }

  /**
   * Test of isProvable method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testIsProvable() throws QueryConstructionException {
    System.out.println("isProvable");
    q = QueryFactory.getQuery(queryStringAbesAPresident);
    assertTrue(q.isProvable());
    q.close();
    q = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt());
    assertTrue(q.isProvable());
    q.getResultSet().afterLast();
    assertTrue(q.isProvable());
  }

  @Test
  public void testClearResults() throws QueryConstructionException, KbException, SessionCommunicationException, InterruptedException {
    System.out.println("clearResults");
    q = QueryFactory.getQuery(testConstants().whatTimeIsIt, Constants.inferencePSCMt());
    final Variable var = q.getQuerySentence().getArgument(2);
    final Object firstTime = q.getAnswer(0).getBinding(var);
    assertEquals(firstTime, q.getAnswer(0).getBinding(var));
    q.clearResults();
    Thread.sleep(1500);
    assertFalse(firstTime.equals(q.getAnswer(0).getBinding(var)));
  }

  /**
   * Test of next method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testNext() throws QueryConstructionException {
    System.out.println("next");
    r = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt()).getResultSet();
    assertTrue(r.next());
  }

  /**
   * Test of close method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testClose() throws IOException, QueryConstructionException {
    System.out.println("close");
    q = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt());
    q.close();
  }

  /**
   * Test of getResultSet method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws java.lang.Exception
   */
  @Test
  public void testGetResultSet() throws QueryConstructionException  {
    System.out.println("getResultSet");
    q = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt());
    q.getResultSet();
  }

  /**
   * Test of getQueryVariablesCyc method, of class QueryImpl.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testGetQueryVariables() throws IOException, QueryConstructionException, KbException {
    System.out.println("getQueryVariablesCyc");
    q = QueryFactory.getQuery(testConstants().queryAnimals, Constants.inferencePSCMt());
    assertTrue(q.getQueryVariables().contains(new VariableImpl("N")));
    q.close();
    q = QueryFactory.getQuery(queryStringConditional);
    assertTrue(q.getQueryVariables().isEmpty());
  }
}
