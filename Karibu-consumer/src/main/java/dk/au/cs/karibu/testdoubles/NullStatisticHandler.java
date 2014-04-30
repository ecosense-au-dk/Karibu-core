package dk.au.cs.karibu.testdoubles;

import java.util.Date;

import dk.au.cs.karibu.backend.StatisticHandler;

public class NullStatisticHandler implements StatisticHandler {

  @Override
  public void notifyReceive(String producerCode, long countOfBytes) {
  }

  @Override
  public void flushToStorage() {
  }

  public Date getEndTimestamp() {
    return null;
  }

  public Date getStartTimestamp() {
    return null;
  }

  public String getMaxChunkProducerCode() {
    return null;
  }

  public long getTotalCountMsg() {
    return 0;
  }

  public long getTotalBytesSent() {
    return 0;
  }

  public long getMaxChunkSize() {
    return 0;
  }

  @Override
  public String getStatusAsString() {
    return "NullStatisticsHandler does not provide any real data";
  }

  @Override
  public String getDaemonIP() {
    return "NullStatisticsHandler has no IP";
  }

}
