/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal.cache;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gemstone.gemfire.InternalGemFireException;
import com.gemstone.gemfire.cache.CacheRuntimeException;
import com.gemstone.gemfire.cache.RegionDestroyedException;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.cache.query.internal.ExecutionContext;
import com.gemstone.gemfire.cache.query.internal.IndexTrackingQueryObserver;
import com.gemstone.gemfire.cache.query.internal.QueryExecutionContext;
import com.gemstone.gemfire.cache.query.internal.QueryMonitor;
import com.gemstone.gemfire.cache.query.internal.QueryObserver;
import com.gemstone.gemfire.cache.query.internal.QueryObserverHolder;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.DataSerializableFixedID;
import com.gemstone.gemfire.internal.cache.PartitionedRegionQueryEvaluator.PRQueryResultCollector;
import com.gemstone.gemfire.internal.cache.execute.BucketMovedException;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.shared.Version;


/**
 * This class takes the responsibility of executing the query on a data store
 * for the buckets specified in bucketList. It contains a
 * <code>PRQueryExecutor</code> thread-pool executor that takes a
 * <code>Callable</code> task identified by <code>PartitionedRegion</code>,
 * queryString and bucketId.
 * 
 * The QueryTasks add results directly to a results queue.
 * The BucketQueryResult is used not only to indicate completion, and holds an exception if there one occurred while
 * processing a query.
 *
 * @author rreja
 * @author Eric Zoerner
 */
public class PRQueryProcessor
{
  final static int BUCKET_QUERY_TIMEOUT = 60;
  
  public final static int NUM_THREADS  = Integer.getInteger("gemfire.PRQueryProcessor.numThreads", 1).intValue();

  /* For Test purpose */
  public static int TEST_NUM_THREADS = 0;
  
  private PartitionedRegionDataStore _prds;
  private PartitionedRegion pr;
  private final DefaultQuery query;
  private final Object[] parameters;
  private final List _bucketsToQuery;
  private volatile int numBucketsProcessed = 0;
  
  private boolean isIndexUsedForLocalQuery = false;
//  private List _failedBuckets;

  public PRQueryProcessor(PartitionedRegionDataStore prDS,
      DefaultQuery query, Object[] parameters, List buckets) {
    Assert.assertTrue(!buckets.isEmpty(), "bucket list can not be empty. ");
    this._prds = prDS;
    this._bucketsToQuery = buckets;
    ((GemFireCacheImpl)prDS.partitionedRegion.getCache()).getLocalQueryService();
    this.query = query;
    this.parameters = parameters;
    PRQueryExecutor.initializeExecutorService();
  }

  public PRQueryProcessor(PartitionedRegion pr,
      DefaultQuery query, Object[] parameters, List buckets) {
    Assert.assertTrue(!buckets.isEmpty(), "bucket list can not be empty. ");
    this.pr = pr;
    this._bucketsToQuery = buckets;
    this.query = query;
    this.parameters = parameters;
    PRQueryExecutor.initializeExecutorService();
  }

  private synchronized void incNumBucketsProcessed() {
    this.numBucketsProcessed++;
  }
  
  private synchronized int getNumBucketsProcessed() {
    return this.numBucketsProcessed;
  }
  
  /**
   * Executes a pre-compiled query on a data store.
   * Adds result objects to resultQueue
   * @throws QueryException
   * @throws ForceReattemptException if query should be tried again
   */
  public void executeQuery(Collection<Collection> resultCollector)
    throws QueryException, InterruptedException, ForceReattemptException {   
    //Set indexInfoMap to this threads observer.
    //QueryObserver observer = QueryObserverHolder.getInstance();
    //if(observer != null && observer instanceof IndexTrackingQueryObserver){
      //((IndexTrackingQueryObserver)observer).setIndexInfo(resultCollector.getIndexInfoMap());
    //}
    
    if (NUM_THREADS > 1 || this.TEST_NUM_THREADS > 1) {  
      executeWithThreadPool(resultCollector);
    } else {
      executeSequentially(resultCollector, this._bucketsToQuery);
    }    
  }
  
  private void executeWithThreadPool(Collection<Collection> resultCollector)
    throws QueryException, InterruptedException, ForceReattemptException {
    if (Thread.interrupted()) throw new InterruptedException();
      
    java.util.List callableTasks = buildCallableTaskList(resultCollector);
    ExecutorService execService = PRQueryExecutor.getExecutorService();

    boolean reattemptNeeded = false;
    ForceReattemptException fre = null;
    
    if (callableTasks != null && !callableTasks.isEmpty()) {
      List futures = null;
      try {
        futures = execService.invokeAll(callableTasks, 300, TimeUnit.SECONDS);
      }
      catch (RejectedExecutionException rejectedExecutionEx) {
        //this._prds.partitionedRegion.checkReadiness();
        throw rejectedExecutionEx;
      }
      
      if (futures != null) {
        Iterator itr = futures.iterator();
        while (itr.hasNext() && !execService.isShutdown() && !execService.isTerminated()) {
          //this._prds.partitionedRegion.checkReadiness();
          Future fut = (Future)itr.next();
          QueryTask.BucketQueryResult bqr = null;
          
          try {
            bqr = (QueryTask.BucketQueryResult)fut.get(BUCKET_QUERY_TIMEOUT, TimeUnit.SECONDS);
            //if (retry.booleanValue()) {
            //  reattemptNeeded = true;
              //fre = (ForceReattemptException)bqr.getException();
            //} else {
              bqr.handleAndThrowException(); // handles an exception if there was one,
              //  otherwise, the results have already been added to the resultQueue
            //}
            if (bqr.retry) {
              reattemptNeeded = true;
            }
            
          } catch (TimeoutException e) {
            throw new InternalGemFireException(LocalizedStrings.PRQueryProcessor_TIMED_OUT_WHILE_EXECUTING_QUERY_TIME_EXCEEDED_0.toLocalizedString(
                Integer.valueOf(BUCKET_QUERY_TIMEOUT)), e);
          } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof QueryException) {
              throw (QueryException)cause;
            }
            else {
              throw new InternalGemFireException(LocalizedStrings.PRQueryProcessor_GOT_UNEXPECTED_EXCEPTION_WHILE_EXECUTING_QUERY_ON_PARTITIONED_REGION_BUCKET.toLocalizedString(), 
              cause);
            }
          }
        }
      }
    }
    
    if (execService == null || execService.isShutdown()
        || execService.isTerminated()) {
      this._prds.partitionedRegion.checkReadiness();
    }
    
    if (reattemptNeeded) {
      throw fre;
    }
    
  }

  /**
   * @throws ForceReattemptException
   *           if bucket was moved so caller should try query again
   */
  private void doBucketQuery(final Integer bId,
                                    final PartitionedRegionDataStore prds,
                                    final DefaultQuery query,
                                    final Object[] params,
                                    final PRQueryResultCollector rq)
    throws QueryException, ForceReattemptException, InterruptedException {
    final BucketRegion bukRegion = (BucketRegion)prds.localBucket2RegionMap.get(bId);
    final PartitionedRegion pr = prds.getPartitionedRegion();
    try {
      pr.checkReadiness();
      if (bukRegion == null) {
        if (pr.isLocallyDestroyed || pr.isClosed) {
          throw new RegionDestroyedException("PR destroyed during query", pr.getFullPath());
        } else {
          throw new ForceReattemptException("Bucket id "
                                            + pr.bucketStringForLogs(bId.intValue())
                                            + " not found on VM "
                                            + pr.getMyId());
        }
      }
      bukRegion.waitForData();
      SelectResults results = null;
      
      // If the query has LIMIT and is not order by, apply the limit while building the result set.
      int limit = -1;
      if (query.getSimpleSelect().getOrderByAttrs() == null) {
        limit = query.getLimit(params);
      }
      
      if (!bukRegion.isBucketDestroyed()) {     
        // If the result queue has reached the limit, no need to 
        // execute the query. Handle the bucket destroy condition 
        // and add the end bucket token.
        int numBucketsProcessed = getNumBucketsProcessed();
        if (limit < 0 || (rq.size() - numBucketsProcessed) < limit) {
          results = (SelectResults) query.prExecuteOnBucket(params, pr,
              bukRegion);
        } 
        
        if (!bukRegion.isBucketDestroyed()) {
          // someday, when queries can return objects as a stream, the entire results set won't need to be manifested
          // here before we can start adding to the results queue
          if (results != null) {
            for (Object r : results){
              if (r == null) { // Blocking queue does not support adding null.
                rq.put(DefaultQuery.NULL_RESULT);              
              } else {
                // Count from each bucket should be > 0 otherwise limit makes the final result wrong.
                // Avoid if query is distinct as this Integer could be a region value. 
                if (!query.getSimpleSelect().isDistinct() && 
                    query.getSimpleSelect().isCount() && r instanceof Integer) {
                  if (((Integer) r).intValue() != 0 ) {
                    rq.put(r);
                  }
                } else {
                  rq.put(r);
                }
              }
            
              // Check if limit is satisfied.
              if (limit >= 0 && (rq.size() - numBucketsProcessed) >= limit) {
                break;
              }
            }
          }
          rq.put(new EndOfBucket(bId.intValue()));
          this.incNumBucketsProcessed();
          return; // success
        }
      }

      // if we get here then the bucket must have been moved
      checkForBucketMoved(bId, bukRegion, pr);
      Assert.assertTrue(false, "checkForBucketMoved should have thrown ForceReattemptException");
    } catch (RegionDestroyedException rde) {
      checkForBucketMoved(bId, bukRegion, pr);
      throw rde;
    } catch (QueryException qe) {
      checkForBucketMoved(bId, bukRegion, pr);
      throw qe;
    }
  }

  /**
   * @throws ForceReattemptException if it detects that the given bucket moved
   * @throws RegionDestroyedException if the given pr was destroyed 
   */
  private static void checkForBucketMoved(Integer bId, BucketRegion br, PartitionedRegion pr)
    throws ForceReattemptException, RegionDestroyedException {
    if (br.isBucketDestroyed()) {
      // see if the pr is destroyed
      if (pr.isLocallyDestroyed || pr.isClosed) {
        throw new RegionDestroyedException("PR destroyed during query", pr.getFullPath());
      }
      pr.checkReadiness();
      throw new ForceReattemptException("Bucket id "
                                        + pr.bucketStringForLogs(bId.intValue())
                                        + " not found on VM "
                                        + pr.getMyId());
    }
  }
                                    
  private void executeSequentially(Collection<Collection> resultCollector, List buckets)
    throws QueryException, InterruptedException, ForceReattemptException {
    /*
    for (Iterator itr = _bucketsToQuery.iterator(); itr.hasNext(); ) {
      Integer bId = (Integer)itr.next();
      doBucketQuery(bId, this._prds, this.query, this.parameters, resultCollector);
    }*/
    
    ExecutionContext context = new QueryExecutionContext(this.parameters, this.pr.getCache(), this.query);
    context.setBucketList(buckets);
    context.setCqQueryContext(query.isCqQuery());
    
    // Check if QueryMonitor is enabled, if so add query to be monitored.
    QueryMonitor queryMonitor = null;
    if (GemFireCacheImpl.getInstance() != null)
    {
      queryMonitor = GemFireCacheImpl.getInstance().getQueryMonitor();
    }
    
    try {
      if (queryMonitor != null) {
        // Add current thread to be monitored by QueryMonitor.
        queryMonitor.monitorQueryThread(Thread.currentThread(), query);
      }
      
      SelectResults results = (SelectResults)query.executeUsingContext(context);
      synchronized(resultCollector) {
        resultCollector.add(results);
      }
      isIndexUsedForLocalQuery = context.isIndexUsed();
      
    } catch (BucketMovedException bme) {
      if (this.pr.getLogWriterI18n().fineEnabled()) {
        this.pr.getLogWriterI18n().fine("Query targeted local bucket not found." + bme.getMessage());
      }
      throw new ForceReattemptException("Query targeted local bucket not found." + bme.getMessage(), bme);
    } catch (RegionDestroyedException rde) {
      throw new QueryInvocationTargetException("The Region on which query is executed may have been destroyed." +
          rde.getMessage(), rde);
    } catch (QueryException qe) {
      // Check if PR is locally destroyed.
      if (pr.isLocallyDestroyed || pr.isClosed) {
        throw new ForceReattemptException("Local Partition Region or the targeted bucket has been moved");
      } 
      throw qe;
    } finally {
      if (queryMonitor != null) {
        queryMonitor.stopMonitoringQueryThread(Thread.currentThread(), query);
      }
    }
  }

  private List buildCallableTaskList(Collection<Collection> resultsColl)
  {
    List callableTasks = new ArrayList();
    for (Iterator itr = _bucketsToQuery.iterator(); itr.hasNext();) {
      Integer bId = (Integer)itr.next();
      callableTasks.add(new QueryTask(this.query, this.parameters, _prds, bId, resultsColl));
    }
    return callableTasks;
  }

  public boolean isIndexUsed() {
    return isIndexUsedForLocalQuery;
  }
  
  public static void shutdown()
  {
    PRQueryExecutor.shutdown();   
  }

  public static void shutdownNow()
  {
    PRQueryExecutor.shutdownNow();
  }

  /**
   * A ThreadPool ( Fixed Size ) with an executor service to execute the query
   * execution spread over buckets.
   * 
   * @author rreja
   * 
   */
  static class PRQueryExecutor {

    private static ExecutorService execService = null;

    /**
     * Closes the executor service. This is called from
     * {@link PartitionedRegion#afterRegionsClosedByCacheClose(GemFireCacheImpl)}
     */
    static synchronized void shutdown() {
      if (execService != null) {
        execService.shutdown();
      }
    }

    static synchronized void shutdownNow() {
      if (execService != null)
        execService.shutdownNow();
    }

    static synchronized ExecutorService getExecutorService() {
      if (execService == null) {
        initializeExecutorService();
      }
      assert execService != null;
      return execService;
    }

    /**
     * Creates the Executor Service.
     */
    static synchronized void initializeExecutorService() {
      if (execService == null || execService.isShutdown()
          || execService.isTerminated()) {
        int numThreads = (TEST_NUM_THREADS > 1 ? TEST_NUM_THREADS : NUM_THREADS);
        execService = Executors.newFixedThreadPool(numThreads);
      }
    }
  }
  
  /**
    * Status token placed in results stream to track completion of
   * query results for a given bucket
   */
  public static final class EndOfBucket implements DataSerializableFixedID {
    
    private int bucketId;
    
    /** Required by DataSerializer */
    public EndOfBucket() {
    }
    
    public EndOfBucket(int bucketId) {
      this.bucketId = bucketId;
    }
    
    public int getBucketId() {
      return this.bucketId;
    }
    
    @Override
    public String toString() {
      return "EndOfBucket(" + this.bucketId + ")";
    }
    
    public int getDSFID() {
      return END_OF_BUCKET;
    }

    public void fromData(DataInput in)
    throws IOException, ClassNotFoundException {
      this.bucketId = in.readInt();
    }
    
    public void toData(DataOutput out) throws IOException {
      out.writeInt(this.bucketId);
    }

    @Override
    public Version[] getSerializationVersions() {
      return null;
    }    
  }
  
  /**
   * Implementation of call-able task to execute query on a bucket region. This
   * task will be generated by the PRQueryProcessor.
   * 
   * @author rreja
   */
  @SuppressWarnings("synthetic-access")
  private final class QueryTask implements Callable {
    private final DefaultQuery query;
    private final Object[] parameters;
    private final PartitionedRegionDataStore _prDs;
    private final Integer _bucketId;
    private final Collection<Collection> resultColl;
    
    public QueryTask(DefaultQuery query, Object[] parameters, PartitionedRegionDataStore prDS, 
        Integer bucketId, final Collection<Collection> rColl) {
      this.query = query;
      this._prDs = prDS;
      this._bucketId = bucketId;
      this.resultColl = rColl;
      this.parameters = parameters;
    }
    
    public Object call() throws Exception {
      BucketQueryResult bukResult = new BucketQueryResult(this._bucketId);
      boolean retry = false;
      try {
        //Add indexInfo of this thread to result collector
        QueryObserver observer = QueryObserverHolder.getInstance();
        if (observer != null && observer instanceof IndexTrackingQueryObserver) {
          //((IndexTrackingQueryObserver)observer).setIndexInfo(resultColl.getIndexInfoMap());
        }
        
        final Integer bId = Integer.valueOf(this._bucketId);
        ArrayList bucketList = new ArrayList();
        bucketList.add(this._bucketId);
        executeSequentially(this.resultColl, bucketList);
        // success
        //doBucketQuery(bId, this._prDs, this.query, this.parameters, this.resultColl);
      } catch (ForceReattemptException fre) {
        bukResult.setException(fre);
      } catch (QueryException e) {
        bukResult.setException(e);
      } catch (CacheRuntimeException cre) {
        bukResult.setException(cre);
      }
      // Exception
      return bukResult;
    }
    
    /**
      * Encapsulates the result for the query on the bucket.
     * 
     * @author rreja
     */
    private final class BucketQueryResult {
      
      private int _buk;
      private Exception _ex = null;
      public boolean retry = false;
      
      /**
        * Constructor
       * 
       * @param bukId
       */
      public BucketQueryResult(int bukId) {
        this._buk = bukId;
      }
      
      public Exception getException()
      {
        return _ex;
      }
      
      public boolean exceptionOccured()
      {
        return _ex != null;
      }
      
      public void setException(Exception e)
      {
        this._ex = e;
      }
      
      public Integer getBucketId()
      {
        return Integer.valueOf(this._buk);
      }
      
      public boolean isReattemptNeeded() {
        return this._ex instanceof ForceReattemptException;
      }
      
      public void handleAndThrowException() throws QueryException
      {
        if (_ex != null) {
          if (_ex instanceof QueryException) {
            throw (QueryException)_ex;
          }
          else if (_ex instanceof CacheRuntimeException) {
            throw (CacheRuntimeException)_ex;
          }
        }
      }
    }  
  }
}

