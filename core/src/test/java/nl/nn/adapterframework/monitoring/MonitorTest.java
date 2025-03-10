package nl.nn.adapterframework.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import lombok.Getter;
import nl.nn.adapterframework.core.Adapter;
import nl.nn.adapterframework.core.PipeLineSession;
import nl.nn.adapterframework.monitoring.events.FireMonitorEvent;
import nl.nn.adapterframework.senders.EchoSender;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.testutil.TestConfiguration;

/**
 * This test tests the Spring pub/sub (publishEvent and onApplicationEvent) methods.
 */
public class MonitorTest {
	private static final String EVENT_CODE = "I'm an error code";

	private TestConfiguration configuration;
	private MonitorManager manager;

	@BeforeEach
	public void setup() {
		configuration = new TestConfiguration("testMonitoringContext.xml");
		configuration.refresh();
		manager = configuration.getBean("monitorManager", MonitorManager.class);
	}

	@AfterEach
	public void teardown() {
		configuration.close();
	}

	@Test
	public void testFireSpringEvent() throws Exception {
		Monitor monitor = configuration.createBean(Monitor.class);
		monitor.setName("monitorName");

		Alarm trigger = configuration.createBean(Alarm.class);
		trigger.addEventCode(EVENT_CODE);
		trigger.setSeverity(Severity.CRITICAL);
		monitor.registerTrigger(trigger);

		manager.addMonitor(monitor);

		SenderMonitorAdapter destination = configuration.createBean(SenderMonitorAdapter.class);
		destination.setName("myTestDestination");

		EchoSender sender = spy(EchoSender.class);
		ArgumentCaptor<Message> messageCapture = ArgumentCaptor.forClass(Message.class);
		ArgumentCaptor<PipeLineSession> sessionCapture = ArgumentCaptor.forClass(PipeLineSession.class);
		when(sender.sendMessage(messageCapture.capture(), sessionCapture.capture())).thenCallRealMethod();

		destination.setSender(sender);
		manager.registerDestination(destination);
		monitor.setDestinations(destination.getName());
		Message message = new Message("very important message");
		message.getContext().put("special-key", 123);
		configuration.configure();

		// Act
		configuration.publishEvent(EventThrowingClass.createMonitorEvent(message));

		// Assert
		Message capturedMessage = messageCapture.getValue();
		String result = "<event hostname=\"XXX\" monitor=\"monitorName\" source=\"TriggerTestClass\" type=\"TECHNICAL\" severity=\"CRITICAL\" event=\"I'm an error code\"/>";
		assertEquals(result, ignoreHostname(capturedMessage.asString()));
		PipeLineSession session = sessionCapture.getValue();
		assertTrue(session.containsKey(PipeLineSession.ORIGINAL_MESSAGE_KEY));
		Message originalMessage = session.getMessage(PipeLineSession.ORIGINAL_MESSAGE_KEY);
		assertEquals(message.asString(), originalMessage.asString());
		assertEquals(123, originalMessage.getContext().get("special-key"));
	}

	private String ignoreHostname(String result) {
		String firstPart = result.substring(result.indexOf("hostname=")+10);
		String hostname = firstPart.substring(0, firstPart.indexOf("\" "));
		return result.replaceAll(hostname, "XXX");
	}

	private static class EventThrowingClass implements EventThrowing {

		private @Getter String eventSourceName = "TriggerTestClass";
		private @Getter Adapter adapter;

		private static FireMonitorEvent createMonitorEvent(Message message) {
			EventThrowingClass event = new EventThrowingClass();
			return new FireMonitorEvent(event, EVENT_CODE, message);
		}
	}
}
