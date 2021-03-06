include $JTESTS/hydraconfig/hydraparams1.inc;
include $JTESTS/hydraconfig/performance.inc;
include $JTESTS/hydraconfig/topology_hct.inc;
include $JTESTS/hydraconfig/single_host.inc;
include $JTESTS/smoketest/perf/statmon.inc;

hydra.Prms-testDescription = "Empty edges continuously query entries in datastore.  Entries are Serializable objects.";

hydra.CachePrms-names = datastore query;

hydra.BridgePrms-names = datastore;

hydra.PoolPrms-names = pool;
hydra.PoolPrms-contactAlgorithm = hydra.EdgeHelper getRoundRobinEndpoints;
hydra.PoolPrms-contactNum = 1;
hydra.PoolPrms-minConnections = 1;
hydra.PoolPrms-subscriptionEnabled = true;
hydra.PoolPrms-readTimeout = 100000;
hydra.PoolPrms-threadLocalConnections = true;

hydra.VmPrms-extraVMArgs = "-Xms275m -Xmx275m";
hydra.VmPrms-extraVMArgsSUN = -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC;
hydra.VmPrms-extraClassPaths = $GEMFIRE/lib/antlr.jar;

INITTASK  taskClass = cacheperf.CachePerfClient taskMethod = openStatisticsTask
          ;
INITTASK  taskClass = cacheperf.CachePerfClient taskMethod = openCacheTask
          hydra.ConfigPrms-cacheConfig = datastore
          hydra.ConfigPrms-regionConfig = datastore
          hydra.ConfigPrms-bridgeConfig = datastore
          threadgroups = datastore
          ;
INITTASK  taskClass = cacheperf.CachePerfClient taskMethod = openCacheTask
          distcache.gemfire.GemFireCachePrms-interestResultPolicy = none
          hydra.ConfigPrms-cacheConfig = query
          hydra.ConfigPrms-regionConfig = query
          threadgroups = query
          ;

CLOSETASK taskClass = cacheperf.CachePerfClient taskMethod = closeCacheTask
          threadGroups = query
          ;
CLOSETASK taskClass = cacheperf.CachePerfClient taskMethod = closeCacheTask
          threadGroups = datastore
          ;
CLOSETASK taskClass = cacheperf.CachePerfClient taskMethod = closeStatisticsTask
          ;
CLOSETASK taskClass = cacheperf.CachePerfClient taskMethod = reportTrimIntervalsTask
          ;

THREADGROUP datastore
    totalThreads = fcn ${bridgeHosts} * ${bridgeVMsPerHost}
                                      * ${bridgeThreadsPerVM} ncf
    clientNames  = fcn "hydra.TestConfigFcns.generateNames
                        (\"bridge\", ${bridgeHosts}, true)" ncf;
THREADGROUP query
    totalThreads = fcn ${edgeHosts} * ${edgeVMsPerHost}
                                    * ${edgeThreadsPerVM} ncf
    clientNames  = fcn "hydra.TestConfigFcns.generateNames
                        (\"edge\", ${edgeHosts}, true)" ncf;

cacheperf.comparisons.cq.CQPrms-cqListener =
          cacheperf.comparisons.cq.CQLatencyListener;
cacheperf.comparisons.cq.CQPrms-useMultipleWhereConditionsInCQs = true;
cacheperf.comparisons.cq.CQPrms-numCQs = 12;
cacheperf.CachePerfPrms-maxKeys   = 200000;
cacheperf.CachePerfPrms-keyType   = objects.BatchString;
objects.BatchStringPrms-batchSize = 50000; // 25% of maxKeys

cacheperf.CachePerfPrms-objectType = objects.BatchObject;
objects.BatchObjectPrms-size       = ${dataSize};

cacheperf.CachePerfPrms-batchTerminatorMethod     = terminateOnBatchSeconds;
cacheperf.CachePerfPrms-batchSeconds              = 360;
cacheperf.CachePerfPrms-warmupTerminatorMethod    = terminateOnTrimSeconds;
cacheperf.CachePerfPrms-warmupTerminatorFrequency = 10 seconds;
cacheperf.CachePerfPrms-trimSeconds               = ${trimSeconds};
cacheperf.CachePerfPrms-taskTerminatorMethod      = terminateOnTotalSeconds;
cacheperf.CachePerfPrms-taskTerminatorFrequency   = 10 seconds;
cacheperf.CachePerfPrms-workSeconds               = 180;

hydra.Prms-totalTaskTimeSec = 259200;  // this test is workload based
hydra.Prms-maxResultWaitSec = 600;
