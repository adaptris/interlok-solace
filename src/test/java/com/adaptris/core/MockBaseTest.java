package com.adaptris.core;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public abstract class MockBaseTest {

  private AutoCloseable closeable;

  @Before
  public void mockSetUp() throws Exception {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @After
  public void mockTearDown() throws Exception {
    closeable.close();
  }

}
