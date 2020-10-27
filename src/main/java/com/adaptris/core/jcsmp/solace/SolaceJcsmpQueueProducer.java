package com.adaptris.core.jcsmp.solace;

import javax.validation.Valid;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LoggingHelper;
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
   * The destination is the Solace Queue
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @ConfigDeprecated(removalVersion = "4.0.0", message = "Use 'queue' instead", groups = Deprecated.class)
  private ProduceDestination destination;

  /**
   * The Solace Queue
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String queue;

  private transient boolean destWarning;


  @Override
  public void prepare() throws CoreException {
    DestinationHelper.logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'queue' instead", LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(getQueue(), getDestination());
  }


  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return DestinationHelper.resolveProduceDestination(getQueue(), getDestination(), msg);
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
