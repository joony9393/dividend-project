package com.dayone.security;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 요청 하나당 필터 한 번 실행

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";    // 인증 타입을 나타낸다. jwt는 토큰 앞에 Bearer을 붙인다.
    public final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);

        if(StringUtils.hasText(token) && this.tokenProvider.validateToken(token)){
            // 토큰 유효성 검증
            org.springframework.security.core.Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info(String.format("[%s] -> %s", this.tokenProvider.getUsername(token)
                , request.getRequestURI()));
        }
        filterChain.doFilter(request, response);
    }

    // request 의 내부에서 헤더를 꺼내온다.
    private String resolveTokenFromRequest(HttpServletRequest request){
        String token = request.getHeader(TOKEN_HEADER);

        if(!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)){    // 토큰 존재하는 경우
            return token.substring(TOKEN_PREFIX.length());  // 앞에 고정 문자열 뒤의 실제 토큰
        }
        return null;
    }
}
