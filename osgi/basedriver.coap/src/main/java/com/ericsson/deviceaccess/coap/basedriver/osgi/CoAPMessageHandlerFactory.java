
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
package com.ericsson.deviceaccess.coap.basedriver.osgi;

import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerSender;

/**
 * A singleton factory for CoAPMessageHandler (takes care of incoming/outgoing
 * CoAP message handlers)
 */
public class CoAPMessageHandlerFactory {

    private static CoAPMessageHandlerFactory coapMessageHandlerFactory;
    private IncomingMessageHandler incomingMessageHandler;
    private OutgoingMessageHandler outgoingMessageHandler;

    /**
     * Constructor is private, use getInstance method to fetch an instance of
     * the CoAPMessageHandlerFactory
     */
    private CoAPMessageHandlerFactory() {
        incomingMessageHandler = null;
        outgoingMessageHandler = null;
    }

    /**
     * Get an instance of the CoAPMessageHandlerFactory. Creates one, if no
     * instance has been created or if one has been created already, return that
     * one
     *
     * @return an instance of CoAPMessageHandlerFactory
     */
    public static synchronized CoAPMessageHandlerFactory getInstance() {
        if (coapMessageHandlerFactory == null) {
            coapMessageHandlerFactory = new CoAPMessageHandlerFactory();
        }
        return coapMessageHandlerFactory;
    }

    /**
     * Returns a singleton instance of the IncomingMessageHandler. Creates one,
     * if no instance has been created yet. If one has been created already,
     * return that one.
     *
     * @return an instance of IncomingMessageHandler
     */
    public IncomingMessageHandler getIncomingCoAPMessageHandler() {
        synchronized (this) {
            if (incomingMessageHandler == null) {
                incomingMessageHandler = new IncomingMessageHandler();
            }
        }
        return incomingMessageHandler;
    }

    /**
     * Returns a singleton instance of the OutgoingMessageHandler. Creates one,
     * if no instance has been created yet. If one has been created already,
     * return that one.
     *
     * @param sender instance of a TransportLayerSender needed for sending messages
     * @return an instance of OutgoingMessageHandler
     */
    public OutgoingMessageHandler getOutgoingCoAPMessageHandler(
            TransportLayerSender sender) {
        synchronized (this) {
            if (outgoingMessageHandler == null) {
                outgoingMessageHandler = new OutgoingMessageHandler(sender);
            }
        }
        return outgoingMessageHandler;
    }

    /**
     * This method is called when the bundle is stopped. The message handlers
     * are set to null.
     */
    public static synchronized void stopService() {
        coapMessageHandlerFactory.outgoingMessageHandler = null;
        coapMessageHandlerFactory.incomingMessageHandler = null;
    }
}
