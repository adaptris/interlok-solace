package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.interlok.util.Args;
import com.solacesystems.jcsmp.Destination;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 * This implementation of {@link AdaptrisMessageProducer} will use the Solace Jcsmp Api to produce messages to a queue on your Solace router.
 * </p>
 *
 * @author aaron
 * @config solace-jcsmp-queue-producer
 *
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component will produce your messages to the Solace VPN queue.", tag="queue,producer,solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-queue-producer")
@NoArgsConstructor
@DisplayOrder(order = {"queue", "maxWaitOnProduceMillis", "traceLogTimings", "messageTranslator"})
public class SolaceJcsmpQueueProducer extends SolaceJcsmpAbstractProducer {

  /**
   * The Solace Queue
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  @NotBlank
  private String queue;


  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getQueue(), "queue");
  }


  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(getQueue());
  }


  @Override
  protected Destination generateDestination(AdaptrisMessage msg, String queueName)
      throws Exception {
    if (getDestinationCache().containsKey(queueName))
      return getDestinationCache().get(queueName);
    else {
      getDestinationCache().put(queueName, jcsmpFactory().createQueue(queueName));
      return generateDestination(msg, queueName);
    }
  }

}
