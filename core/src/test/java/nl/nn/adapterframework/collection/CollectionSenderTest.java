package nl.nn.adapterframework.collection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.nn.adapterframework.configuration.ConfigurationException;
import nl.nn.adapterframework.core.PipeLineSession;
import nl.nn.adapterframework.senders.SenderTestBase;

public class CollectionSenderTest extends SenderTestBase<CollectorSenderBase<TestCollector, TestCollectorPart>> {

	private TestCollector collector = new TestCollector();

	@Override
	public CollectorSenderBase<TestCollector, TestCollectorPart> createSender() throws ConfigurationException {
		return new CollectorSenderBase<TestCollector, TestCollectorPart>() {
			@Override
			protected Collection<TestCollector, TestCollectorPart> getCollection(PipeLineSession session) throws CollectionException {
				return new Collection<TestCollector, TestCollectorPart>(collector);
			}
		};
	}

	@Test
	public void testWrite() throws Exception {
		sender.configure();
		sender.open();

		String input = "testWrite";
		sendMessage(input);

		assertEquals(true, collector.open);
		assertEquals(input, collector.getInput());
	}
}
