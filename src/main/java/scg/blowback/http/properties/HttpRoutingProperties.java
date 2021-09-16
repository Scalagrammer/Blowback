package scg.blowback.http.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("http-routing")
public class HttpRoutingProperties {

    private DebugMode debugger = new DebugMode();

    @Data
    public static class DebugMode {
        private boolean enabled = false;
    }

}
