package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.interlok.util.Args;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 * This implementation of {@link AdaptrisMessageConsumer} will use the Solace Jcsmp Api to consume messages from a Queue on your Solace router.
 * </p>
 * <p>
 * There are four main components that you will need to configure;
 * <ul>
 * <li><b>Destination: </b> The Solace end point to consume messages from.</li>
 * <li><b>End point permissions: </b> Should match the Solace configured endpoint properties.</li>
 * <li><b>End point access type: </b> Should match either EXCLUSIVE or NONEXCLUSIVE</li>
 * <li><b>Acknowledge mode: </b> Should either be CLIENT or AUTO.</li>
 * </ul>
 * </p>
 * @author aaron
 * @config solace-jcsmp-queue-consumer
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that consumes your messages from the Solace VPN.", tag="queue,consumer,solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-queue-consumer")
@NoArgsConstructor
@DisplayOrder(order = {"queue", "acknowledgeMode", "endpointPermissions", "endpointAccessType"})
public class SolaceJcsmpQueueConsumer extends SolaceJcsmpAbstractConsumer {

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient FlowReceiver flowReceiver;

  /**
   * The Solace Queue
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String queue;


  @Override
  public void startReceive() throws Exception {
    getSessionHelper().createSession();

    final Queue queue = jcsmpFactory().createQueue(queueName());
    // Actually provision it, and do not fail if it already exists
    getSessionHelper().getSession().provision(queue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    setFlowReceiver(getSessionHelper().getSession().createFlow(this, createConsumerFlowProperties(queue), createEndpointProperties()));

    getFlowReceiver().start();
  }

  @Override
  public void stop() {
    if(getFlowReceiver() != null)
      getFlowReceiver().stop();
    super.stop();
  }

  @Override
  public void close() {
    if(getFlowReceiver() != null)
      getFlowReceiver().close();
    super.close();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getQueue(), "queue");
  }

  @Override
  protected String newThreadName() {
    return DestinationHelper.threadName(retrieveAdaptrisMessageListener());
  }

  private String queueName() {
    return getQueue();
  }
}
