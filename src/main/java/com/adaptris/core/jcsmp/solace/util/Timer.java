package com.adaptris.core.jcsmp.solace.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timer {
  
  protected static final Logger log = LoggerFactory.getLogger(Timer.class.getName());
  
  protected static final DecimalFormat format = new DecimalFormat("###,###,##0.00");
  
  private static final double MILLION = 1000000;
  
  private static Map<String, Timing> timings;

  static {
    timings = new HashMap<String, Timer.Timing>();
  }
  
  public static void start(String name, int resetAfterNumberTimings) {
    start(name, resetAfterNumberTimings, timings);
  }
  
  protected static void start(String name, int resetAfterNumberTimings, Map<String, Timing> timingsMap) {
    if(!timingsMap.containsKey(name)) {
      Timing timing = new Timing(name, resetAfterNumberTimings);
      timing.lastStart = System.nanoTime();
      timingsMap.put(name, timing);
    } else {
      if(timingsMap.get(name).numberOfTimings >= timingsMap.get(name).resetAfterNumberTimings)
        clear(name, timingsMap);
      timingsMap.get(name).lastStart = System.nanoTime();
    }
  }
  
  public static void start(String parentname, String name, int resetAfterNumberTimings) {
    if(timings.containsKey(parentname)) {
      Timing parentTiming = timings.get(parentname);
      start(name, resetAfterNumberTimings, parentTiming.timings);
    }
  }
  
  public static double stop(String name) {
    return stop(name, timings);
  }
  
  public static double stop(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing parentTiming = timings.get(parentName);
      return stop(name, parentTiming.timings);
    }
    return 0;
  }

  protected static double stop(String name, Map<String, Timing> timingMap) {
    if(timingMap.containsKey(name)) {
      Timing timing = timingMap.get(name);
      timing.lastStop = System.nanoTime();
      timing.lastDif = timing.lastStop - timing.lastStart; 
      timing.totalTime += timing.lastDif;
      timing.numberOfTimings ++;
      double result = timing.lastDif / MILLION;
      
      return result;
    } else
      return 0;
  }
  
  public static void stopAndLog(String name) {
    stopAndLog(name, timings);
  }
  
  public static void stopAndLog(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing parentTiming = timings.get(parentName);
      stopAndLog(name, parentTiming.timings);
    }
  }
  
  protected static void stopAndLog(String name, Map<String, Timing> timingMap) {
    if(timingMap.containsKey(name)) {
      Timing timing = timingMap.get(name);
      timing.lastStop = System.nanoTime();
      timing.lastDif = timing.lastStop - timing.lastStart; 
      timing.totalTime += timing.lastDif;
      timing.numberOfTimings ++;
      timing.log();
    } 
  }
  
  public static void log(String name) {
    if(timings.containsKey(name)) {
      timings.get(name).log();
    }
  }
  
  public static void log(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing timing = timings.get(parentName);
      if(timing.timings.containsKey(name))
        timing.timings.get(name).log();
    }
  }
  
  public static double getLastTimingMs(String name) {
    if(timings.containsKey(name)) {
      return timings.get(name).lastDif / MILLION;
    } else
      return 0;
  }
  
  public static double getLastTimingMs(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing timing = timings.get(parentName);
      if(timing.timings.containsKey(name))
        return timing.timings.get(name).lastDif / MILLION;
      else
        return 0;
    } else
      return 0;
  }
  
  public static double getLastTimingNanos(String name) {
    if(timings.containsKey(name)) {
      return timings.get(name).lastDif;
    } else
      return 0;
  }
  
  public static double getLastTimingNanos(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing timing = timings.get(parentName);
      if(timing.timings.containsKey(name))
        return timing.timings.get(name).lastDif;
      else
        return 0;
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
  
  public static double getAvgTimingMs(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing parentTiming = timings.get(parentName);
      if(parentTiming.timings.containsKey(name)) {
          Timing timing = parentTiming.timings.get(name);
          return (timing.totalTime / timing.numberOfTimings) / MILLION;
      } else
        return 0;
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
  
  public static double getAvgTimingNanos(String parentName, String name) {
    if(timings.containsKey(parentName)) {
      Timing timing = timings.get(parentName);
      if(timing.timings.containsKey(name))
        return (timing.totalTime / timing.numberOfTimings);
      else
        return 0;
    } else
      return 0;
  }
  
  public static void clear() {
    timings.clear();
  }
  
  public static void clear(String name, Map<String, Timing> timingsMap) {
    if(timingsMap.containsKey(name)) {
      Timing timing = timingsMap.get(name);
      timing.lastStart = 0;
      timing.lastStop = 0;
      timing.lastDif = 0;
      timing.numberOfTimings = 0;
      timing.totalTime = 0;
    }
  }
  
  static class Timing {
    String name;
    int resetAfterNumberTimings;
    double lastStart;
    double lastStop;
    double lastDif;
    double totalTime;
    double numberOfTimings;
    private Map<String, Timing> timings;
    
    public Timing(String name) {
      this.name = name;
      timings = new HashMap<>();
    }
    
    public Timing(String name, int resetAfterNumberTimings) {
      this(name);
      this.resetAfterNumberTimings = resetAfterNumberTimings;
    }
    
    public void log() {
      StringBuffer logBuffer = new StringBuffer();
      logBuffer.append("\n" + this.logString(this));
      
      this.timings.forEach((key, value) -> {
        logBuffer.append("\n\t" + this.logString(value));
      });
      log.trace(logBuffer.toString());
    }
    
    protected String logString(Timing timing) {
      return "Timer [" + timing.name + "]: last time " + format.format(timing.lastDif) + " nanos, avg time " 
            + format.format((timing.totalTime / timing.numberOfTimings) / MILLION) + " ms number of timings " + timing.numberOfTimings;
    }
  }
}
