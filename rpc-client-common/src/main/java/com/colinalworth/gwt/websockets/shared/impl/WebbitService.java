/**
 *  Copyright 2011 Colin Alworth
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.colinalworth.gwt.websockets.shared.impl;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Dummy service interface used to convince RPC internals that a real RPC call is being made.
 *
 */
public interface WebbitService extends RemoteService {
	ClientInvocation dummy(ServerInvocation serverInvocation);
}
