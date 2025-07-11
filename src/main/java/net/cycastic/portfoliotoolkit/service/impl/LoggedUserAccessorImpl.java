package net.cycastic.portfoliotoolkit.service.impl;

import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.Null;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.ApplicationConstants;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.JwtUtilities;
import net.cycastic.portfoliotoolkit.domain.SessionStorage;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.auth.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Component
@RequiredArgsConstructor
public class LoggedUserAccessorImpl implements LoggedUserAccessor {
    private record ClaimsWrapper(@Null Claims claims){}
    private static final String CLAIMS_IDENTIFIER = "$__claims";
    private static final Logger logger = LoggerFactory.getLogger(LoggedUserAccessorImpl.class);

    private final SessionStorage sessionStorage;
    private final JwtVerifier jwtVerifier;

    private @NonNull ServletRequestAttributes getAttributes(){
        var attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return Objects.requireNonNull(attr, () -> {
            throw new IllegalStateException("Must be called inside an HTTP request");
        });
    }

    @Override
    public Optional<Integer> tryGetProjectId() {
        var request = getAttributes().getRequest();
        var header = request.getHeader(ApplicationConstants.PROJECT_ID_HEADER);
        if (header == null){
            return Optional.empty();
        }

        return ApplicationUtilities.tryParseInt(header);
    }

    private Claims createClaimsFromRequest(){
        var request = getAttributes().getRequest();
        final var authHeader = request.getHeader("Authorization");
        if (authHeader == null || (!authHeader.startsWith("Bearer ") && !authHeader.startsWith("bearer "))){
            return null;
        }
        final var jwt = authHeader.substring("Bearer ".length());
        try {
            return jwtVerifier.extractClaims(jwt);
        } catch (RequestException e){
            logger.error("RequestException caught while extracting claims", e);
            return null;
        }
    }

    public @Null Claims getClaims(){
        var wrapper = sessionStorage.get(CLAIMS_IDENTIFIER, ClaimsWrapper.class);
        if (wrapper != null){
            return wrapper.claims;
        }

        var claims = createClaimsFromRequest();
        sessionStorage.put(CLAIMS_IDENTIFIER, new ClaimsWrapper(claims));
        return claims;
    }

    public boolean hasInvalidClaims(){
        var wrapper = sessionStorage.get(CLAIMS_IDENTIFIER, ClaimsWrapper.class);
        if (wrapper != null){
            return wrapper.claims == null;
        }
        var claims = createClaimsFromRequest();
        sessionStorage.put(CLAIMS_IDENTIFIER, new ClaimsWrapper(claims));
        return claims == null;
    }

    @Override
    public Optional<Integer> tryGetUserId() {
        var claims = getClaims();
        if (claims == null){
            return Optional.empty();
        }

        var entry = claims.getSubject();
        if (entry == null){
            return Optional.empty();
        }
        return ApplicationUtilities.tryParseInt(entry);
    }

    @Override
    public Set<String> getRoles() {
        var claims = getClaims();
        if (claims == null){
            return HashSet.newHashSet(0);
        }

        return JwtUtilities.extractRoles(claims);
    }

    @Override
    public String getRequestPath() {
        var attrs = getAttributes();
        var request = attrs.getRequest();

        var url = request.getRequestURL();
        var query = request.getQueryString();

        if (query != null) {
            url.append('?').append(query);
        }

        return url.toString();
    }

    @Override
    public Locale getRequestLocale() {
        return getAttributes().getRequest().getLocale();
    }
}
