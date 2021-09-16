package scg.blowback.http;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import scg.blowback.utils.functions.RequestFlowHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

public class SampleHttpRouting extends HttpRouting {

    @Bean
    public RouterFunction<ServerResponse> onRequest(RequestFlowHandler h) {
        return this.route(GET("/sample"), h);
    }

}
