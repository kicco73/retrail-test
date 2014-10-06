/*
 * CNR - IIT
 * Coded by: 2014 Enrico "KMcC;) Carniani
 */

package it.cnr.iit.retrail.test;

import it.cnr.iit.retrail.commons.PepRequestInterface;
import it.cnr.iit.retrail.commons.PepSessionInterface;
import it.cnr.iit.retrail.commons.impl.PepRequest;
import it.cnr.iit.retrail.commons.impl.PepAttribute;
import it.cnr.iit.retrail.commons.impl.PepSession;
import it.cnr.iit.retrail.commons.Status;
import it.cnr.iit.retrail.server.pip.impl.PIP;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kicco
 */
// Expiring attribute handler
public class TestPIPSessions extends PIP {

    protected int sessions = 0;
    final public String id = "openSessions";
    final public String category = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    
    public TestPIPSessions() {
        super();
        this.log = LoggerFactory.getLogger(TestPIPSessions.class);
    }

    @Override
    public void onBeforeTryAccess(PepRequestInterface request) {
        log.info("Number of open sessions: " + sessions);
        PepAttribute test = newSharedAttribute(id, "http://www.w3.org/2001/XMLSchema#integer", Integer.toString(sessions), "http://localhost:8080/federation-id-prov/saml", category);
        request.add(test);
    }

    @Override
    public void onBeforeStartAccess(PepRequestInterface request, PepSessionInterface session) {
        sessions++;
        log.info("Number of open sessions: " + sessions);
        PepAttribute test = newSharedAttribute(id, "http://www.w3.org/2001/XMLSchema#integer", Integer.toString(sessions), "http://localhost:8080/federation-id-prov/saml", category);
        request.add(test);
    }
    
    @Override
    public void onAfterStartAccess(PepRequestInterface request, PepSessionInterface session) {
        if(session.getStatus() != Status.ONGOING)
            sessions--;
        log.info("Number of open sessions: {}, status = {}", sessions, session.getStatus());
    }
    
    @Override
    public void onBeforeEndAccess(PepRequestInterface request, PepSessionInterface session) {
        if(session.getStatus() != Status.TRY)
           sessions--;
        log.info("Number of open sessions: {}, status = {}", sessions, session.getStatus());
    }
    
}
