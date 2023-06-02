package com.adaptris.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class MockBaseTest {

  private AutoCloseable closeable;

  @BeforeEach
  public void mockSetUp() throws Exception {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void mockTearDown() throws Exception {
    closeable.close();
  }

}
