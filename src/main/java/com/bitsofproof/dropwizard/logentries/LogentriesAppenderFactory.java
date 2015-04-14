package com.bitsofproof.dropwizard.logentries;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.logentries.logback.LogentriesAppender;
import io.dropwizard.logging.AbstractAppenderFactory;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

@JsonTypeName("logentries")
public class LogentriesAppenderFactory extends AbstractAppenderFactory
{
    @NotNull
    @NotBlank
    @JsonProperty
    private String token;

    @NotNull
    @NotBlank
    @JsonProperty
    private String facility;

    @JsonProperty
    private String accountKey = "";

    @JsonProperty
    private String location = "";

    @JsonProperty
    private boolean ssl = false;

    @JsonProperty
    private boolean debug = false;

    @JsonProperty
    private boolean httpPut = false;

    @JsonProperty
    private boolean useDataHub = false;

    @JsonProperty
    private String dataHubAddr;

    @JsonProperty
    private int dataHubPort;

    @JsonProperty
    private boolean logHostName = false;

    @JsonProperty
    private String hostName = "";

    @JsonProperty
    private String logID = "";

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getFacility()
    {
        return facility;
    }

    public void setFacility(String facility)
    {
        this.facility = facility;
    }

    public String getAccountKey()
    {
        return accountKey;
    }

    /**
     * Sets the ACCOUNT KEY value for HTTP PUT.
     */
    public void setAccountKey(String account_key)
    {
        this.accountKey = account_key;
    }

    public String getLocation()
    {
        return location;
    }

    /**
     * Sets the LOCATION value for HTTP PUT.
     */
    public void setLocation(String log_location)
    {
        this.location = log_location;
    }

    public boolean isSsl()
    {
        return ssl;
    }

    /**
     * Sets the SSL boolean flag
     */
    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }

    public boolean isDebug()
    {
        return debug;
    }

    /**
     * Sets the debug flag.
     * <p>
     * <p>Appender in debug mode will print error messages on error console.</p>
     *
     * @param debug debug flag to set
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public boolean isHttpPut()
    {
        return httpPut;
    }

    /**
     * Sets the HTTP PUTflag. <p>Send logs via HTTP PUT instead of default Token
     * TCP.</p>
     *
     * @param httpPut true to use HTTP PUT API
     */
    public void setHttpPut(boolean httpPut)
    {
        this.httpPut = httpPut;
    }

    public boolean isUseDataHub()
    {
        return useDataHub;
    }

    public void setUseDataHub(boolean useDataHub)
    {
        this.useDataHub = useDataHub;
    }

    public String getDataHubAddr()
    {
        return dataHubAddr;
    }

    /**
     * Sets the address where DataHub server resides.
     *
     * @param dataHubAddr address like "127.0.0.1"
     */
    public void setDataHubAddr(String dataHubAddr)
    {
        this.dataHubAddr = dataHubAddr;
    }

    public int getDataHubPort()
    {
        return dataHubPort;
    }

    /**
     * Sets the port number on which DataHub instance waits for log messages.
     */
    public void setDataHubPort(int dataHubPort)
    {
        this.dataHubPort = dataHubPort;
    }

    public boolean isLogHostName()
    {
        return logHostName;
    }

    /**
     * Determines whether to send HostName alongside with the log message
     */
    public void setLogHostName(boolean logHostName)
    {
        this.logHostName = logHostName;
    }

    public String getHostName()
    {
        return hostName;
    }

    /**
     * Sets the HostName from the configuration
     */
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getLogID()
    {
        return logID;
    }

    /**
     * Sets LogID parameter from the configuration
     */
    public void setLogID(String logID)
    {
        this.logID = logID;
    }

    public TimeZone getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    /**
     * Sets the flag which determines if DataHub instance is used instead of Logentries service.
     *
     * @param useDataHub set to true to send log messaged to a DataHub instance.
     */
    public void setIsUsingDataHub(boolean useDataHub)
    {
        this.useDataHub = useDataHub;
    }

    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout)
    {
        LogentriesAppender appender = new LogentriesAppender();
        appender.setToken(token);
        appender.setKey(accountKey);
        appender.setFacility(facility);
        appender.setLocation(location);
        appender.setSsl(ssl);
        appender.setDebug(debug);
        appender.setHttpPut(httpPut);
        appender.setIsUsingDataHub(useDataHub);
        appender.setDataHubPort(dataHubPort);
        appender.setDataHubAddr(dataHubAddr);
        appender.setLogHostName(logHostName);
        appender.setHostName(hostName);
        appender.setLogID(logID);

        appender.setContext(context);
        appender.setSuffixPattern(logFormat);
        addThresholdFilter(appender, threshold);
        appender.stop();
        appender.start();

        return appender;
    }
}
