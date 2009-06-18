package ccnmtl.vital3.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring web HandlerInterceptor that binds a Hibernate Session to the thread for the
 * duration of the handler execution. This is in contrast to the "Open Session in View" pattern,
 * which keeps the session open throughout the entire duration of the request execution,
 * including the view. Perhaps this is overreacting, but I want to ensure that the
 * database operations are completely finished before the view is ever involved. In case
 * of any errors, the postHandle method can modify the ModelAndView. It makes no sense to
 * use this class for unit tests because it is hibernate-only. For this reason, exceptions
 * should not be handled here! Maybe create another interceptor for catching them?
 *
 * <p>The original class used a default flush mode of FLUSH_NEVER. I have thought about
 * whether this is a good idea and I think it may be wiser for us to use FLUSH_AUTO
 * for Vital3. Therefore I have changed the default to FLUSH_AUTO, but obviously this
 * may be overridden by calling setFlushMode(FLUSH_NEVER). Note that by using FLUSH_AUTO,
 * changes are essentially saved automatically to already-persisted objects!
 *
 * <p>Edited original documentation from OpenSessionInViewInterceptor follows.
 *
 * <p>This interceptor works similar to the AOP HibernateInterceptor: It just makes
 * Hibernate Sessions available via the thread. It is suitable for non-transactional
 * execution but also for middle tier transactions via HibernateTransactionManager
 * or JtaTransactionManager. In the latter case, Sessions pre-bound by this interceptor
 * will automatically be used for the transactions and flushed accordingly.
 *
 * <p>In contrast to OpenSessionInViewFilter, this interceptor is set up in a Spring
 * application context and can thus take advantage of bean wiring. It derives from
 * HibernateAccessor to inherit common Hibernate configuration properties.
 *
 * <p><b>WARNING:</b> Applying this interceptor to existing logic can cause issues that
 * have not appeared before, through the use of a single Hibernate Session for the
 * processing of an entire request. In particular, the reassociation of persistent
 * objects with a Hibernate Session has to occur at the very beginning of request
 * processing, to avoid clashes will already loaded instances of the same objects.
 *
 * <p>Alternatively, turn this interceptor into deferred close mode, by specifying
 * "singleSession"="false": It will not use a single session per request then,
 * but rather let each data access operation or transaction use its own session
 * (like without Open Session in View). Each of those sessions will be registered
 * for deferred close, though, actually processed at HANDLER completion.
 *
 * <p>A single session per request allows for most efficient first-level caching,
 * but can cause side effects, for example on saveOrUpdate or if continuing
 * after a rolled-back transaction. The deferred close strategy is as safe as
 * no Open Session in View in that respect, while still allowing for lazy loading
 * in views (but not providing a first-level cache for the entire request).
 *
 *
 * @author Juergen Hoeller, Eric Mattes
 */
public class OpenSessionInHandlerInterceptor extends HibernateAccessor implements HandlerInterceptor {
    
	/**
     * Suffix that gets appended to the SessionFactory toString representation
	 * for the "participate in existing session handling" request attribute.
	 * @see #getParticipateAttributeName
	 */
	public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";
    
    
	/**
     * Create a new OpenSessionInHandlerInterceptor,
	 * turning the default flushMode to FLUSH_AUTO.
	 * @see #setFlushMode
	 */
	public OpenSessionInHandlerInterceptor() {
		setFlushMode(FLUSH_AUTO);
	}
    
    
	/**
     * Open a new Hibernate Session according to the settings of this HibernateAccessor
	 * and binds in to the thread via TransactionSynchronizationManager.
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws DataAccessException {
            
            // if there is already an open session, we want to participate in it. We mark the request with
            // a count variable so we can keep track of how many stacked interceptors are using the session.
            // See code in postHandle to see what happens after the handling...
            
            if (TransactionSynchronizationManager.hasResource(getSessionFactory()) || SessionFactoryUtils.isDeferredCloseActive(getSessionFactory())) {
                // do not modify the Session: just mark the request accordingly
                String participateAttributeName = getParticipateAttributeName();
                Integer count = (Integer) request.getAttribute(participateAttributeName);
                int newCount = (count != null) ? count.intValue() + 1 : 1;
                request.setAttribute(getParticipateAttributeName(), new Integer(newCount));
            
            } else {
                
                logger.debug("Opening single Hibernate Session in OpenSessionInHandlerInterceptor");
                Session session = SessionFactoryUtils.getSession(getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
                applyFlushMode(session, false);
                TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
            }
            
            return true;
        }
    
	/**
     * Flush the Hibernate Session before view rendering, if necessary.
	 * Note that this just applies in single session mode!
	 * <p>The default is FLUSH_NEVER to avoid this extra flushing, assuming that
	 * middle tier transactions have flushed their changes on commit.
	 * @see #setFlushMode
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
        throws DataAccessException {
            
            // Flush if necessary
            
            SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
            logger.debug("Flushing single Hibernate Session in OpenSessionInHandlerInterceptor");
            try {
                flushIfNecessary(sessionHolder.getSession(), false);
            }
            catch (HibernateException ex) {
                throw convertHibernateAccessException(ex);
            }
            
            
            /*** THE CODE BELOW HERE WAS ORIGINALLY IN "AFTERCOMPLETION" (except my comments)
                  Although it is tempting to try and merge this with what is above, I believe they
                  need to be separate and in this sequence. -Eric
            ***/
            
            // If we are participating in a session which was opened by an earlier interceptor,
            // we cannot close the session!
            
            String participateAttributeName = getParticipateAttributeName();
            Integer count = (Integer) request.getAttribute(participateAttributeName);
            if (count == null) {
                sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
                logger.debug("Closing single Hibernate Session in OpenSessionInHandlerInterceptor");
                SessionFactoryUtils.releaseSession(sessionHolder.getSession(), getSessionFactory());
                
            }
            
        }
    
	/**
     * This releases the session in case a thrown exception precluded the postHandle method from executing.
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws DataAccessException {
            
            // count is decremented here:
            
            String participateAttributeName = getParticipateAttributeName();
            Integer count = (Integer) request.getAttribute(participateAttributeName);
            if (count != null) {
                // Do not modify the Session: just clear the marker.
                if (count.intValue() > 1) {
                    request.setAttribute(participateAttributeName, new Integer(count.intValue() - 1));
                } else {
                    request.removeAttribute(participateAttributeName);
                }
            } else {
                // redundant session-closing in case of errors:
                if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
                    SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
                    logger.debug("Redundantly closing single Hibernate Session in OpenSessionInHandlerInterceptor");
                    SessionFactoryUtils.releaseSession(sessionHolder.getSession(), getSessionFactory());
                }
            }
        }
    
	/**
     * Return the name of the request attribute that identifies that a request is
	 * already filtered. Default implementation takes the toString representation
	 * of the SessionFactory instance and appends ".PARTICIPATE".
	 * @see #PARTICIPATE_SUFFIX
	 */
	protected String getParticipateAttributeName() {
		return getSessionFactory().toString() + PARTICIPATE_SUFFIX;
	}
    
}
