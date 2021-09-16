package scg.blowback.http.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import scg.blowback.http.ClockProvider;
import scg.blowback.http.properties.HttpRoutingProperties;
import scg.blowback.http.RqIdGenerator;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(HttpRoutingProperties.class)
public class HttpRoutingConfiguration {

    @Bean
    public RqIdGenerator rqIdGenerator() {
        return RqIdGenerator.randomizer();
    }

    @Bean
    public ClockProvider systemUTCService() {
        return Clock::systemUTC;
    }

}
