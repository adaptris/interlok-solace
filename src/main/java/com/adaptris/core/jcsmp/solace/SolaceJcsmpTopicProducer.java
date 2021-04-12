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
@DisplayOrder(order = {"topic", "maxWaitOnProduceMillis", "traceLogTimings", "messageTranslator"})
public class SolaceJcsmpTopicProducer extends SolaceJcsmpAbstractProducer {

  /**
   * The Solace Topic
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  @NotBlank
  private String topic;


  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getTopic(), "topic");
  }


  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(getTopic());
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
