package by.nexus.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements WebFilter {

    private final WebClient webClient;
    private final String authUrl;

    public JwtAuthFilter(WebClient webClient, @Value("${auth.url.validate}") String authUrl) {
        this.webClient = webClient;
        this.authUrl = authUrl;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            return validateTokenWithAuthService(token)
                    .flatMap(isValid -> {
                        if (isValid) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(token, null, List.of());
                            SecurityContext context = new SecurityContextImpl(authToken);

                            exchange.getAttributes().put("jwt.authHeader", authHeader);

                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder
                                            .withSecurityContext(Mono.just(context)));
                        } else {
                            exchange.getResponse().setRawStatusCode(401);
                            return exchange.getResponse().setComplete();
                        }
                    });
        }

        return chain.filter(exchange);
    }

    private Mono<Boolean> validateTokenWithAuthService(String token) {
        return webClient.post()
                .uri("lb://auth-service" + authUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }
}

