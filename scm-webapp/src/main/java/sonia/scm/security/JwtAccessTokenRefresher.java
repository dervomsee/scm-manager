package sonia.scm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JwtAccessTokenRefresher {

  private static final Logger log = LoggerFactory.getLogger(JwtAccessTokenRefresher.class);

  private final JwtAccessTokenBuilderFactory builderFactory;
  private final JwtAccessTokenRefreshStrategy refreshStrategy;
  private final Clock clock;

  @Inject
  public JwtAccessTokenRefresher(JwtAccessTokenBuilderFactory builderFactory, JwtAccessTokenRefreshStrategy refreshStrategy) {
    this(builderFactory, refreshStrategy, Clock.systemDefaultZone());
  }

  JwtAccessTokenRefresher(JwtAccessTokenBuilderFactory builderFactory, JwtAccessTokenRefreshStrategy refreshStrategy, Clock clock) {
    this.builderFactory = builderFactory;
    this.refreshStrategy = refreshStrategy;
    this.clock = clock;
  }

  public Optional<JwtAccessToken> refresh(JwtAccessToken oldToken) {
    JwtAccessTokenBuilder builder = builderFactory.create();
    Map<String, Object> claims = oldToken.getClaims();
    claims.forEach(builder::custom);

    if (canBeRefreshed(oldToken) && shouldBeRefreshed(oldToken)) {
      Optional<Object> parentTokenId = oldToken.getCustom("scm-manager.parentTokenId");
      if (!parentTokenId.isPresent()) {
        log.warn("no parent token id found in token; could not refresh");
        return Optional.empty();
      }
      builder.expiresIn(computeOldExpirationInMillis(oldToken), TimeUnit.MILLISECONDS);
      builder.parentKey(parentTokenId.get().toString());
      return Optional.of(builder.build());
    } else {
      return Optional.empty();
    }
  }

  private long computeOldExpirationInMillis(JwtAccessToken oldToken) {
    return oldToken.getExpiration().getTime() - oldToken.getIssuedAt().getTime();
  }

  private boolean canBeRefreshed(JwtAccessToken oldToken) {
    return tokenIsValid(oldToken) && tokenCanBeRefreshed(oldToken);
  }

  private boolean shouldBeRefreshed(JwtAccessToken oldToken) {
    return refreshStrategy.shouldBeRefreshed(oldToken);
  }

  private boolean tokenCanBeRefreshed(JwtAccessToken oldToken) {
    return oldToken.getRefreshExpiration().map(this::isAfterNow).orElse(false);
  }

  private boolean tokenIsValid(JwtAccessToken oldToken) {
    return isAfterNow(oldToken.getExpiration());
  }

  private boolean isAfterNow(Date expiration) {
    return expiration.toInstant().isAfter(clock.instant());
  }
}
