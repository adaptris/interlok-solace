package com.adaptris.core.jcsmp.solace;

import javax.validation.Valid;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
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
 * This implementation of {@link AdaptrisMessageProducer} will use the Solace Jcsmp Api to produce messages to a topic on your Solace router.
 * </p>
 *
 * @author aaron
 * @config solace-jcsmp-topic-producer
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component will produce your messages to the Solace VPN topic.", tag="topic,producer,solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-topic-producer")
@NoArgsConstructor
@DisplayOrder(order = {"queue", "maxWaitOnProduceMillis", "traceLogTimings", "messageTranslator"})
public class SolaceJcsmpTopicProducer extends SolaceJcsmpAbstractProducer {

  /**
   * The destination represents the base-directory where you are producing files to.
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @Removal(version = "4.0.0", message = "Use 'path' instead")
  private ProduceDestination destination;

  /**
   * The SMB Path to write files to in the form {@code \\server-name\shareName\path\to\dir}.
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String topic;
  private transient boolean destWarning;


  @Override
  public void prepare() throws CoreException {
    DestinationHelper.logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'topic' instead", LoggingHelper.friendlyName(this));
    DestinationHelper.mustHaveEither(getTopic(), getDestination());
  }


  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return DestinationHelper.resolveProduceDestination(getTopic(), getDestination(), msg);
  }

  @Override
  protected Destination generateDestination(AdaptrisMessage msg, String topicName)
      throws Exception {
    if(getDestinationCache().containsKey(topicName))
      return getDestinationCache().get(topicName);
    else {
      getDestinationCache().put(topicName, jcsmpFactory().createTopic(topicName));
      return generateDestination(msg, topicName);
    }
  }

}
