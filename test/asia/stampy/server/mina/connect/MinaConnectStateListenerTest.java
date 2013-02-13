/*
 * Copyright (C) 2013 Burton Alexander
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 */
package asia.stampy.server.mina.connect;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.CONNECT;
import static asia.stampy.common.message.StompMessageType.DISCONNECT;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.STOMP;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.apache.mina.core.service.IoServiceListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.AbstractMinaListenerTest;
import asia.stampy.server.listener.connect.AlreadyConnectedException;
import asia.stampy.server.listener.connect.NotConnectedException;

@RunWith(MockitoJUnitRunner.class)
public class MinaConnectStateListenerTest extends AbstractMinaListenerTest {
  private MinaConnectStateListener connect = new MinaConnectStateListener();

  @Before
  public void before() throws Exception {
    connect.setGateway(serverGateway);

    verify(serverGateway).addServiceListener(any(IoServiceListener.class));

    connect.messageReceived(getMessage(DISCONNECT), hostPort);
  }

  @Test
  public void testTypes() throws Exception {
    testTypes(connect, StompMessageType.values());
  }

  @Test
  public void testConnected() throws Exception {
    StompMessageType[] connectedTypes = { ABORT, ACK, BEGIN, COMMIT, NACK, SEND, SUBSCRIBE, UNSUBSCRIBE };

    for (StompMessageType type : connectedTypes) {
      try {
        connect.messageReceived(getMessage(type), hostPort);
        fail("Should have thrown not connected exception");
      } catch (NotConnectedException e) {
        // expected
      }
    }

    connect.messageReceived(getMessage(CONNECT), hostPort);

    try {
      connect.messageReceived(getMessage(CONNECT), hostPort);
      fail("Should have thrown not connected exception");
    } catch (AlreadyConnectedException e) {
      // expected
    }

    try {
      connect.messageReceived(getMessage(STOMP), hostPort);
      fail("Should have thrown not connected exception");
    } catch (AlreadyConnectedException e) {
      // expected
    }
  }

  @Test
  public void testDisconnect() throws Exception {
    connect.messageReceived(getMessage(CONNECT), hostPort);
    try {
      connect.messageReceived(getMessage(CONNECT), hostPort);
      fail("Should have thrown not connected exception");
    } catch (AlreadyConnectedException e) {
      // expected
    }
    connect.messageReceived(getMessage(DISCONNECT), hostPort);
    connect.messageReceived(getMessage(CONNECT), hostPort);
  }

}