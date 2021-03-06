/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 *
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,

 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.ericsson.deviceaccess.coap.basedriver.api.ext;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPService;
import com.ericsson.deviceaccess.coap.basedriver.api.IncomingCoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.osgi.IncomingMessageHandler;
import com.ericsson.deviceaccess.coap.basedriver.osgi.LocalCoAPEndpoint;
import com.ericsson.deviceaccess.coap.basedriver.osgi.OutgoingMessageHandler;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoAPExtEndpoint extends LocalCoAPEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoAPService.class);
    final private Set<IncomingCoAPRequestListener> reqListenerSet;

    public CoAPExtEndpoint(OutgoingMessageHandler outgoingMessageHandler,
            IncomingMessageHandler incomingMessageHandler, URI uri) {
        super(outgoingMessageHandler, incomingMessageHandler, uri);

        LOGGER.debug("Constructor");
        reqListenerSet = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public void handleRequest(CoAPRequest request) {
        LOGGER.debug("Handle request");
        reqListenerSet.forEach(listener -> listener.incomingRequestReceived(request));
    }

    public void setIncomingCoAPRequestListener(IncomingCoAPRequestListener reqListener) {
        LOGGER.debug("Set incoming CoAPRequestListener");
    }

    public void unsetIncomingCoAPRequestListener(IncomingCoAPRequestListener reqListener) {
        LOGGER.debug("Unset incoming CoAPRequestListener");
        reqListenerSet.remove(reqListener);
    }

}
