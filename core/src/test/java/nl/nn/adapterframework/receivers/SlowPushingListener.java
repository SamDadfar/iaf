/*
   Copyright 2022-2023 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.adapterframework.receivers;

import javax.jms.Message;

import nl.nn.adapterframework.core.IMessageHandler;
import nl.nn.adapterframework.core.IPushingListener;
import nl.nn.adapterframework.core.IbisExceptionListener;
import nl.nn.adapterframework.core.PipeLineSession;

public class SlowPushingListener extends SlowListenerBase implements IPushingListener<Message> {

	@Override
	public void setHandler(IMessageHandler<Message> handler) {
		// No-op
	}

	@Override
	public void setExceptionListener(IbisExceptionListener listener) {
		// No-op
	}

	@Override
	public RawMessageWrapper<Message> wrapRawMessage(Message rawMessage, PipeLineSession session) {
		return null;
	}
}
