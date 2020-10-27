package com.adaptris.core.jcsmp.solace;

import javax.validation.Valid;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LoggingHelper;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.thoughtworks.xstream.annotations.XStreamAlias;
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
public class SolaceJcsmpQueueConsumer extends SolaceJcsmpAbstractConsumer {

  private transient FlowReceiver flowReceiver;
  private transient boolean destinationWarningLogged = false;

  /**
   * The consume destination is the queue that we receive messages from.
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @ConfigDeprecated(removalVersion = "4.0.0", message = "Use 'queue' instead", groups = Deprecated.class)
  private ConsumeDestination destination;
  /**
   * The Solace Queue
   *
   */
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String queue;


  @Override
  public void startReceive() throws Exception {
    setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());

    final Queue queue = jcsmpFactory().createQueue(queueName());
    // Actually provision it, and do not fail if it already exists
    getCurrentSession().provision(queue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    setFlowReceiver(getCurrentSession().createFlow(this, createConsumerFlowProperties(queue), createEndpointProperties()));

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

  FlowReceiver getFlowReceiver() {
    return flowReceiver;
  }

  void setFlowReceiver(FlowReceiver flowReceiver) {
    this.flowReceiver = flowReceiver;
  }

  @Override
  public void prepare() throws CoreException {
    DestinationHelper.logConsumeDestinationWarning(destinationWarningLogged,
        () -> destinationWarningLogged = true, getDestination(),
        "{} uses destination, use 'queue' instead", LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(getQueue(), getDestination());
  }

  @Override
  protected String newThreadName() {
    return DestinationHelper.threadName(retrieveAdaptrisMessageListener(), getDestination());
  }

  private String queueName() {
    return DestinationHelper.consumeDestination(getQueue(), getDestination());
  }
}
