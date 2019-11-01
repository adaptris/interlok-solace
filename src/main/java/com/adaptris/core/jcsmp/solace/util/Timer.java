package com.adaptris.core.jcsmp.solace.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timer {
  
  protected static final Logger log = LoggerFactory.getLogger(Timer.class.getName());
  
  protected static final DecimalFormat format = new DecimalFormat("###,###,###.00");
  
  private static final double MILLION = 1000000;
  
  private static Map<String, Timing> timings;

  static {
    timings = new HashMap<String, Timer.Timing>();
  }
  
  public static void start(String name, String description) {
    if(!timings.containsKey(name)) {
      Timing timing = new Timing(name, description);
      timing.lastStart = System.nanoTime();
      timings.put(name, timing);
    } else
      timings.get(name).lastStart = System.nanoTime();
  }
  
  public static double stop(String name) {
    if(timings.containsKey(name)) {
      Timing timing = timings.get(name);
      timing.lastStop = System.nanoTime();
      timing.lastDif = timing.lastStop - timing.lastStart; 
      timing.totalTime += timing.lastDif;
      timing.numberOfTimings ++;
      return timing.lastDif / MILLION;
    } else
      return 0;
  }
  
  public static void stopAndLog(String name) {
    if(timings.containsKey(name)) {
      Timing timing = timings.get(name);
      timing.lastStop = System.nanoTime();
      timing.lastDif = timing.lastStop - timing.lastStart; 
      timing.totalTime += timing.lastDif;
      timing.numberOfTimings ++;
      log.trace("Timer [{}]: last time {} nanos, avg time {} nanos.", timing.name, format.format(timing.lastDif), format.format(timing.totalTime / timing.numberOfTimings));
    } 
  }
  
  public static double getLastTimingMs(String name) {
    if(timings.containsKey(name)) {
      return timings.get(name).lastDif / MILLION;
    } else
      return 0;
  }
  
  public static double getLastTimingNanos(String name) {
    if(timings.containsKey(name)) {
      return timings.get(name).lastDif;
    } else
      return 0;
  }
  
  public static double getAvgTimingMs(String name) {
    if(timings.containsKey(name)) {
      Timing timing = timings.get(name);
      return (timing.totalTime / timing.numberOfTimings) / MILLION;
    } else
      return 0;
  }
  
  public static double getAvgTimingNanos(String name) {
    if(timings.containsKey(name)) {
      Timing timing = timings.get(name);
      return (timing.totalTime / timing.numberOfTimings);
    } else
      return 0;
  }
  
  public static void clear() {
    timings.clear();
  }
  
  public static void clear(String name) {
    if(timings.containsKey(name)) {
      Timing timing = timings.get(name);
      timing.lastStart = 0;
      timing.lastStop = 0;
      timing.lastDif = 0;
      timing.numberOfTimings = 0;
      timing.totalTime = 0;
    }
  }
  
  static class Timing {
    String name;
    String description;
    double lastStart;
    double lastStop;
    double lastDif;
    double totalTime;
    double numberOfTimings;
    
    public Timing(String name) {
      this.name = name;
    }
    
    public Timing(String name, String description) {
      this.name = name;
      this.description = description;
    }
  }
}
