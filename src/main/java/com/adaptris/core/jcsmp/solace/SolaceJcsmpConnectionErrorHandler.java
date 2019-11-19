/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * The Interlok {@link ConnectionErrorHandler} implementation specifically for {@link SolaceJcsmpConnection} 
 *  </p>
 *  <p>
 *  Should a connection be disconnected or report an error, this handler will fire, attempting to restart everything.
 *  </p>
 * @author aaron
 *
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP connection error handler, used to restart affected components on error.", tag="ceh,connection,solace,jcsmp")
@XStreamAlias("solace-jcsmp-connection-error-handler")
public class SolaceJcsmpConnectionErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

}
