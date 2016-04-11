package io.dropwizard.metrics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Factory and utility methods for {@link InstrumentedExecutorService},
 * {@link InstrumentedScheduledExecutorService}, and {@link InstrumentedThreadFactory}
 * classes defined in this package. This class supports the following kinds of methods:
 * <p>
 * <ul>
 * <li> Methods that create and return an {@link InstrumentedExecutorService}
 * set up with commonly useful configuration settings.
 * <li> Methods that create and return a {@link InstrumentedScheduledExecutorService}
 * set up with commonly useful configuration settings.
 * <li> Methods that create and return a "wrapped" ExecutorService, that
 * disables reconfiguration by making implementation-specific methods
 * inaccessible.
 * <li> Methods that create and return a {@link InstrumentedThreadFactory}
 * that sets newly created threads to a known state.
 * </ul>
 * </p>
 *
 * @see java.util.concurrent.Executors
 */
public final class InstrumentedExecutors {
    /**
     * Creates an instrumented thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * {@code nThreads} threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly {@link ExecutorService#shutdown shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @param name     the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     * @see Executors#newFixedThreadPool(int)
     */
    public static InstrumentedExecutorService newFixedThreadPool(int nThreads, MetricRegistry registry, String name) {
        return new InstrumentedExecutorService(Executors.newFixedThreadPool(nThreads), registry, name);
    }

    /**
     * Creates an instrumented thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * {@code nThreads} threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly {@link java.util.concurrent.ExecutorService#shutdown shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     * @see Executors#newFixedThreadPool(int)
     */
    public static InstrumentedExecutorService newFixedThreadPool(int nThreads, MetricRegistry registry) {
        return new InstrumentedExecutorService(Executors.newFixedThreadPool(nThreads), registry);
    }

    /**
     * Creates an instrumented thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most {@code nThreads} threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
     * exist until it is explicitly {@link ExecutorService#shutdown shutdown}.
     *
     * @param nThreads      the number of threads in the pool
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @param name          the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created thread pool
     * @throws NullPointerException     if threadFactory is null
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     * @see Executors#newFixedThreadPool(int, ThreadFactory)
     */
    public static InstrumentedExecutorService newFixedThreadPool(
            int nThreads, ThreadFactory threadFactory, MetricRegistry registry, String name) {
        return new InstrumentedExecutorService(Executors.newFixedThreadPool(nThreads, threadFactory), registry, name);
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most {@code nThreads} threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
     * exist until it is explicitly {@link ExecutorService#shutdown
     * shutdown}.
     *
     * @param nThreads      the number of threads in the pool
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created thread pool
     * @throws NullPointerException     if threadFactory is null
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     * @see Executors#newFixedThreadPool(int, ThreadFactory)
     */
    public static InstrumentedExecutorService newFixedThreadPool(
            int nThreads, ThreadFactory threadFactory, MetricRegistry registry) {
        return new InstrumentedExecutorService(Executors.newFixedThreadPool(nThreads, threadFactory), registry);
    }

    /**
     * Creates an InstrumentedExecutor that uses a single worker thread operating
     * off an unbounded queue. (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * {@code newFixedThreadPool(1)} the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @param name     the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created single-threaded Executor
     * @see Executors#newSingleThreadExecutor()
     */
    public static InstrumentedExecutorService newSingleThreadExecutor(MetricRegistry registry, String name) {
        return new InstrumentedExecutorService(Executors.newSingleThreadExecutor(), registry, name);
    }

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue. (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * {@code newFixedThreadPool(1)} the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created single-threaded Executor
     * @see Executors#newSingleThreadExecutor()
     */
    public static InstrumentedExecutorService newSingleThreadExecutor(MetricRegistry registry) {
        return new InstrumentedExecutorService(Executors.newSingleThreadExecutor(), registry);
    }

    /**
     * Creates an InstrumentedExecutor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed. Unlike the otherwise
     * equivalent {@code newFixedThreadPool(1, threadFactory)} the
     * returned executor is guaranteed not to be reconfigurable to use
     * additional threads.
     *
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @param name          the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     * @see Executors#newSingleThreadExecutor(ThreadFactory)
     */
    public static InstrumentedExecutorService newSingleThreadExecutor(
            ThreadFactory threadFactory, MetricRegistry registry, String name) {
        return new InstrumentedExecutorService(Executors.newSingleThreadExecutor(threadFactory), registry, name);
    }

    /**
     * Creates an InstrumentedExecutor that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed. Unlike the otherwise
     * equivalent {@code newFixedThreadPool(1, threadFactory)} the
     * returned executor is guaranteed not to be reconfigurable to use
     * additional threads.
     *
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     * @see Executors#newSingleThreadExecutor(ThreadFactory)
     */
    public static InstrumentedExecutorService newSingleThreadExecutor(
            ThreadFactory threadFactory, MetricRegistry registry) {
        return new InstrumentedExecutorService(Executors.newSingleThreadExecutor(threadFactory), registry);
    }

    /**
     * Creates an instrumented thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @param name     the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created thread pool
     * @see Executors#newCachedThreadPool()
     */
    public static InstrumentedExecutorService newCachedThreadPool(MetricRegistry registry, String name) {
        return new InstrumentedExecutorService(Executors.newCachedThreadPool(), registry, name);
    }

    /**
     * Creates an instrumented thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created thread pool
     * @see Executors#newCachedThreadPool()
     */
    public static InstrumentedExecutorService newCachedThreadPool(MetricRegistry registry) {
        return new InstrumentedExecutorService(Executors.newCachedThreadPool(), registry);
    }

    /**
     * Creates an instrumented thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available, and uses the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @param name          the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    public static InstrumentedExecutorService newCachedThreadPool(
            ThreadFactory threadFactory, MetricRegistry registry, String name) {
        return new InstrumentedExecutorService(
                Executors.newCachedThreadPool(threadFactory), registry, name);
    }

    /**
     * Creates an instrumented thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available, and uses the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     * @see Executors#newCachedThreadPool(ThreadFactory)
     */
    public static InstrumentedExecutorService newCachedThreadPool(
            ThreadFactory threadFactory, MetricRegistry registry) {
        return new InstrumentedExecutorService(Executors.newCachedThreadPool(threadFactory), registry);
    }

    /**
     * Creates a single-threaded instrumented executor that can schedule commands
     * to run after a given delay, or to execute periodically.
     * (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * {@code newScheduledThreadPool(1)} the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @param name     the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return the newly created scheduled executor
     * @see Executors#newSingleThreadScheduledExecutor()
     */
    public static InstrumentedScheduledExecutorService newSingleThreadScheduledExecutor(
            MetricRegistry registry, String name) {
        return new InstrumentedScheduledExecutorService
                (Executors.newSingleThreadScheduledExecutor(), registry, name);
    }

    /**
     * Creates a single-threaded instrumented executor that can schedule commands
     * to run after a given delay, or to execute periodically.
     * (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * {@code newScheduledThreadPool(1)} the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @return the newly created scheduled executor
     * @see Executors#newSingleThreadScheduledExecutor()
     */
    public static InstrumentedScheduledExecutorService newSingleThreadScheduledExecutor(MetricRegistry registry) {
        return new InstrumentedScheduledExecutorService(Executors.newSingleThreadScheduledExecutor(), registry);
    }

    /**
     * Creates a single-threaded instrumented executor that can schedule commands
     * to run after a given delay, or to execute periodically.  (Note
     * however that if this single thread terminates due to a failure
     * during execution prior to shutdown, a new one will take its
     * place if needed to execute subsequent tasks.)  Tasks are
     * guaranteed to execute sequentially, and no more than one task
     * will be active at any given time. Unlike the otherwise
     * equivalent {@code newScheduledThreadPool(1, threadFactory)}
     * the returned executor is guaranteed not to be reconfigurable to
     * use additional threads.
     *
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @param name          the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return a newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     * @see Executors#newSingleThreadExecutor(ThreadFactory)
     */
    public static InstrumentedScheduledExecutorService newSingleThreadScheduledExecutor(
            ThreadFactory threadFactory, MetricRegistry registry, String name) {
        return new InstrumentedScheduledExecutorService(
                Executors.newSingleThreadScheduledExecutor(threadFactory), registry, name);
    }

    /**
     * Creates a single-threaded instrumented executor that can schedule commands
     * to run after a given delay, or to execute periodically.  (Note
     * however that if this single thread terminates due to a failure
     * during execution prior to shutdown, a new one will take its
     * place if needed to execute subsequent tasks.)  Tasks are
     * guaranteed to execute sequentially, and no more than one task
     * will be active at any given time. Unlike the otherwise
     * equivalent {@code newScheduledThreadPool(1, threadFactory)}
     * the returned executor is guaranteed not to be reconfigurable to
     * use additional threads.
     *
     * @param threadFactory the factory to use when creating new threads
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @return a newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     * @see Executors#newSingleThreadExecutor(ThreadFactory)
     */
    public static InstrumentedScheduledExecutorService newSingleThreadScheduledExecutor(
            ThreadFactory threadFactory, MetricRegistry registry) {
        return new InstrumentedScheduledExecutorService(
                Executors.newSingleThreadScheduledExecutor(threadFactory), registry);
    }

    /**
     * Creates an instrumented thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle
     * @param registry     the {@link MetricRegistry} that will contain the metrics.
     * @param name         the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @see Executors#newScheduledThreadPool(int)
     */
    public static InstrumentedScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, MetricRegistry registry, String name) {
        return new InstrumentedScheduledExecutorService(
                Executors.newScheduledThreadPool(corePoolSize), registry, name);
    }

    /**
     * Creates an instrumented thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     *
     * @param corePoolSize the number of threads to keep in the pool,
     *                     even if they are idle
     * @param registry     the {@link MetricRegistry} that will contain the metrics.
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @see Executors#newScheduledThreadPool(int)
     */
    public static InstrumentedScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, MetricRegistry registry) {
        return new InstrumentedScheduledExecutorService(
                Executors.newScheduledThreadPool(corePoolSize), registry);
    }

    /**
     * Creates an instrumented thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     *
     * @param corePoolSize  the number of threads to keep in the pool,
     *                      even if they are idle
     * @param threadFactory the factory to use when the executor
     *                      creates a new thread
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @param name          the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException     if threadFactory is null
     * @see Executors#newScheduledThreadPool(int, ThreadFactory)
     */
    public static InstrumentedScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory, MetricRegistry registry, String name) {
        return new InstrumentedScheduledExecutorService(
                Executors.newScheduledThreadPool(corePoolSize, threadFactory), registry, name);
    }

    /**
     * Creates an instrumented thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     *
     * @param corePoolSize  the number of threads to keep in the pool, even if they are idle
     * @param threadFactory the factory to use when the executor creates a new thread
     * @param registry      the {@link MetricRegistry} that will contain the metrics.
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException     if threadFactory is null
     * @see Executors#newScheduledThreadPool(int, ThreadFactory)
     */
    public static InstrumentedScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory, MetricRegistry registry) {
        return new InstrumentedScheduledExecutorService(
                Executors.newScheduledThreadPool(corePoolSize, threadFactory), registry);
    }

    /**
     * Returns an instrumented default thread factory used to create new threads.
     * This factory creates all new threads used by an Executor in the
     * same {@link ThreadGroup}. If there is a {@link
     * java.lang.SecurityManager}, it uses the group of {@link
     * System#getSecurityManager}, else the group of the thread
     * invoking this {@code defaultThreadFactory} method. Each new
     * thread is created as a non-daemon thread with priority set to
     * the smaller of {@code Thread.NORM_PRIORITY} and the maximum
     * priority permitted in the thread group.  New threads have names
     * accessible via {@link Thread#getName} of
     * <em>pool-N-thread-M</em>, where <em>N</em> is the sequence
     * number of this factory, and <em>M</em> is the sequence number
     * of the thread created by this factory.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @param name     the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return a thread factory
     * @see Executors#defaultThreadFactory()
     */
    public static InstrumentedThreadFactory defaultThreadFactory(MetricRegistry registry, String name) {
        return new InstrumentedThreadFactory(Executors.defaultThreadFactory(), registry, name);
    }

    /**
     * Returns an instrumented default thread factory used to create new threads.
     * This factory creates all new threads used by an Executor in the
     * same {@link ThreadGroup}. If there is a {@link
     * java.lang.SecurityManager}, it uses the group of {@link
     * System#getSecurityManager}, else the group of the thread
     * invoking this {@code defaultThreadFactory} method. Each new
     * thread is created as a non-daemon thread with priority set to
     * the smaller of {@code Thread.NORM_PRIORITY} and the maximum
     * priority permitted in the thread group.  New threads have names
     * accessible via {@link Thread#getName} of
     * <em>pool-N-thread-M</em>, where <em>N</em> is the sequence
     * number of this factory, and <em>M</em> is the sequence number
     * of the thread created by this factory.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @return a thread factory
     * @see Executors#defaultThreadFactory()
     */
    public static InstrumentedThreadFactory defaultThreadFactory(MetricRegistry registry) {
        return new InstrumentedThreadFactory(Executors.defaultThreadFactory(), registry);
    }

    /**
     * Returns an instrumented thread factory used to create new threads that
     * have the same permissions as the current thread.
     * <p>
     * This factory creates threads with the same settings as {@link
     * Executors#defaultThreadFactory}, additionally setting the
     * AccessControlContext and contextClassLoader of new threads to
     * be the same as the thread invoking this
     * {@code privilegedThreadFactory} method.  A new
     * {@code privilegedThreadFactory} can be created within an
     * {@link java.security.AccessController#doPrivileged AccessController.doPrivileged}
     * action setting the current thread's access control context to
     * create threads with the selected permission settings holding
     * within that action.
     * </p>
     * <p>Note that while tasks running within such threads will have
     * the same access control and class loader settings as the
     * current thread, they need not have the same {@link
     * java.lang.ThreadLocal} or {@link
     * java.lang.InheritableThreadLocal} values. If necessary,
     * particular values of thread locals can be set or reset before
     * any task runs in {@link ThreadPoolExecutor} subclasses using
     * {@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)}.
     * Also, if it is necessary to initialize worker threads to have
     * the same InheritableThreadLocal settings as some other
     * designated thread, you can create a custom ThreadFactory in
     * which that thread waits for and services requests to create
     * others that will inherit its values.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @param name     the (metrics) name for this executor service, see {@link MetricRegistry#name(String, String...)}.
     * @return a thread factory
     * @throws java.security.AccessControlException if the current access control
     *                                              context does not have permission to both get and set context
     *                                              class loader
     * @see Executors#privilegedThreadFactory()
     */
    public static InstrumentedThreadFactory privilegedThreadFactory(MetricRegistry registry, String name) {
        return new InstrumentedThreadFactory(Executors.privilegedThreadFactory(), registry, name);
    }

    /**
     * Returns an instrumented thread factory used to create new threads that
     * have the same permissions as the current thread.
     * <p>
     * This factory creates threads with the same settings as {@link
     * Executors#defaultThreadFactory}, additionally setting the
     * AccessControlContext and contextClassLoader of new threads to
     * be the same as the thread invoking this
     * {@code privilegedThreadFactory} method.  A new
     * {@code privilegedThreadFactory} can be created within an
     * {@link java.security.AccessController#doPrivileged AccessController.doPrivileged}
     * action setting the current thread's access control context to
     * create threads with the selected permission settings holding
     * within that action.
     * </p>
     * <p>Note that while tasks running within such threads will have
     * the same access control and class loader settings as the
     * current thread, they need not have the same {@link
     * java.lang.ThreadLocal} or {@link
     * java.lang.InheritableThreadLocal} values. If necessary,
     * particular values of thread locals can be set or reset before
     * any task runs in {@link ThreadPoolExecutor} subclasses using
     * {@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)}.
     * Also, if it is necessary to initialize worker threads to have
     * the same InheritableThreadLocal settings as some other
     * designated thread, you can create a custom ThreadFactory in
     * which that thread waits for and services requests to create
     * others that will inherit its values.
     *
     * @param registry the {@link MetricRegistry} that will contain the metrics.
     * @return a thread factory
     * @throws java.security.AccessControlException if the current access control
     *                                              context does not have permission to both get and set context
     *                                              class loader
     * @see Executors#privilegedThreadFactory()
     */
    public static InstrumentedThreadFactory privilegedThreadFactory(MetricRegistry registry) {
        return new InstrumentedThreadFactory(Executors.privilegedThreadFactory(), registry);
    }

    /**
     * Cannot instantiate.
     */
    private InstrumentedExecutors() {
    }
}