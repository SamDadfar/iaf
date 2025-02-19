/*
   Copyright 2018 Nationale-Nederlanden, 2021 WeAreFrank

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
package nl.nn.adapterframework.util.flow;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ResultHandler {
	private final BlockingQueue<String> value = new ArrayBlockingQueue<>(5);
	private boolean ok;

	public void setResult(String result) {
		ok = true;
		value.add(result);
	}

	public void setError(String error) {
		ok = false;
		value.add(error);
	}

	public String waitFor() throws FlowGenerationException {
		try {
			final String v = value.poll(5, TimeUnit.SECONDS); //Shouldn't take longer then 50 ms, but just to be sure..
			if(v == null) {
				throw new FlowGenerationException("Timeout exceeded");
			}
			if (ok) {
				return v;
			}
			throw new FlowGenerationException(v);
		} catch (InterruptedException e) {
			throw new FlowGenerationException("Waiting for result interrupted", e);
		}
	}
}
