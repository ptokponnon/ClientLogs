package com.pegolab.cyber;

import java.time.LocalDateTime;
import java.util.Objects;

public class LogEvent implements Comparable{
    LocalDateTime localDateTime;
    String ipAddress;
    boolean isLogIn;

    public LogEvent(LocalDateTime ldt, String ipAddress, boolean isLogIn) {
        this.localDateTime = ldt;
        this.ipAddress = ipAddress;
        this.isLogIn = isLogIn;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isLogIn() {
        return isLogIn;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setLogIn(boolean isLogIn) {
        this.isLogIn = isLogIn;
    }

    @Override
    public int compareTo(Object o) {
        return getLocalDateTime().compareTo(((LogEvent)o).getLocalDateTime());
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
 
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof LogEvent)) {
            return false;
        }      
        
        LogEvent le = (LogEvent) o;
        return localDateTime.equals(le.getLocalDateTime()) && ipAddress.equals(le.getIpAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDateTime, ipAddress);
    }    

}
